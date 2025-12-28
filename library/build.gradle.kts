import com.gradleup.librarian.gradle.Librarian

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
}

kotlin {
  jvm()

  sourceSets {
    commonMain {
      dependencies {
        // Ktor
        implementation(Ktor.client.core)
        implementation(Ktor.client.contentNegotiation)
        implementation(Ktor.client.auth)
        implementation(Ktor.client.logging)
        implementation(Ktor.plugins.serialization.kotlinx.json)

        // Logging
        implementation("org.jraf.klibnanolog:klibnanolog:_")
      }
    }

    jvmMain {
      dependencies {
        implementation(KotlinX.coroutines.jdk9)
        implementation(Ktor.client.okHttp)
      }
    }
  }
}

Librarian.module(project)
