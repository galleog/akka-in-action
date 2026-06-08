plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))
    implementation(platform(libs.jackson.bom))
    implementation(platform(libs.junit.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-serialization-jackson_$scalaVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.logback)

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_$scalaVersion")
}