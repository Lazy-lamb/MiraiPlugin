plugins {
    val kotlinVersion = "1.5.10"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("net.mamoe.mirai-console") version "2.8.0-M1"
}

group = "org.cyy"
version = "2.0"

repositories {
    maven("https://maven.aliyun.com/repository/public")
    mavenCentral()
}
dependencies {
    // https://mvnrepository.com/artifact/org.quartz-scheduler/quartz
    implementation("org.quartz-scheduler:quartz:2.3.2")
    dependencies {
        api("net.mamoe:mirai-console-terminal:2.8.0-M1") // 自行替换版本
        api("net.mamoe:mirai-core:2.8.0-M1")
    }
}
