plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.azisaba.simpleProxy"
version = "0.0.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
    maven { url = uri("https://nexus.velocitypowered.com/repository/velocity-artifacts-release/") }
}

dependencies {
    implementation("com.velocitypowered:velocity-native:3.1.0")
    compileOnly("net.azisaba.simpleProxy:api:0.0.3-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.8")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

tasks {
    withType<ProcessResources> {
        filteringCharset = "UTF-8"
        from(sourceSets.main.get().resources.srcDirs) {
            include("**")

            val tokenReplacementMap = mapOf(
                "version" to project.version,
                "name" to project.name,
            )

            filter<org.apache.tools.ant.filters.ReplaceTokens>("tokens" to tokenReplacementMap)
        }

        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        from(projectDir) { include("LICENSE") }
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }
}