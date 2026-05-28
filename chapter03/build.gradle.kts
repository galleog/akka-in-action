plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation(libs.kotlin.coroutines)
    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation(libs.logback)
}