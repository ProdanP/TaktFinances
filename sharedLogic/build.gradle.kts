import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.sqldelight)
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
            implementation(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.androidDriver)
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
        jvmMain.dependencies {
            implementation(libs.sqldelight.sqliteDriver)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

// The one app database. Each feature owns the .sq files for its tables and is listed here.
sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.prodan.taktfinances.db")
            dependency(project(":feature:accounts"))
        }
    }
}