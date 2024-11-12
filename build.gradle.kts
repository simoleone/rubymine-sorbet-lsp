plugins {
  id("java")
  id("org.jetbrains.kotlin.jvm") version "1.9.25"
  id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "im.simo.rubymine.sorbetlsp"
version = providers.gradleProperty("pluginVersion").get()

kotlin {
  jvmToolchain(21)
}

repositories {
  mavenCentral()
  intellijPlatform {
    defaultRepositories()
  }
}

dependencies {
  intellijPlatform {
    rubymine("2024.2")
    bundledPlugin("org.jetbrains.plugins.ruby")
    instrumentationTools()
    pluginVerifier()
    zipSigner()
  }
}

intellijPlatform {
  pluginConfiguration {
    version = providers.gradleProperty("pluginVersion").get()

    ideaVersion {
      sinceBuild = "242"
      // no max build. see https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-tasks.html#patchPluginXml-untilBuild
      untilBuild = provider { null }
    }
  }

  signing {
    privateKey = providers.environmentVariable("PRIVATE_KEY").orNull
    password = providers.environmentVariable("PRIVATE_KEY_PASSWORD").orNull
    certificateChain = providers.environmentVariable("CERTIFICATE_CHAIN").orNull
  }

  publishing {
    token = providers.environmentVariable("PUBLISH_TOKEN").orNull
  }

  pluginVerification {
    ides {
      recommended()
    }
  }
}

tasks {
  wrapper {
    gradleVersion = providers.gradleProperty("gradleVersion").get()
  }
}