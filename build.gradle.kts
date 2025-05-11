plugins {
    id("java")
    id("application")
    id("org.openjfx.javafxplugin") version "0.0.14"
    id("org.beryx.jlink") version "2.26.0"
}

group = "br.com.dio"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.liquibase:liquibase-core:4.29.1") {
        exclude(group = "javax.xml.bind", module = "jaxb-api")
    }
    implementation("mysql:mysql-connector-java:8.0.33")
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
}

javafx {
    version = "21"
    modules("javafx.controls", "javafx.fxml")
}

application {
    mainClass.set("br.com.dio.Main")
    mainModule.set("br.com.dio")
}

jlink {
    forceMerge("jackson", "snakeyaml")

    imageZip = project.layout.buildDirectory.file("image-zip/image.zip").get().asFile

    mergedModule {
        additive = true
        uses("java.sql.Driver")
    }

    options.set(listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages"))
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
            "--vendor", "DIO",
            "--app-version", "1.0.0"
        )
    }
}
tasks.withType<JavaCompile> {
    options.compilerArgs.add("-Xlint:unchecked")
}