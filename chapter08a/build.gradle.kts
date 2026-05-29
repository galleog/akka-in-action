plugins {
    id("com.github.galleog.pekko.gradle.kotlin")
    application
}

val scalaVersion: String by project

dependencies {
    implementation(platform(libs.pekko.bom))

    implementation("org.apache.pekko:pekko-actor-typed_$scalaVersion")
    implementation("org.apache.pekko:pekko-cluster-typed_$scalaVersion")
    implementation(libs.pekko.management)
    implementation(libs.pekko.management.cluster.http)
    implementation(libs.logback)
}

application {
    mainClass.set("com.github.galleog.pekko.chapter08a.ClusterDomainEventListenerKt")
}

tasks.withType<JavaExec> {
    val propertiesToPass = listOf("ARTERY_PORT", "MANAGEMENT_PORT")
    propertiesToPass.forEach { propName ->
        System.getProperty(propName)?.let { propValue -> systemProperty(propName, propValue) }
    }
}