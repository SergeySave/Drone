import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.71"
}

group = "com.sergeysav"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    jcenter()
    maven("https://oss.sonatype.org/content/groups/public")
}

dependencies {
    compile(project(":core"))
    compile("com.beust:klaxon:3.0.1")
    compile("com.pi4j:pi4j-core:1.2-SNAPSHOT")
    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

val jar: Jar by tasks
jar.apply {
    manifest {
        attributes(mapOf("Main-Class" to "com.sergeysav.drone.MainKt"))
    }
    
    from(configurations.compile.map { if (it.isDirectory()) it else zipTree(it) })
}