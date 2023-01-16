import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.0.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.21"
	kotlin("plugin.spring") version "1.7.21"
}

group = "app.birdsocial.birdapi"
version = "0.0.2" // TODO - Bump Version
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-graphql")
	implementation("org.springframework.boot:spring-boot-starter-web")

	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.springframework.boot:spring-boot-starter-data-neo4j:3.0.1")
//	implementation("org.neo4j.driver:neo4j-java-driver-spring-boot-starter:4.3.6.0")

//	implementation("org.neo4j:neo4j-ogm-embedded-driver:3.2.39")
//	implementation("org.neo4j.test:neo4j-harness:5.3.0")

//	implementation("io.github.cdimascio:dotenv-kotlin:6.4.0")

	implementation("com.bucket4j:bucket4j-core:8.1.1")

//	implementation("com.opencsv:opencsv:4.1")

	implementation("org.mindrot:jbcrypt:0.4")

	implementation("com.auth0:java-jwt:4.2.1")

	implementation("org.passay:passay:1.6.2")

	implementation("org.springframework.boot:spring-boot-starter-validation:3.0.1")

	implementation("io.sentry:sentry:6.11.0")
//	implementation("io.sentry:sentry-spring-boot-starter:6.11.0")
//	implementation("io.sentry:sentry-graphql:6.11.0")
//	implementation("io.sentry:sentry-logback:6.11.0")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework:spring-webflux")
	testImplementation("org.springframework.graphql:spring-graphql-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
