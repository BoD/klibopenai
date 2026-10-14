plugins {
  alias(libs.plugins.kotlin.jvm)
  application
}

application {
  mainClass.set("org.jraf.klibopenai.sample.MainKt")
}

dependencies {
  // Kotlin
  implementation(libs.kotlinx.coroutines.jdk9)

  // Logging
  implementation(libs.klibnanolog)

  // Library
  implementation(project(":klibopenai"))
}
