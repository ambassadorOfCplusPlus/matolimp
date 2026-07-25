import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

// AI-конфиг читаем из local.properties (не в гите), значения попадают в BuildConfig.
val aiProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
fun aiProp(key: String, def: String): String = aiProps.getProperty(key) ?: def

android {
    namespace = "com.matolimp.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.matolimp.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "0.1"
        vectorDrawables { useSupportLibrary = true }

        buildConfigField("String", "AI_BASE_URL", "\"${aiProp("ai.baseUrl", "https://routerai.ru/api/v1")}\"")
        buildConfigField("String", "AI_API_KEY", "\"${aiProp("ai.apiKey", "")}\"")
        buildConfigField("String", "AI_MODEL", "\"${aiProp("ai.model", "deepseek/deepseek-v4-flash")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
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

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.2")

    // Рендер LaTeX-формул на Canvas (без WebView)
    implementation("ru.noties:jlatexmath-android:0.2.0")

    // HTTP-клиент для AI-объяснения (RouterAI, OpenAI-совместимый)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    testImplementation("junit:junit:4.13.2")
}
