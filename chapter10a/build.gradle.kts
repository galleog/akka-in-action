plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))
    implementation(platform(libs.junit.bom))
    implementation(platform(libs.kotest.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-stream_$scalaVersion")
    implementation(libs.logback)

    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.pekko:pekko-actor-testkit-typed_$scalaVersion")
    testImplementation("io.kotest:kotest-assertions-core")
}

application {
    mainClass.set("com.github.galleog.pekko.chapter10a.MainKt")
}