plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding-typed_$scalaVersion")
    implementation(libs.logback)
}

application {
    mainClass.set("com.github.galleog.pekko.chapter10c.LoggerShardedApp")
}