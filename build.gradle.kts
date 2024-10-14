plugins {
    java
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "net.azisaba.simpleproxy"
version = "1.0.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }

    withJavadocJar()
    withSourcesJar()
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.azisaba.net/repository/maven-public/") }
    maven { url = uri("https://repo.acrylicstyle.xyz/repository/maven-public/") }
}

dependencies {
    compileOnly("net.azisaba.simpleproxy:api:2.1.0")
    compileOnly("org.jetbrains:annotations:24.0.0")
    compileOnly("it.unimi.dsi:fastutil:8.5.11")
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

val javaComponent = components["java"] as AdhocComponentWithVariants
javaComponent.withVariantsFromConfiguration(configurations["sourcesElements"]) {
    skip()
}

publishing {
    repositories {
        maven {
            name = "repo"
            credentials(PasswordCredentials::class)
            url = uri(
                    if (project.version.toString().endsWith("SNAPSHOT"))
                        project.findProperty("deploySnapshotURL") ?: System.getProperty("deploySnapshotURL", "https://repo.azisaba.net/repository/maven-snapshots/")
                    else
                        project.findProperty("deployReleasesURL") ?: System.getProperty("deployReleasesURL", "https://repo.azisaba.net/repository/maven-releases/")
            )
        }
    }

    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks.getByName("sourcesJar"))
        }
    }
}
