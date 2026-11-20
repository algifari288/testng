plugins {
    java
}

group = "com.hiyura"
version = "0.1.0"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")
    compileOnly(fileTree("libs") { include("*.jar") })
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

sourceSets {
    main {
        java {
            setSrcDirs(listOf("."))
            include("*.java")
        }
        resources {
            setSrcDirs(listOf("."))
            include("plugin.yml", "config.yml")
        }
    }
}

tasks.processResources {
    filesMatching("plugin.yml") { expand("version" to project.version) }
}
