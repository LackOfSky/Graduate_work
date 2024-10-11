import java.math.BigInteger
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.spec.DHParameterSpec

class DiffieHellmanKeyExchange {
    private val keyPair: KeyPair
    val privateKey: PrivateKey
    val publicKey: PublicKey

    init {
        // Генерация параметров DH с использованием безопасных параметров
        val keyPairGenerator = KeyPairGenerator.getInstance("DH")

        // Генерация ключевой пары с безопасными параметрами
        keyPairGenerator.initialize(2048) // Используйте 2048 бит или более для безопасности
        keyPair = keyPairGenerator.generateKeyPair()

        // Установка приватного и публичного ключей
        privateKey = keyPair.private
        publicKey = keyPair.public
    }

    fun getEncodedPrivateKey(): ByteArray {
        return privateKey.encoded
    }

    fun getEncodedPublicKey(): ByteArray {
        return publicKey.encoded
    }

    fun loadPublicKey(encodedKey: ByteArray): PublicKey {
        val keySpec = X509EncodedKeySpec(encodedKey)
        val keyFactory = KeyFactory.getInstance("DH")
        return keyFactory.generatePublic(keySpec)
    }

    fun loadPrivateKey(encodedKey: ByteArray): PrivateKey {
        val keySpec = PKCS8EncodedKeySpec(encodedKey)
        val keyFactory = KeyFactory.getInstance("DH")
        return keyFactory.generatePrivate(keySpec)
    }
}