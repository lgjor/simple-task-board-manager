plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "2.26.0"
}

group = "br.com.dio"
version = "1.0.5"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.google.com") }
}

repositories {
    mavenCentral()
    maven { url = uri ("https://repo1.maven.org/maven2/") }
}

dependencies {
    implementation("org.openjfx:javafx-controls:24.0.1")
    implementation("org.openjfx:javafx-fxml:24.0.1")
    implementation("org.openjfx:javafx-swing:24.0.1")
    implementation("org.liquibase:liquibase-core:4.26.0")
    implementation("org.liquibase:liquibase-groovy-dsl:3.0.2")

    implementation("info.picocli:picocli:4.6.1")
    implementation("mysql:mysql-connector-java:5.1.34")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    // Google Tasks API
    implementation("com.google.api-client:google-api-client:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20250518-2.0.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.47.0")
    runtimeOnly("com.google.apis:google-api-services-tasks:v1-rev71-1.25.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

javafx {
    version = "24.0.1"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("org.desviante.Main")
}

sourceSets {
    test {
        java {
            setSrcDirs(listOf("src/test/java"))
        }
    }
}



tasks.test {
    useJUnitPlatform()
}