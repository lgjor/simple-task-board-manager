/**
 * Este é um script de build Gradle limpo e moderno, usando as melhores práticas.
 * Ele declara todos os plugins no bloco `plugins {}`, que é a abordagem recomendada.
 * As configurações são aplicadas diretamente nos blocos de extensão (`application`, `javafx`, `jlink`),
 * que é a forma padrão e mais legível.
 *
 * Esta versão "padrão ouro" serve como um teste definitivo. Se ela falhar com um erro
 * de "Unresolved reference", o problema reside fora deste código (provavelmente em um
 * cache do Gradle corrompido ou uma incompatibilidade de versão).
 */
plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
}

group = "org.desviante"
version = "2"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // JavaFX
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-swing:21")

    // Database & Persistence
    implementation("org.liquibase:liquibase-core:4.26.0")
    implementation("org.xerial:sqlite-jdbc:3.50.1.0")
    implementation("org.hibernate:hibernate-core:7.0.6.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:7.0.6.Final")

    // Dependências opcionais do Hibernate, declaradas diretamente para maior clareza e robustez.
    implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.2")
    implementation("jakarta.transaction:jakarta.transaction-api:2.0.1")
    implementation("jakarta.annotation:jakarta.annotation-api:2.1.1")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api:4.1.0") // Fornece o módulo 'jakarta.cdi' exigido pelo Hibernate

    // Logging
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Google APIs & HTTP
    implementation("com.google.api-client:google-api-client:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20250518-2.0.0")
    implementation("com.google.http-client:google-http-client:1.47.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")

    // Jackson JSON
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")

    // Utilities
    implementation("info.picocli:picocli:4.6.1")
    implementation("org.checkerframework:checker-qual:3.42.0")

    // Compile-time only
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testfx:testfx-core:4.0.17")
    testImplementation("org.testfx:testfx-junit5:4.0.17")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    // Dependência do H2, apenas para o escopo de teste, para a nova estratégia de diagnóstico.
    testImplementation("com.h2database:h2:2.2.224")
}

// --- Dependency Resolution Strategy ---

// Usar 'configureEach' em vez de 'all' é a prática moderna e recomendada pelo Gradle.
// 'configureEach' é "lazy" e aplica a configuração de forma segura, apenas quando
// cada configuração é resolvida. Este bloco foi movido para ANTES da configuração
// das tarefas para garantir que a estratégia de resolução seja definida antes que
// qualquer configuração (como 'hibernateModules') seja resolvida prematuramente.
configurations.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.13.3")
            because("Evitar substituição automática pelo jackson-bom da API do Google")
        }
    }
}

// --- Task Configurations ---

tasks.withType<Test> {
    useJUnitPlatform()
    failFast = true

    // NOVA ESTRATÉGIA: RADICAL SIMPLIFICATION
    // O erro mudou de 'IllegalAccessError' para 'HibernateException', o que significa que o 'persistence.xml' de teste resolveu o problema de reflexão.
    // As complexas flags 'jvmArgs' agora são a causa mais provável do novo erro.
    // Vamos remover TODA a configuração customizada e confiar na construção de module-path padrão do Gradle.
    // Se um erro ocorrer, ele será limpo e nos dirá exatamente o que está faltando.
}


// Configura explicitamente a tarefa 'run', criada pelo plugin 'application'.
// Esta abordagem é mais robusta do que usar 'applicationDefaultJvmArgs' no bloco 'application',
// pois evita conflitos com outros plugins (como JavaFX e JLink) que também modificam esta tarefa.
tasks.named<JavaExec>("run") {
    // Obtém o source set principal para acessar seu classpath de tempo de execução.
    // Espelha a configuração da tarefa de teste para consistência e robustez.
    // Removemos os jvmArgs daqui também para manter a consistência com a tarefa de teste.
    // Os plugins Application e JavaFX gerenciarão os argumentos necessários
    // para a execução da aplicação com base nas dependências e no module-info.
}

// --- Plugin Configurations ---

javafx {
    version = "21"
    
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.swing")
    
}

application {
    mainClass.set("org.desviante.Main")
    mainModule.set("org.desviante")
}

jlink {
    options.set(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"))

    addExtraDependencies("info.picocli")
    addExtraDependencies("jakarta.cdi")
    addExtraDependencies("jakarta.annotation")

    forceMerge("jackson-core")
    forceMerge("jackson-annotations")
    forceMerge("jackson-databind")
    forceMerge("snakeyaml")
    forceMerge("google-http-client")
    forceMerge("google-auth-library")
    forceMerge("liquibase")
    forceMerge("grpc")

    mergedModule {
        additive = true
        uses("com.fasterxml.jackson.core.JsonFactory")
        uses("com.fasterxml.jackson.databind.Module")
        uses("java.sql.Driver")
        uses("com.google.auth.oauth2.CredentialsProvider")
        uses("io.grpc.ManagedChannelProvider")
        uses("liquibase.servicelocator.PrioritizedService")
        uses("org.hibernate.integrator.spi.Integrator")
    }

    addExtraDependencies("ALL")
    imageZip = project.layout.buildDirectory.file("image-zip/image.zip").get().asFile

    launcher {
        name = "Simple Task Board Manager"
        jvmArgs = listOf("-Xmx2048m") }

    jpackage {
        skipInstaller = false
        installerType = "exe"
        installerName = "Simple Task Board Manager"
        installerOptions = listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--vendor", "AuDesviante",
            "--app-version", "2")
        icon = project.file("src/main/resources/icon.ico").absolutePath
    }
}
