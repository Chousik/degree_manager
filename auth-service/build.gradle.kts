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
	implementation("org.springframework.boot:spring-boot-starter-security:3.4.4")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web:3.4.4")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	testImplementation ("org.junit.jupiter:junit-jupiter-api:5.8.2")
	testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine:5.8.2")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-netflix-eureka-client
	//	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client:4.2.1")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("com.thoughtworks.xstream:xstream:1.4.21")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-config
	// https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
	// https://mvnrepository.com/artifact/org.springframework.security/spring-security-oauth2-authorization-server
	implementation("org.springframework.security:spring-security-oauth2-authorization-server:1.4.2")
	// https://mvnrepository.com/artifact/com.amazonaws/aws-java-sdk-s3
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.780")
	// https://mvnrepository.com/artifact/software.amazon.awssdk/apache-client
	implementation("software.amazon.awssdk:apache-client:2.31.49")
	// https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
	// https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
