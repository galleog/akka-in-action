plugins {
    id("com.github.galleog.pekko.gradle.conventions")
}

dependencies {
    implementation(platform(libs.pekko.bom))
    implementation(platform(libs.junit.bom))
    implementation(platform(libs.kotest.bom))

    implementation("org.apache.pekko:pekko-actor-typed_2.13")
    implementation(libs.logback)

    testImplementation("org.junit.vintage:junit-vintage-engine")
    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_2.13")
    testImplementation("io.kotest:kotest-assertions-core")
}