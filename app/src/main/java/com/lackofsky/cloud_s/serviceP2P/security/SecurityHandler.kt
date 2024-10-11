import io.netty.channel.ChannelDuplexHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelPromise
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class SecurityHandler(private val isClient: Boolean) : ChannelDuplexHandler() {

    private val cipher: Cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", BouncyCastleProvider())
    private val iv: ByteArray = ByteArray(16) // 16 байт для IV (AES)
    private lateinit var sharedKey: SecretKey
    private val secureRandom: SecureRandom = SecureRandom()

    // Экземпляр класса для DH-обмена
    private val dhKeyExchange: DiffieHellmanKeyExchange = DiffieHellmanKeyExchange()

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    override fun channelActive(ctx: ChannelHandlerContext) {
        if (isClient) {
            // Отправляем публичный ключ на сервер
            val publicKey = dhKeyExchange.publicKey
            ctx.writeAndFlush(Unpooled.wrappedBuffer(publicKey.encoded))
        }
        super.channelActive(ctx)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is ByteBuf) {
            val receivedData = ByteArray(msg.readableBytes())
            msg.readBytes(receivedData)

            // Проверка на то, что это публичный ключ или зашифрованные данные
            if (isClient && receivedData.size == 204) { // пример размера публичного ключа (может варьироваться)
                // Обработка публичного ключа сервера
                val serverPublicKey = dhKeyExchange.loadPublicKey(receivedData)
                generateSharedSecret(serverPublicKey)
            } else if (!isClient && receivedData.size == 204) { // пример размера публичного ключа
                // Обработка публичного ключа клиента
                val clientPublicKey = dhKeyExchange.loadPublicKey(receivedData)
                generateSharedSecret(clientPublicKey)
            } else {
                // Обработка зашифрованных данных
                decryptData(receivedData, ctx)
            }
        } else {
            ctx.fireChannelRead(msg)
        }
    }

    private fun generateSharedSecret(remotePublicKey: PublicKey) {
        try {
            val keyAgree = KeyAgreement.getInstance("DH")
            keyAgree.init(dhKeyExchange.privateKey)
            keyAgree.doPhase(remotePublicKey, true)
            val secret = keyAgree.generateSecret()
            // Генерация ключа AES из секрета
            sharedKey = SecretKeySpec(secret.take(16).toByteArray(), "AES") // Используйте первые 16 байт
        } catch (e: Exception) {
            // Обработка исключений
            e.printStackTrace()
            throw RuntimeException("Ошибка при генерации общего секрета: ${e.message}")
        }
    }

    private fun decryptData(receivedData: ByteArray, ctx: ChannelHandlerContext) {
        // Извлекаем IV из входящих данных
        val iv = ByteArray(16)
        val encryptedData = ByteArray(receivedData.size - iv.size)

        System.arraycopy(receivedData, 0, iv, 0, iv.size)
        System.arraycopy(receivedData, iv.size, encryptedData, 0, encryptedData.size)

        try {
            // Дешифрование данных
            cipher.init(Cipher.DECRYPT_MODE, sharedKey, IvParameterSpec(iv))
            val decryptedData = cipher.doFinal(encryptedData)

            // Передаем расшифрованные данные дальше по pipeline
            ctx.fireChannelRead(Unpooled.wrappedBuffer(decryptedData))
        } catch (e: Exception) {
            // Обработка ошибок дешифрования
            e.printStackTrace()
            throw RuntimeException("Ошибка при дешифровании данных: ${e.message}")
        }
    }

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {
        if (msg is ByteBuf) {
            val dataToEncrypt = ByteArray(msg.readableBytes())
            msg.readBytes(dataToEncrypt)

            // Генерация случайного вектора инициализации (IV)
            secureRandom.nextBytes(iv)

            try {
                // Шифрование данных
                cipher.init(Cipher.ENCRYPT_MODE, sharedKey, IvParameterSpec(iv))
                val encryptedData = cipher.doFinal(dataToEncrypt)

                // Формируем сообщение: IV + Зашифрованные данные
                val combinedMessage = ByteArray(iv.size + encryptedData.size)
                System.arraycopy(iv, 0, combinedMessage, 0, iv.size)
                System.arraycopy(encryptedData, 0, combinedMessage, iv.size, encryptedData.size)

                // Отправляем зашифрованные данные
                ctx.write(Unpooled.wrappedBuffer(combinedMessage), promise)
            } catch (e: Exception) {
                // Обработка ошибок шифрования
                e.printStackTrace()
                throw RuntimeException("Ошибка при шифровании данных: ${e.message}")
            }
        } else {
            ctx.write(msg, promise)
        }
    }
}
