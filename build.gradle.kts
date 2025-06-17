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
    maven { url = uri("https://maven.google.com") }
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1") {
        exclude(group = "javax.xml.bind", module = "jaxb-api")
    }
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("com.google.api-client:google-api-client:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client:1.34.1")
    implementation("com.google.apis:google-api-services-calendar:v3-rev20250404-2.0.0")
    implementation("com.google.oauth-client:google-oauth-client-java6:1.34.1")
    implementation("com.google.oauth-client:google-oauth-client-jetty:1.34.1")
    implementation("com.google.http-client:google-http-client-jackson2:1.43.3")
    implementation("com.fasterxml.jackson.core:jackson-core:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.17.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.openjfx:javafx-swing:21")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}


javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("org.desviante.Main")
    mainModule.set("org.desviante")
}

jlink {
    forceMerge("jackson", "snakeyaml")

    imageZip = project.layout.buildDirectory.file("image-zip/image.zip").get().asFile

    mergedModule {
        additive = true
        uses("java.sql.Driver")
    }

    addExtraDependencies("sqlite-jdbc", "jackson-core", "jackson-annotations", "jackson-databind","google-http-client-jackson2")

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages", "--bind-services"))
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
            "--app-version", "1.0.7"
        )
        icon = file("src/main/resources/icon.ico").absolutePath
    }

    tasks.register<JavaExec>("runApp") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass = "org.desviante.Main"
    }
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
