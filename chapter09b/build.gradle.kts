plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))
    implementation(platform(libs.jackson.bom))
    implementation(platform(libs.junit.bom))
    implementation(platform(libs.kotest.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-serialization-jackson_$scalaVersion")
    implementation("org.apache.pekko:pekko-persistence-typed_$scalaVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.logback)

    runtimeOnly(libs.pekko.persistence.r2dbc)
    runtimeOnly(libs.postgres.r2dbc)

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_$scalaVersion")
    testImplementation("org.apache.pekko:pekko-persistence-testkit_$scalaVersion")
    testImplementation("io.kotest:kotest-assertions-core")
}

application {
    mainClass.set("com.github.galleog.pekko.chapter09b.ContainerApp")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
    standardOutput = System.out
}