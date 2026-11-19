plugins {
    java
}

group = "id.hiyura"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.extendedclip.com/releases/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    // Semua .jar di folder libs/ ikut dipakai saat kompilasi (tidak dimasukkan ke jar hasil)
    compileOnly(fileTree("libs") { include("*.jar") })
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

tasks.jar {
    archiveFileName.set("testng.jar")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}
