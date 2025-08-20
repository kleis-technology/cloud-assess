import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("maven-publish")
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.3"
    kotlin("jvm") version "1.8.22"
    kotlin("plugin.spring") version "1.8.22"
    kotlin("plugin.serialization") version "1.9.0"
    id("org.openapi.generator") version "7.2.0"
}

group = "org.cloud_assess"
version = "1.8.2"

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
    mavenCentral()
	maven {
		name = "github"
		url = uri("https://maven.pkg.github.com/kleis-technology/lcaac")
		credentials {
			username = System.getenv("GITHUB_ACTOR")
			password = System.getenv("GITHUB_TOKEN")
		}
	}
}

dependencies {
    val lcaacVersion = "1.8.0"
    implementation("ch.kleis.lcaac:core:$lcaacVersion")
    implementation("ch.kleis.lcaac:grammar:$lcaacVersion")


    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-json")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation("org.springframework.boot:spring-boot-starter-validation")
    testImplementation("org.springframework.boot:spring-boot-starter-validation")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.+")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.+")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    testImplementation("io.mockk:mockk:1.13.4")

    implementation("com.charleskorn.kaml:kaml:0.59.0")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = "17"
    }
    dependsOn.add("openApiGenerate")
}

tasks.withType<Test> {
    useJUnitPlatform  {
        excludeTags("Performance")
    }
}

sourceSets {
    main {
        kotlin {
            srcDir("$buildDir/generated/src/main/kotlin")
        }
    }
}

openApiGenerate {
    generatorName.set("kotlin-spring")
    additionalProperties = mapOf(
        "apiPackage" to "$group.api",
        "modelPackage" to "$group.dto",
        "groupId" to group,
        "artifactId" to "api",
        "interfaceOnly" to true,
        "documentationProvider" to "none",
        "useSpringBoot3" to true,
    )
    inputSpec.set("$rootDir/openapi/api.yaml")
    outputDir.set("$buildDir/generated")
    importMappings.set(mapOf(
        "EntryValueDto" to " org.cloud_assess.dto.EntryValueDto",
        "ParameterValueDto" to "org.cloud_assess.dto.ParameterValueDto",
    ))
}

openApiValidate {
    inputSpec.set("$rootDir/openapi/api.yaml")
    recommend.set(true)
}
