import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	id("net.fabricmc.fabric-loom")
	`maven-publish`
	id("org.jetbrains.kotlin.jvm") version "2.4.0"
}

version = providers.gradleProperty("mod_version").get()
group = providers.gradleProperty("maven_group").get()

repositories {}

dependencies {
	minecraft("com.mojang:minecraft:${providers.gradleProperty("minecraft_version").get()}")
	implementation("net.fabricmc:fabric-loader:${providers.gradleProperty("loader_version").get()}")

	implementation("net.fabricmc.fabric-api:fabric-api:${providers.gradleProperty("fabric_api_version").get()}")
    implementation("net.fabricmc:fabric-language-kotlin:${providers.gradleProperty("fabric_kotlin_version").get()}")
}

tasks.processResources {
	val version = version
	inputs.property("version", version)

	filesMatching("fabric.mod.json") {
		expand("version" to version)
	}
}

tasks.withType<JavaCompile>().configureEach {
	options.release = 25
}

kotlin {
	compilerOptions {
		jvmTarget = JvmTarget.JVM_25
	}
}

java {
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

tasks.jar {
	val projectName = project.name
	inputs.property("projectName", projectName)

	from("LICENSE") {
		rename { "${it}_$projectName" }
	}
}

publishing {
	publications {
		register<MavenPublication>("mavenJava") {
			from(components["java"])
		}
	}

	repositories {}
}
