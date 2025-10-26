plugins {
  kotlin("jvm") version "2.1.21"
  kotlin("plugin.serialization") version "2.1.21"
  application
  id("com.gradleup.shadow") version "9.2.2"
  id("com.google.devtools.ksp") version "2.1.21-2.0.2"
  id("dev.detekt") version ("2.0.0-alpha.0")
}

group = "com.ereyesalvarez"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  gradlePluginPortal()

}

dependencies {
  implementation(platform(libs.http4.bom))
  implementation("org.http4k:http4k-core")
  implementation("org.http4k:http4k-format-kotlinx-serialization")

  implementation(platform(libs.micrometer.bom))
  implementation("io.micrometer:micrometer-registry-prometheus")

  implementation("io.jsonwebtoken:jjwt-api:0.13.0")
  runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
  runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
  //   implementation("com.auth0:java-jwt:4.4.0")

  implementation("org.jdbi:jdbi3-core:3.45.1")

  implementation("de.mkammerer:argon2-jvm:2.11")

  implementation(libs.holpite.core)
  implementation(libs.zaxxer.hikari)
  implementation(libs.postgresql.driver)
  implementation(libs.sentry.sentry)

  implementation(libs.slf4j.api)
  implementation(libs.logback.classic)

  testImplementation(kotlin("test"))
  testImplementation(libs.testcontainer.postgres)
  testImplementation("org.mockito:mockito-core:5.14.2")
  testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
  testImplementation("com.squareup.okhttp3:okhttp:4.12.0")
  testImplementation("com.willowtreeapps.assertk:assertk-jvm:0.28.0")
}

application {
  mainClass = "target.MainKt"
}


tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

detekt {
  toolVersion = "2.0.0-alpha.0"
  buildUponDefaultConfig = true
}