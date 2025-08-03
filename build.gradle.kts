import java.io.File

val platform = when {
    org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
    org.gradle.internal.os.OperatingSystem.current().isLinux -> "linux"
    org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "mac"
    else -> throw GradleException("Unsupported OS")
}

// --- Helper para o JPackage (usando a API padrão do Java) ---
val osName = System.getProperty("os.name").lowercase()
val javafxOsClassifier = when {
    "windows" in osName -> "win"
    "mac" in osName -> "mac"
    "linux" in osName -> "linux"
    else -> throw GradleException("Unsupported OS for JavaFX: $osName")
}

plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("java")
    id("application")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "org.desviante"
version = "1.0.3"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

// A exclusão do commons-logging continua sendo uma boa prática.
configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "commons-logging", module = "commons-logging")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "javax.servlet")
    exclude(group = "ch.qos.logback")
}

dependencies {

    val javafxVersion = "21.0.4"
    val javafxOsClassifier = when {
        org.gradle.internal.os.OperatingSystem.current().isWindows -> "win"
        org.gradle.internal.os.OperatingSystem.current().isLinux -> "linux"
        org.gradle.internal.os.OperatingSystem.current().isMacOsX -> "mac"
        else -> throw GradleException("Unsupported OS for JavaFX")
    }

    //micrometer e blockhound
    implementation(platform("io.micrometer:micrometer-bom:1.15.2"))
    implementation("io.micrometer:micrometer-observation")
    implementation("io.micrometer:context-propagation:1.1.3") // substitui micrometer-context
    implementation("io.projectreactor.tools:blockhound:1.0.9.RELEASE") // versão compatível
    implementation("com.fasterxml:classmate:1.5.1")

    // logger
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Dependências da aplicação principal
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.jboss.logging:jboss-logging:3.5.3.Final")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.glassfish:jakarta.el:4.0.2")
    implementation("org.springframework.boot:spring-boot-starter-jdbc"){
        exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
        exclude(group = "org.apache.logging.log4j")
    }
    implementation("org.slf4j:slf4j-simple:2.0.13")
    runtimeOnly("com.h2database:h2:2.3.232")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20240630-2.0.0")
    implementation("com.google.api-client:google-api-client:2.2.0")
    implementation("com.google.api-client:google-api-client-jackson2:2.2.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.19.0")
    implementation("com.google.auth:google-auth-library-credentials:1.19.0")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.42.3")

    // Dependências de teste
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Adiciona as dependências JavaFX (necessárias para o jlink)
    implementation("org.openjfx:javafx-base:$javafxVersion:$javafxOsClassifier")
    implementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxOsClassifier")
    implementation("org.openjfx:javafx-controls:$javafxVersion:$javafxOsClassifier")
    implementation("org.openjfx:javafx-fxml:$javafxVersion:$javafxOsClassifier")

    // Adiciona dependências Jackson básicas
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.0")

    // Usa a constante simples para garantir que o compilador não se confunda.
    testImplementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxOsClassifier")
    testImplementation("org.openjfx:javafx-controls:$javafxVersion:$javafxOsClassifier")
    testImplementation("org.openjfx:javafx-fxml:$javafxVersion:$javafxOsClassifier")
}

application {
    // A classe de entrada para o usuário final continua a mesma.
    mainClass.set("org.desviante.SimpleTaskBoardManagerApplication")
    applicationDefaultJvmArgs = listOf(
        "--add-opens=com.fasterxml.jackson.databind/com.fasterxml.jackson.databind=com.google.http.client.jackson2"
    )
}

// Configuração do Spring Boot
springBoot {
    buildInfo()
}



configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.13.3")
            because("Evitar substituição automática pelo jackson-bom da API do Google")
        }
    }
}

tasks.shadowJar {
    mergeServiceFiles()
    archiveClassifier.set("all")
}

tasks.named<JavaCompile>("compileJava") {
    options.compilerArgs.addAll(listOf("--module-path", project.configurations.compileClasspath.get().asPath))
}

// A configuração do bootJar continua a mesma, mas não será usada pelo jlink.
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("app")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Configuração para jpackage (instalador Windows)
tasks.register<Exec>("jpackage") {
    dependsOn("shadowJar")
    
    val shadowJar = tasks.shadowJar.get().archiveFile.get().asFile
    val appName = "SimpleTaskBoardManager"
    val iconFile = file("src/main/resources/icon.ico")
    
    commandLine(
        "jpackage",
        "--input", shadowJar.parent,
        "--name", appName,
        "--main-jar", shadowJar.name,
        "--main-class", "org.desviante.SimpleTaskBoardManagerApplication",
        "--type", "exe",
        "--dest", "${layout.buildDirectory.get()}/dist",
        "--java-options", "-Xmx2048m",
        "--win-dir-chooser",
        "--win-menu",
        "--win-shortcut",
        "--vendor", "AuDesviante",
        "--app-version", "1.0.3",
        "--icon", iconFile.absolutePath
    )
    
    doFirst {
        // Cria o diretório de destino se não existir
        file("${layout.buildDirectory.get()}/dist").mkdirs()
        
        // Verifica se o ícone existe
        if (!iconFile.exists()) {
            throw GradleException("Ícone não encontrado: ${iconFile.absolutePath}")
        }
    }
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    options.charSet = "UTF-8"
    options.docEncoding = "UTF-8"
    
    // Opcional: incluir links para APIs externas
    options.links("https://docs.oracle.com/en/java/javase/11/docs/api/")
    options.links("https://docs.spring.io/spring-framework/docs/current/javadoc-api/")
}