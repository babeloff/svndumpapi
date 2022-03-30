import ca.coglinc.gradle.plugins.javacc.CompileJavaccTask

plugins {
    `java-library`
    id("ca.coglinc.javacc") version "2.4.0"
    `jacoco`
    `svndump-sample-dump`
}

repositories {
    mavenCentral()
}

tasks {
    compileJavacc {
        inputDirectory = layout.projectDirectory.dir("src/main/javacc").asFile
        outputDirectory = layout.buildDirectory.dir("generated/javacc").get().asFile
    }
    jjdoc {
        inputDirectory = layout.projectDirectory.dir("src/main/javacc").asFile
        outputDirectory = layout.buildDirectory.dir("generated/jjdoc").get().asFile
        arguments = mapOf("text" to "true")
    }
    compileJava {
        dependsOn(compileJavacc)
    }
    compileTestJava {
        dependsOn(compileJavacc)
    }
    jacocoTestReport {
        reports {
            xml.required.set(false)
            csv.required.set(false)
            html.outputLocation.set(layout.buildDirectory.dir("jacocoHtml"))
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(11))
    }
}

sourceSets {
    named("main") {
        java {
            srcDirs += tasks.named<CompileJavaccTask>("compileJavacc").get().outputDirectory
        }
    }
}

jacoco {
    toolVersion = "0.8.7"
    reportsDirectory.set(layout.buildDirectory.dir("customJacocoReportDir"))
}

version = "0.3.0"

dependencies {
    // https://mvnrepository.com/artifact/org.javatuples/javatuples
    implementation("org.javatuples:javatuples:1.2")

// https://mvnrepository.com/artifact/junit/junit
    testImplementation("junit:junit:4.13.2")
// https://mvnrepository.com/artifact/junit/junit-dep
    testImplementation("junit:junit-dep:4.11")
// https://mvnrepository.com/artifact/org.hamcrest/hamcrest-all
    testImplementation("org.hamcrest:hamcrest-all:1.3")
// https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305
    implementation("com.google.code.findbugs:jsr305:3.0.2")
// https://mvnrepository.com/artifact/org.jmock/jmock-junit4
    testImplementation("org.jmock:jmock-junit4:2.12.0")
// https://mvnrepository.com/artifact/com.google.guava/guava
    testImplementation("com.google.guava:guava:30.1.1-jre")
}