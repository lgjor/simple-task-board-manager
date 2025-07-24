// --- Helper para o JPackage (usando a API padrão do Java) ---
val osName = System.getProperty("os.name").lowercase()
val javafxOsClassifier = when {
    "windows" in osName -> "win"
    "mac" in osName -> "mac"
    "linux" in osName -> "linux"
    else -> throw GradleException("Unsupported OS for JavaFX: $osName")
}

// Definir a versão como uma constante simples no topo do script.
val javafxVersion = "21"

// Usando o bloco de plugins moderno e correto.
plugins {
    id("org.springframework.boot") version "3.2.5"
    id("io.spring.dependency-management") version "1.1.5"
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
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
    // Usa a constante simples definida acima.
    version = javafxVersion
    modules = listOf("javafx.controls", "javafx.fxml")
}

// A exclusão do commons-logging continua sendo uma boa prática.
configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
}

dependencies {
    // As dependências da aplicação principal não mudam.
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    runtimeOnly("com.h2database:h2:2.3.232")
    compileOnly("org.projectlombok:lombok:1.18.32")
    annotationProcessor("org.projectlombok:lombok:1.18.32")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20240630-2.0.0")
    implementation("com.google.api-client:google-api-client:2.4.0")
    implementation("com.google.api-client:google-api-client-jackson2:2.4.0")
    implementation("com.google.oauth-client:google-oauth-client-java6:1.36.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.36.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.45.0")

    // As dependências de teste não mudam.
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    // Usa a constante simples para garantir que o compilador não se confunda.
    testImplementation("org.openjfx:javafx-graphics:$javafxVersion:$javafxOsClassifier")
    testImplementation("org.openjfx:javafx-controls:$javafxVersion:$javafxOsClassifier")
    testImplementation("org.openjfx:javafx-fxml:$javafxVersion:$javafxOsClassifier")
}

application {
    // A classe de entrada para o usuário final continua a mesma.
    mainClass.set("org.desviante.SimpleTaskBoardManagerApplication")
}

// A configuração do bootJar continua a mesma, mas não será usada pelo jlink.
tasks.getByName<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = true
    archiveClassifier.set("app")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// A configuração padrão e correta para o plugin jlink.
jlink {
    // Opções para otimizar a imagem Java gerada.
    options.set(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"
    ))

    // Configuração crucial para aplicações com Spring e outras bibliotecas dinâmicas.
    mergedModule {
        additive = true
        // Jackson (usado pelo Google Client)
        uses("com.fasterxml.jackson.core.JsonFactory")
        uses("com.fasterxml.jackson.databind.Module")

        // Database Driver (H2)
        uses("java.sql.Driver")

        // Google Auth
        uses("com.google.auth.oauth2.CredentialsProvider")

        // Hibernate Validator
        uses("org.hibernate.integrator.spi.Integrator")
        uses("org.hibernate.service.spi.ServiceContributor")
        uses("org.hibernate.bytecode.spi.BytecodeProvider")
        uses("org.hibernate.dialect.Dialect")
    }

    // Configura o lançador (o .exe dentro da pasta de instalação).
    launcher {
        name = "SimpleTaskBoardManager" // Nome do executável
        jvmArgs = listOf(
            "-Xmx2048m",
            // Adiciona permissões de reflexão necessárias para o Jackson funcionar bem com o Google Client.
            "--add-opens=com.fasterxml.jackson.databind/com.fasterxml.jackson.databind=com.google.http.client.jackson2"
        )
    }

    // Bloco 'jpackage' único para configurar o instalador final.
    jpackage {
        // A linha 'mainJar' foi REMOVIDA.
        // O plugin agora usará a convenção do plugin 'application' para encontrar o JAR correto.

        // O nome do arquivo do instalador (ex: SimpleTaskBoardManager-1.0-SNAPSHOT.exe)
        installerName = "SimpleTaskBoardManager"

        // Opções específicas para o instalador do Windows.
        installerOptions = listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--vendor", "AuDesviante"
        )
        // Usa a versão do projeto dinamicamente.
        appVersion = project.version.toString()
        // Define o ícone da aplicação.
        icon = project.file("src/main/resources/icon.ico").absolutePath
    }
}