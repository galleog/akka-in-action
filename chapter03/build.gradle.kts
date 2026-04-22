plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
}

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation(libs.kotlin.coroutines)
    implementation("org.apache.pekko:pekko-actor-typed_2.13")
    implementation(libs.logback)
}