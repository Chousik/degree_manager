plugins {
	java
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
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
extra["springCloudVersion"] = "2024.0.1"
configurations.all {
	resolutionStrategy {
		force("com.thoughtworks.xstream:xstream:1.4.21")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security:")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-mail")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
	compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
//	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("com.thoughtworks.xstream:xstream:1.4.21")
	implementation("dev.samstevens.totp:totp:1.7.1")
	// https://mvnrepository.com/artifact/org.springframework.cloud/spring-cloud-starter-config
	// https://mvnrepository.com/artifact/com.nimbusds/nimbus-jose-jwt
	// https://mvnrepository.com/artifact/org.springframework.security/spring-security-oauth2-authorization-server
	implementation("org.springframework.security:spring-security-oauth2-authorization-server:1.4.2")
	// https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
	// https://mvnrepository.com/artifact/org.springdoc/springdoc-openapi-starter-webmvc-ui
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
}

dependencyManagement {
	imports {
		mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

jacoco {
	toolVersion = "0.8.12"
}

tasks.test {
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	classDirectories.setFrom(
		files(classDirectories.files.map {
			fileTree(it) {
				exclude(
					"**/config/**",
					"**/dto/**",
					"**/entity/**",
					"**/entities/**",
					"**/exception/**",
					"**/exceptions/**",
					"**/repository/**",
					"**/security/**",
					"**/*Application.*"
				)
			}
		})
	)
}

tasks.jacocoTestCoverageVerification {
	dependsOn(tasks.test)
	classDirectories.setFrom(tasks.jacocoTestReport.get().classDirectories)
	violationRules {
		rule {
			limit {
				minimum = "0.50".toBigDecimal()
			}
		}
	}
}

tasks.check {
	dependsOn(tasks.jacocoTestCoverageVerification)
}
