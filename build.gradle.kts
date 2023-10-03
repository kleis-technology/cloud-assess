import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("maven-publish")
	id("org.springframework.boot") version "3.1.3"
	id("io.spring.dependency-management") version "1.1.3"
	kotlin("jvm") version "1.8.22"
	kotlin("plugin.spring") version "1.8.22"
	kotlin("plugin.serialization") version "1.9.0"
}

group = "org.cloud_assess"
version = "1.0"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenLocal() // TODO: Remove once ch.kleis.lcaac.{core, grammar} are publicly available.
	mavenCentral()
	maven {
		name = "github"
		url = uri("https://maven.pkg.github.com/kleis-technology/lca-plugin")
		credentials {
			username = System.getenv("GITHUB_ACTOR")
			password = System.getenv("GITHUB_TOKEN")
		}
	}

}

dependencies {
	val lcaacVersion = "0.0.7-alpha"
	implementation("ch.kleis.lcaac:core:$lcaacVersion")
	implementation("ch.kleis.lcaac:grammar:$lcaacVersion")


	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-json")
	testImplementation("org.springframework.boot:spring-boot-starter-test")

	implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
	testImplementation("io.mockk:mockk:1.13.4")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs += "-Xjsr305=strict"
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

