plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.0"
}

group = "nl.gjorgdy"
version = "1.0-SNAPSHOT"

tasks.jar {
    manifest {
        attributes["Main-Class"] = "nl.gjorgdy.Mainframe"
    }
}


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
    // DOTENV config
    implementation("io.github.cdimascio:dotenv-java:2.2.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.7.1")
}

tasks.test {
    useJUnitPlatform()
}