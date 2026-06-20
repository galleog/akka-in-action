plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))
    implementation(platform(libs.junit.bom))
    implementation(platform(libs.testcontainers.bom))

    implementation(project(":chapter09b"))
    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-persistence-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-persistence-jdbc_$scalaVersion")
    implementation("org.apache.pekko:pekko-serialization-jackson_$scalaVersion")
    implementation("org.apache.pekko:pekko-stream_$scalaVersion")
    implementation("org.apache.pekko:pekko-persistence-query_$scalaVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.pekko.persistence.jdbc)
    implementation(libs.logback)

    runtimeOnly(libs.postgres)

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_$scalaVersion")
    testImplementation("org.apache.pekko:pekko-persistence-testkit_$scalaVersion")
    testImplementation("org.apache.pekko:pekko-stream-testkit_$scalaVersion")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-postgresql")
}

application {
    mainClass.set("com.github.galleog.pekko.chapter10b.MainKt")
}