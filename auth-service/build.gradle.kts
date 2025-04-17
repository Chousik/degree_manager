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
configurations.all {
	resolutionStrategy {
		force("com.thoughtworks.xstream:xstream:1.4.21")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-eureka-client
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("com.thoughtworks.xstream:xstream:1.4.21")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-config
	// https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
	implementation("com.auth0:java-jwt:4.4.0")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
