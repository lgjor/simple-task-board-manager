plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.1.1"
}

group = "br.com.dio"
version = "1.0.8"

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
    implementation("org.openjfx:javafx-controls:21")
    implementation("org.openjfx:javafx-fxml:21")
    implementation("org.openjfx:javafx-swing:21")
    implementation("org.liquibase:liquibase-core:4.26.0")

    implementation("info.picocli:picocli:4.6.1")
    implementation("mysql:mysql-connector-java:5.1.34")
    implementation("org.xerial:sqlite-jdbc:3.50.1.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    // Google Tasks API
    implementation("com.google.api-client:google-api-client:2.8.0")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.39.0")
    implementation("com.google.apis:google-api-services-tasks:v1-rev20250518-2.0.0")
    implementation("com.google.http-client:google-http-client-jackson2:1.47.0")
    runtimeOnly("com.google.apis:google-api-services-tasks:v1-rev71-1.25.0")
    implementation("com.fasterxml.jackson.core:jackson-core:2.14")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

tasks.withType<Test> {
    javaLauncher.set(javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    })
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
    mainModule.set("org.desviante.board")
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

    forceMerge("jackson")
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
        uses("java.sql.Driver")
        uses("com.fasterxml.jackson.databind.Module")
        uses("com.google.auth.oauth2.OAuth2Credentials")
        uses("io.grpc.ManagedChannelProvider")
    }

    addExtraDependencies("sqlite-jdbc", "jackson-core", "jackson-annotations", "jackson-databind", "google-http-client-jackson2")

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
            "--app-version", "1.0.8"
        )
        icon = file("src/main/resources/icon.ico").absolutePath
    }

    tasks.register<JavaExec>("runApp") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass = "org.desviante.Main"
    }
}

tasks.test {
    useJUnitPlatform()
}