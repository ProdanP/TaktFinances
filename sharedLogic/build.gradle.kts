import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
}

kotlin {
    // Room's @ConstructedBy needs an expect object (AppDatabaseConstructor).
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

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
            api(libs.koin.core)
            implementation(libs.room3.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.kotlinx.coroutinesCore)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.koin.test)
            implementation(libs.kotlinx.coroutinesTest)
        }
    }
}

// Room's exported schema is the reviewed record of the app database.
room3 {
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    listOf("kspAndroid", "kspIosArm64", "kspIosSimulatorArm64", "kspJvm").forEach {
        add(it, libs.room3.compiler)
    }
}
