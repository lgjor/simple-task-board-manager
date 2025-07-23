plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
}

group = "org.desviante"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.fxml")
}

dependencies {
    // --- Bean Validation (JSR 380) ---
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    // --- Spring Framework ---
    // BOM (Bill of Materials) for consistent Spring versions.
    implementation(platform("org.springframework:spring-framework-bom:6.1.10"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework:spring-tx")

    // --- Database Connectivity ---
    implementation("com.zaxxer:HikariCP:5.1.0")
    runtimeOnly("com.h2database:h2:2.3.232")

    // --- Logging ---
    implementation("org.slf4j:slf4j-api:2.0.12")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.12")

    // --- Lombok ---
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")

    // --- Google API (MODERN AND ALIGNED VERSIONS) ---
    // This modern set is compatible with the Spring 6 ecosystem, resolving all conflicts.
    implementation("com.google.apis:google-api-services-tasks:v1-rev20240630-2.0.0")
    implementation("com.google.api-client:google-api-client:2.4.0")
    implementation("com.google.api-client:google-api-client-jackson2:2.4.0")
    implementation("com.google.oauth-client:google-oauth-client-java6:1.36.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")

    implementation("com.google.http-client:google-http-client-jackson2:1.45.0")

    // --- Testing ---
    testImplementation("org.springframework:spring-test")
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

application {
    mainClass = "org.desviante.Main"
}

tasks.withType<Test> {
    useJUnitPlatform()
}