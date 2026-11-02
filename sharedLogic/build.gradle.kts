import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "SharedLogic"
            isStatic = true
            export(project(":core"))
            export(project(":feature:onboarding"))
            export(project(":feature:home"))
            export(project(":feature:accounts"))
        }
    }

    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all {
            linkerOpts("-lsqlite3")
        }
    }
    
    jvm()
    
    android {
       namespace = "com.prodan.taktfinances.sharedLogic"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        commonMain.dependencies {
            api(project(":core"))
            api(project(":feature:onboarding"))
            api(project(":feature:home"))
            api(project(":feature:accounts"))
            api(libs.sqldelight.runtime)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            api(libs.sqldelight.android.driver)
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            api(libs.sqldelight.native.driver)
        }
        jvmMain.dependencies {
            api(libs.sqldelight.sqlite.driver)
        }
    }
}
