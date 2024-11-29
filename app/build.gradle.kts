plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.devtoolsKsp)
    kotlin("kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.lackofsky.cloud_s"
    compileSdk = 34



    defaultConfig {
        applicationId = "com.lackofsky.cloud_s"
        minSdk = 34
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }

    packaging {
        resources.excludes.add("META-INF/versions/9/OSGI-INF/MANIFEST.MF")
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/*"
        }

    }
}


dependencies {



    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")

    implementation("androidx.compose.material3:material3:1.1.0")
    implementation("androidx.compose.ui:ui:1.4.7")
    implementation("androidx.compose.ui:ui-tooling-preview:1.4.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.compose.runtime:runtime-livedata")
    implementation("androidx.activity:activity-compose:1.7.2")


    implementation("androidx.core:core-ktx:1.12.0")

    implementation("androidx.navigation:navigation-compose:2.5.0")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")

    //adding image
    implementation("io.coil-kt:coil-compose:2.4.0")
    //datastore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    //JSON
    implementation ("com.google.code.gson:gson:2.8.8")
    //p2p
    //implementation("androidx.wifi:wifi-aware:1.1.0")
    implementation("org.jmdns:jmdns:3.5.8")
    implementation("io.netty:netty-all:4.1.96.Final") // Подключает все необходимые модули
    implementation("org.bouncycastle:bcpkix-jdk18on:1.78.1")
    // Или же можно добавить выборочно
//    implementation("io.netty:netty-transport:4.1.96.Final")  // Транспорт (TCP/UDP)
//    implementation("io.netty:netty-handler:4.1.96.Final")    // Для шифрования и обработки данных
//    implementation("io.netty:netty-buffer:4.1.96.Final")     // Для управления буферами
//    implementation("io.netty:netty-codec:4.1.96.Final")
// Hilt dependencies
    implementation("com.google.dagger:hilt-android:2.50")
    kapt("com.google.dagger:hilt-android-compiler:2.50")
    kapt("androidx.hilt:hilt-compiler:1.2.0")
    implementation("androidx.hilt:hilt-navigation-fragment:1.2.0")

//Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation (libs.androidx.runtime.livedata)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    annotationProcessor(libs.androidx.room.room.compiler)
    ksp(libs.androidx.room.room.compiler)
    implementation(kotlin("script-runtime"))
}

kapt {
    correctErrorTypes = true
}