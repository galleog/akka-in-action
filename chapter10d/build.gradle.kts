plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation(project(":chapter09b"))
    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-sharding-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-persistence-typed_$scalaVersion")
    implementation(libs.pekko.projection.r2dbc)
    implementation(libs.pekko.projection.eventsourced)
    implementation(libs.logback)

    runtimeOnly(libs.postgres.r2dbc)
}

application {
    mainClass.set("com.github.galleog.pekko.chapter10d.ProjectionApp")
}