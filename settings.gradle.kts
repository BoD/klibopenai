plugins {
  // See https://jmfayard.github.io/refreshVersions
  id("de.fayard.refreshVersions") version "0.50.2"
}

rootProject.name = "klibopenai-root"

include(":library")
project(":library").name = "klibopenai"

include(":sample")
