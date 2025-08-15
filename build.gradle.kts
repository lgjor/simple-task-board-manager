import java.io.File

// Constante centralizada para a versão da aplicação
val appVersion = "1.1.4"

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
    id("io.freefair.lombok") version "8.4"
}

group = "org.desviante"
version = appVersion

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
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    // Liquibase para migrações de banco (comentado temporariamente devido a conflitos)
    // implementation("org.springframework.boot:spring-boot-starter-liquibase"){
    //     exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    //     exclude(group = "org.apache.logging.log4j")
    // }
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

// Configuração de testes cross-platform
tasks.withType<Test> {
    useJUnitPlatform()
    
    // Configurações específicas para Linux para evitar problemas de processo
    if (org.gradle.internal.os.OperatingSystem.current().isLinux) {
        maxParallelForks = 1
        forkEvery = 1
        systemProperty("java.awt.headless", "true")
        systemProperty("testfx.robot", "awt")
        systemProperty("testfx.headless", "true")
        systemProperty("prism.order", "sw")
        systemProperty("prism.text", "t2k")
    }
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
        "--win-per-user-install",
        "--win-upgrade-uuid", "550e8400-e29b-41d4-a716-446655440000",
        "--win-help-url", "https://github.com/desviante/simple-task-board-manager",
        "--win-update-url", "https://github.com/desviante/simple-task-board-manager/releases",
        "--vendor", "AuDesviante",
        "--app-version", appVersion,
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

// Configuração para jpackage Linux (AppImage)
tasks.register<Exec>("jpackageLinux") {
    dependsOn("shadowJar")
    
    val shadowJar = tasks.shadowJar.get().archiveFile.get().asFile
    val appName = "SimpleTaskBoardManager"
    val iconFile = file("src/main/resources/icon.png")
    
    commandLine(
        "jpackage",
        "--input", shadowJar.parent,
        "--name", appName,
        "--main-jar", shadowJar.name,
        "--main-class", "org.desviante.SimpleTaskBoardManagerApplication",
        "--type", "app-image",
        "--dest", "${layout.buildDirectory.get()}/dist",
        "--java-options", "-Xmx2048m",
        "--vendor", "AuDesviante",
        "--app-version", appVersion,
        "--icon", iconFile.absolutePath
    )
    
    doFirst {
        // Cria o diretório de destino se não existir
        file("${layout.buildDirectory.get()}/dist").mkdirs()
        
        // Verifica se o ícone existe
        if (!iconFile.exists()) {
            logger.warn("Ícone PNG não encontrado, usando ícone padrão")
        }
    }
}

// Configuração para jpackage Linux (DEB package)
tasks.register<Exec>("jpackageLinuxDeb") {
    dependsOn("shadowJar")
    
    val shadowJar = tasks.shadowJar.get().archiveFile.get().asFile
    val appName = "simple-task-board-manager"
    val iconFile = file("src/main/resources/icon.png")
    
    commandLine(
        "jpackage",
        "--input", shadowJar.parent,
        "--name", appName,
        "--main-jar", shadowJar.name,
        "--main-class", "org.desviante.SimpleTaskBoardManagerApplication",
        "--type", "deb",
        "--dest", "${layout.buildDirectory.get()}/dist",
        "--java-options", "-Xmx2048m",
        "--vendor", "AuDesviante",
        "--app-version", appVersion,
        "--icon", iconFile.absolutePath,
        "--linux-app-category", "Office",
        "--linux-menu-group", "Office",
        "--linux-shortcut",
        "--linux-deb-maintainer", "lucas@desviante.org",
        "--linux-package-name", "simple-task-board-manager",
        "--linux-package-deps", "openjfx"
    )
    
    doFirst {
        // Cria o diretório de destino se não existir
        file("${layout.buildDirectory.get()}/dist").mkdirs()
        
        // Verifica se o ícone existe
        if (!iconFile.exists()) {
            logger.warn("Ícone PNG não encontrado, usando ícone padrão")
        }
    }
}

// Configuração para jpackage Linux (RPM package)
tasks.register<Exec>("jpackageLinuxRpm") {
    dependsOn("shadowJar")
    
    val shadowJar = tasks.shadowJar.get().archiveFile.get().asFile
    val appName = "simple-task-board-manager"
    val iconFile = file("src/main/resources/icon.png")
    
    commandLine(
        "jpackage",
        "--input", shadowJar.parent,
        "--name", appName,
        "--main-jar", shadowJar.name,
        "--main-class", "org.desviante.SimpleTaskBoardManagerApplication",
        "--type", "rpm",
        "--dest", "${layout.buildDirectory.get()}/dist",
        "--java-options", "-Xmx2048m",
        "--vendor", "AuDesviante",
        "--app-version", appVersion,
        "--icon", iconFile.absolutePath,
        "--linux-app-category", "Office",
        "--linux-menu-group", "Office",
        "--linux-shortcut",
        "--linux-package-name", "simple-task-board-manager"
    )
    
    doFirst {
        // Cria o diretório de destino se não existir
        file("${layout.buildDirectory.get()}/dist").mkdirs()
        
        // Verifica se o ícone existe
        if (!iconFile.exists()) {
            logger.warn("Ícone PNG não encontrado, usando ícone padrão")
        }
    }
}

// Task para gerar AppImage usando appimagetool (comentado temporariamente)
// tasks.register<Exec>("createAppImage") {
//     // Implementação futura
// }

// Task para gerar Snap package (comentado temporariamente)
// tasks.register<Exec>("createSnap") {
//     // Implementação futura
// }

// Configuração do Javadoc com compatibilidade cross-platform
tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    
    // Configuração básica que funciona em ambos os sistemas operacionais
    // As configurações avançadas são opcionais e podem ser adicionadas manualmente se necessário
}

// Task específica para Linux que pula os testes problemáticos
tasks.register("buildLinux") {
    dependsOn("shadowJar")
    description = "Build para Linux sem executar testes"
}

// Task específica para Windows
tasks.register("buildWindows") {
    dependsOn("build")
    description = "Build completo para Windows"
}