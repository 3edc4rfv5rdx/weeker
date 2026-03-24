import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.kapt)
}

val keyProperties = Properties()
val keyPropertiesFile = sequenceOf(
    File("/home/e/.my-safe/key.properties"),
    rootProject.file("key.properties")
).firstOrNull { it.exists() } ?: rootProject.file("key.properties")
val hasReleaseSigning = keyPropertiesFile.exists().also { exists ->
    if (exists) {
        keyPropertiesFile.inputStream().use { keyProperties.load(it) }
    }
}
val keyStoreFile = (keyProperties["storeFile"] as String?)?.let { rawPath ->
    val candidate = File(rawPath)
    if (candidate.isAbsolute) candidate else File(keyPropertiesFile.parentFile, rawPath)
}

val buildNumberFile = rootProject.file("build_number.txt")
val buildMetaRaw = buildNumberFile.takeIf { it.exists() }?.readText()?.trim().orEmpty()
val buildMetaMap = buildMetaRaw.lineSequence()
    .map { it.trim() }
    .filter { it.contains("=") }
    .associate { line ->
        val key = line.substringBefore("=").trim()
        val value = line.substringAfter("=").trim()
        key to value
    }
val isBuildInvocation = gradle.startParameter.taskNames.any { task ->
    val name = task.lowercase()
    (name.contains("assemble") || name.contains("bundle") || name.contains("install"))
        && !name.contains("release")
}
val storedBuildNumber = run {
    val direct = buildMetaRaw.toIntOrNull()
    if (direct != null) {
        direct
    } else {
        buildMetaMap["build"]?.toIntOrNull()
            ?: 32
    }
}
val buildNumber = if (isBuildInvocation) storedBuildNumber + 1 else storedBuildNumber.coerceAtLeast(33)
val baseVersion = buildMetaMap["base_version"]?.ifBlank { "0.5" } ?: "0.5"
val versionDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
val computedVersionName = "$baseVersion.$versionDate"
if (isBuildInvocation) {
    buildNumberFile.writeText(
        """
        base_version=$baseVersion
        build=$buildNumber
        version=$computedVersionName
        """.trimIndent() + "\n"
    )
}

android {
    namespace = "com.weeker.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.weeker.app"
        minSdk = 26
        targetSdk = 34
        versionCode = buildNumber
        versionName = computedVersionName

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = keyStoreFile
                storePassword = keyProperties["storePassword"] as String
                keyAlias = keyProperties["keyAlias"] as String
                keyPassword = keyProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            signingConfig = signingConfigs.findByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    splits {
        abi {
            isEnable = true
            reset()
            include("arm64-v8a", "armeabi-v7a", "x86_64")
            isUniversalApk = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
        jniLibs {
            useLegacyPackaging = false
        }
    }
}

abstract class RenameReleaseApks : DefaultTask() {
    @get:org.gradle.api.tasks.Input
    abstract val versionName: Property<String>

    @get:org.gradle.api.tasks.Input
    abstract val versionCode: Property<Int>

    @get:org.gradle.api.tasks.Internal
    abstract val outputDir: DirectoryProperty

    @org.gradle.api.tasks.TaskAction
    fun rename() {
        val outDir = outputDir.get().asFile
        val prefix = "weeker-${versionName.get()}+${versionCode.get()}-release"
        val mappings = mapOf(
            "app-universal-release.apk" to "$prefix-universal.apk",
            "app-arm64-v8a-release.apk" to "$prefix-arm64-v8a.apk",
            "app-armeabi-v7a-release.apk" to "$prefix-armeabi-v7a.apk",
            "app-x86_64-release.apk" to "$prefix-x86_64.apk"
        )
        mappings.forEach { (srcName, dstName) ->
            val src = File(outDir, srcName)
            if (!src.exists()) return@forEach
            val dst = File(outDir, dstName)
            if (dst.exists()) dst.delete()
            src.renameTo(dst)
        }
    }
}

val renameReleaseApks by tasks.registering(RenameReleaseApks::class) {
    versionName.set(computedVersionName)
    versionCode.set(buildNumber)
    outputDir.set(layout.buildDirectory.dir("outputs/apk/release"))
}

tasks.configureEach {
    if (name == "assembleRelease") {
        finalizedBy(renameReleaseApks)
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)

    implementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
