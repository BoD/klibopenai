plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("maven-publish")
  id("org.jetbrains.dokka")
  id("signing")
}

tasks {
  // Generate a Version.kt file with a constant for the version name
  register("generateVersionKt") {
    val outputDir = layout.buildDirectory.dir("generated/source/kotlin").get().asFile
    outputs.dir(outputDir)
    doFirst {
      val outputWithPackageDir = File(outputDir, "org/jraf/klibopenai/internal").apply { mkdirs() }
      File(outputWithPackageDir, "Version.kt").writeText(
        """
          package org.jraf.klibopenai.internal
          internal const val VERSION = "${project.version}"
        """.trimIndent()
      )
    }
  }

  // Generate Javadoc (Dokka) Jar
  register<Jar>("dokkaHtmlJar") {
    archiveClassifier.set("javadoc")
    from("${layout.buildDirectory}/dokka")
    dependsOn(dokkaHtml)
  }
}

kotlin {
  jvm()
  jvmToolchain(11)

  sourceSets {
    val commonMain by getting {
      kotlin.srcDir(tasks.getByName("generateVersionKt").outputs.files)
      dependencies {
        // Ktor
        implementation(Ktor.client.core)
        implementation(Ktor.client.contentNegotiation)
        implementation(Ktor.client.auth)
        implementation(Ktor.client.logging)
        implementation(Ktor.plugins.serialization.kotlinx.json)
      }
    }

    val jvmMain by getting {
      dependencies {
        // Slf4j
        implementation("org.slf4j:slf4j-api:_")
        implementation("org.slf4j:slf4j-simple:_")
        implementation(KotlinX.coroutines.jdk9)
        implementation(Ktor.client.okHttp)
      }
    }
  }
}

publishing {
  repositories {
    maven {
      // Note: declare your user name / password in your home's gradle.properties like this:
      // mavenCentralNexusUsername = <user name>
      // mavenCentralNexusPassword = <password>
      url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2")
      name = "mavenCentralNexus"
      credentials(PasswordCredentials::class)
    }
  }

  publications.withType<MavenPublication>().forEach { publication ->
    publication.artifact(tasks.getByName("dokkaHtmlJar"))

    publication.pom {
      name.set("klibopenai")
      description.set("A Kotlin (JVM) library to interact with the Minitel")
      url.set("https://github.com/BoD/klibopenai")
      licenses {
        license {
          name.set("The Apache License, Version 2.0")
          url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          distribution.set("repo")
        }
      }
      developers {
        developer {
          id.set("BoD")
          name.set("Benoit 'BoD' Lubek")
          email.set("BoD@JRAF.org")
          url.set("https://JRAF.org")
          organization.set("JRAF.org")
          organizationUrl.set("https://JRAF.org")
          roles.set(listOf("developer"))
          timezone.set("+1")
        }
      }
      scm {
        connection.set("scm:git:https://github.com/BoD/klibopenai")
        developerConnection.set("scm:git:https://github.com/BoD/klibopenai")
        url.set("https://github.com/BoD/klibopenai")
      }
      issueManagement {
        url.set("https://github.com/BoD/klibopenai/issues")
        system.set("GitHub Issues")
      }
    }
  }
}

signing {
  // Note: declare the signature key, password and file in your home's gradle.properties like this:
  // signing.keyId=<8 character key>
  // signing.password=<your password>
  // signing.secretKeyRingFile=<absolute path to the gpg private key>
  sign(publishing.publications)
}

// Workaround for https://youtrack.jetbrains.com/issue/KT-46466
val dependsOnTasks = mutableListOf<String>()
tasks.withType<AbstractPublishToMaven>().configureEach {
  dependsOnTasks.add(this.name.replace("publish", "sign").replaceAfter("Publication", ""))
  dependsOn(dependsOnTasks)
}

tasks.dokkaHtml.configure {
  outputDirectory.set(rootProject.file("docs"))
}

// Run `./gradlew dokkaHtml` to generate the docs
// Run `./gradlew publishToMavenLocal` to publish to the local maven repo
// Run `./gradlew publish` to publish to Maven Central (then go to https://oss.sonatype.org/#stagingRepositories and "close", and "release")
