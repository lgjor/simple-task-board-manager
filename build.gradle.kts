plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
    id("org.hibernate.orm") version "7.0.6.Final"
}

group = "br.com.dio"
version = "1.0.9"

repositories {
    mavenCentral()
    google()
    maven { url = uri("https://maven.google.com") }
}

dependencies {
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-swing:21")
    implementation("org.liquibase:liquibase-core:4.26.0")

    implementation("info.picocli:picocli:4.6.1")
    implementation("mysql:mysql-connector-java:5.1.34")
    implementation("org.xerial:sqlite-jdbc:3.50.1.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    implementation("org.hibernate:hibernate-core:7.0.6.Final")
    implementation("org.hibernate.orm:hibernate-community-dialects:7.0.6.Final")

    // Google Tasks API
    //runtimeOnly("com.google.apis:google-api-services-tasks:v1-rev71-1.25.0") removido
    implementation("com.google.api-client:google-api-client:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20250518-2.0.0")
    implementation("com.google.http-client:google-http-client:1.47.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.13.3")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.13.3")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")

    // --- Dependências de Teste (Versão Corrigida e Unificada) ---

    // O JUnit BOM (Bill of Materials) gerencia as versões de todas as bibliotecas JUnit para garantir compatibilidade.
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    // Dependência principal para JUnit 5 (a versão é gerenciada pelo BOM)
    testImplementation("org.junit.jupiter:junit-jupiter")

    // Unifica a versão do TestFX para a versão estável mais recente
    testImplementation("org.testfx:testfx-core:4.0.17")
    testImplementation("org.testfx:testfx-junit5:4.0.17")

    // Mockito e Hamcrest
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
    testImplementation("org.hamcrest:hamcrest:2.2")

    // Necessário para a execução dos testes via linha de comando ou em alguns CIs
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Configura a tarefa de teste para usar a plataforma JUnit 5
tasks.withType<Test> {
    useJUnitPlatform()
    // Garante que os testes não falhem se nenhum teste for encontrado
    failFast = true
}

//tasks.withType<JavaCompile> {
//    options.compilerArgs.addAll(listOf(
//        "--patch-module", "org.desviante.board=${classpath.asPath}"
//    ))
//}

javafx {
    version = "21"
    modules = listOf("javafx.controls")
}

application {
    mainClass.set("org.desviante.Main")
   // mainModule.set("org.desviante.board")
}

sourceSets {
    test {
        java {
            setSrcDirs(listOf("src/test/java"))
        }
    }
}

jlink {


    options.set(listOf(
        "--strip-debug",
        "--compress", "2",
        "--no-header-files",
        "--no-man-pages"
    ))

    forceMerge("jackson-core")
    forceMerge("jackson-annotations")
    forceMerge("jackson-databind")
    forceMerge("snakeyaml")
    forceMerge("sqlite-jdbc")
    forceMerge("slf4j")
    forceMerge("picocli")
    forceMerge("google-http-client")
    forceMerge("google-auth-library")
    forceMerge("liquibase")
    forceMerge("grpc")

    mergedModule {
        additive = true
        uses("com.fasterxml.jackson.core.JsonFactory")
        uses("com.fasterxml.jackson.databind.Module")
        uses("java.sql.Driver")
        uses("com.google.auth.oauth2.OAuth2Credentials")
        uses("io.grpc.ManagedChannelProvider")
    }

    addExtraDependencies("ALL")

    imageZip = project.layout.buildDirectory.file("image-zip/image.zip").get().asFile

    launcher {
        name = "Simple Task Board Manager"
        jvmArgs = listOf("-Xmx2048m")
    }

    jpackage {
        skipInstaller = false
        installerType = "exe"
        installerName = "Simple Task Board Manager"
        installerOptions = listOf(
            "--win-dir-chooser",
            "--win-menu",
            "--win-shortcut",
            "--vendor", "AuDesviante",
            "--app-version", "1.0.9"
        )
        icon = file("src/main/resources/icon.ico").absolutePath
    }

//    tasks.register<JavaExec>("runApp") {
//        classpath = sourceSets["main"].runtimeClasspath
//        mainClass = "org.desviante.Main"
//    }
}

configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "com.fasterxml.jackson.core") {
            useVersion("2.13.3")
            because("Evitar substituição automática pelo jackson-bom:2.18.2")
        }
    }
}