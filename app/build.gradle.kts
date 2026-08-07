plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.example.dynamicfeatures"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.dynamicfeatures"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
    dynamicFeatures += setOf(":onboarding", ":premium")
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.feature.delivery.ktx)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.dynamic.features)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val buildLocalTestingApksDebug by tasks.registering(JavaExec::class) {
    group = "local-testing"
    description = "Builds app-local-test.apks from app-debug.aab with --local-testing enabled"
    dependsOn("bundleDebug")
    classpath = rootProject.buildscript.configurations.getByName("classpath")
    mainClass.set("com.android.tools.build.bundletool.BundleToolMain")
    val aabFile = layout.buildDirectory.file("outputs/bundle/debug/app-debug.aab")
    val apksFile = layout.buildDirectory.file("outputs/app-local-test.apks")
    val adbFile = androidComponents.sdkComponents.adb
    inputs.file(aabFile)
    outputs.file(apksFile)
    argumentProviders.add(CommandLineArgumentProvider {
        val sdkRoot = adbFile.get().asFile.parentFile.parentFile
        val aapt2File = sdkRoot.resolve("build-tools").listFiles()?.maxByOrNull { it.name }?.resolve("aapt2")
        listOfNotNull(
            "build-apks",
            "--bundle=${aabFile.get().asFile.absolutePath}",
            "--output=${apksFile.get().asFile.absolutePath}",
            aapt2File?.let { "--aapt2=${it.absolutePath}" },
            "--local-testing",
            "--overwrite",
        )
    })
}

val installLocalTestingDebug by tasks.registering(JavaExec::class) {
    group = "local-testing"
    description = "Uninstalls previous APK, installs app-local-test.apks in --local-testing mode, and launches MainActivity"
    dependsOn(buildLocalTestingApksDebug)
    classpath = rootProject.buildscript.configurations.getByName("classpath")
    mainClass.set("com.android.tools.build.bundletool.BundleToolMain")
    val apksFile = layout.buildDirectory.file("outputs/app-local-test.apks")
    val adbFile = androidComponents.sdkComponents.adb
    doFirst {
        ProcessBuilder(adbFile.get().asFile.absolutePath, "uninstall", "com.example.dynamicfeatures")
            .start()
            .waitFor()
    }
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(
            "install-apks",
            "--apks=${apksFile.get().asFile.absolutePath}",
            "--adb=${adbFile.get().asFile.absolutePath}",
        )
    })
    doLast {
        ProcessBuilder(
            adbFile.get().asFile.absolutePath,
            "shell",
            "am",
            "start",
            "-n",
            "com.example.dynamicfeatures/.MainActivity"
        ).start().waitFor()
    }
}