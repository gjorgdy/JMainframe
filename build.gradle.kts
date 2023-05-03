plugins {
    id("java")
}

group = "nl.gjorgdy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // MongoDB wrapper
    implementation("org.mongodb:mongodb-driver-sync:4.9.1")
    // Discord API wrapper
    implementation("net.dv8tion:JDA:5.0.0-beta.8")
    // slf4j 2 Log4j
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
}

tasks.test {
    useJUnitPlatform()
}