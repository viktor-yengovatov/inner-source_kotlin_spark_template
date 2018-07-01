import kotlin.collections.listOf
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import java.io.FileInputStream

val kotlinVersion = "1.2.41"
val junitVersion = "5.2.0"
val jooqVersion = "3.11.0"

allprojects {
    loadProperties(".env", ext) // load local.properties into ext
}

plugins {
    application
    kotlin("jvm") version "1.2.41"
//    id("com.github.rengelman.shadow").version("2.0.4")
    id("com.github.johnrengelman.shadow").version("2.0.4")
}

dependencies {
    compile(kotlin("stdlib", kotlinVersion))
    compile(kotlin("stdlib-jdk8", kotlinVersion))
    compile("org.jetbrains.kotlinx", "kotlinx-coroutines-core", "0.22.5")

    compile(project(":dal"))

    compile(group = "io.github.cdimascio", name = "java-dotenv", version = "3.1.1")
    compile(group = "com.sparkjava", name = "spark-core", version = "2.7.2")
    compile(group = "com.squareup.retrofit2", name = "retrofit", version = "2.4.0")
    compile(group = "com.squareup.retrofit2", name = "converter-jackson", version = "2.4.0")
    compile(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = "2.9.5")
    compile(group = "com.fasterxml.jackson.module", name = "jackson-module-parameter-names", version = "2.9.5")
    compile(group = "org.slf4j", name = "slf4j-simple", version = "1.7.25")
    compile(group = "org.jooq", name = "jooq", version = jooqVersion)
    compile(group = "org.postgresql", name = "postgresql", version = "42.2.2")

    testCompile(group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = kotlinVersion)
    testImplementation(group = "org.junit", name = "junit-bom", version = junitVersion)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api")
    testRuntimeOnly(group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testCompile(group = "org.mockito", name = "mockito-junit-jupiter", version = "2.18.3")
}

application {
    group = "lv.ctco.spark"
    version = "1.0-SNAPSHOT"
    applicationName = "spark-web-example"
    mainClassName = "com.home.ApplicationKt"
    applicationDefaultJvmArgs = listOf("-Xmx10m")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.javaParameters = true
    kotlinOptions.jvmTarget = "1.8"
}

val shadowJar: ShadowJar by tasks
shadowJar.archiveName = "app.jar"


fun loadProperties(path: String, pr: ExtraPropertiesExtension) = loadProperties(file(path), pr)

fun loadProperties(file: File, pr: ExtraPropertiesExtension) {
    Properties().apply {
        load(FileInputStream(file))
        forEach { (k, v) ->
            pr["$k"] = v
        }
    }
}
