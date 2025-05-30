plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "ru.chousik.web"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-cache")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
//	implementation("org.springframework.boot:spring-boot-starter-data-redis")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	// https://mvnrepository.com/artifact/org.modelmapper/modelmapper
	implementation("org.modelmapper:modelmapper:3.1.1")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-eureka-client
//	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:3.1.4")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-config
	// https://mvnrepository.com/artifact/io.jsonwebtoken/jjwt-api
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:3.4.4")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-openfeign
	implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.2.0")
	// https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-oauth2-client
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client:3.4.4")
	// https://mvnrepository.com/artifact/software.amazon.awssdk/s3
	implementation("software.amazon.awssdk:s3:2.31.49")
	// https://mvnrepository.com/artifact/software.amazon.awssdk/apache-client
	implementation("software.amazon.awssdk:apache-client:2.31.49")
	implementation("ru.chousik.web:common:1.0-SNAPSHOT")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
