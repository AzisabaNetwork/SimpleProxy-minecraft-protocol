plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.azisaba.simpleProxy"
version = "0.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
}

dependencies {
    compileOnly("net.azisaba.simpleProxy:api:1.1.2")
    compileOnly("org.jetbrains:annotations:23.0.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.8")
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

tasks {
    processResources {
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

    test {
        useJUnitPlatform()
    }
}