plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation(libs.logback)

    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_$scalaVersion")
    testImplementation(libs.mockk)
}