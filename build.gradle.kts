plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.14"
}

group = "br.com.dio"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1")
    implementation("mysql:mysql-connector-java:8.0.33")
    implementation("org.projectlombok:lombok:1.18.38")
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")

    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("br.com.dio.Main")
    applicationDefaultJvmArgs = listOf(
        "--module-path", "C:/Users/Lucas/Downloads/javafx-sdk-21.0.2/lib",
        "--add-modules", "javafx.controls,javafx.fxml"
    )
}

java {
    modularity.inferModulePath = true
}