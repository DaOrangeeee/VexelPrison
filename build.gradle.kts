plugins {
    java
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.vexelcore"
version = "1.0.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")

    implementation("org.xerial:sqlite-jdbc:3.46.0.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("com.google.code.gson:gson:2.11.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveClassifier.set("")
        relocate("com.zaxxer.hikari", "com.vexelcore.prison.libs.hikari")
        relocate("org.sqlite", "com.vexelcore.prison.libs.sqlite")
        relocate("com.mysql", "com.vexelcore.prison.libs.mysql")
    }
    processResources {
        filteringCharset = "UTF-8"
    }
    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(21)
    }
}
