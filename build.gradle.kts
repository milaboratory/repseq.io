import com.palantir.gradle.gitversion.VersionDetails
import groovy.lang.Closure

plugins {
    `java-library`
    `maven-publish`
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.palantir.git-version") version "0.12.3"
}


val miRepoAccessKeyId: String by project
val miRepoSecretAccessKey: String by project

val versionDetails: Closure<VersionDetails> by extra
val gitDetails = versionDetails()

val longTests: String? by project

group = "com.milaboratory"
version =
    if (gitDetails.commitDistance == 0) gitDetails.lastTag
    else "${gitDetails.lastTag}-${gitDetails.commitDistance}-${gitDetails.gitHash}"
description = "MiLib"

tasks.register("createInfoFile") {
    doLast {
        projectDir
            .resolve("build_info.json")
            .writeText("""{"version":"$version"}""")
    }
}

repositories {
    mavenCentral()

    maven {
        url = uri("https://pub.maven.milaboratory.com")
    }
}

val milibVersion = "1.13.1-8-ee94440152"

dependencies {
    api("com.milaboratory:milib:$milibVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.4")
    implementation("org.reflections:reflections:0.9.10")
    implementation("commons-io:commons-io:2.7")
    implementation("org.apache.httpcomponents:httpclient:4.5.10")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("com.beust:jcommander:1.72")
    implementation("net.sf.trove4j:trove4j:3.0.3")

    testImplementation("junit:junit:4.13.1")
    api(testFixtures("com.milaboratory:milib:$milibVersion"))
    testImplementation("org.mockito:mockito-all:1.9.5")
}

group = "io.repseq"
version = "1.3.5-SNAPSHOT"
description = "RepSeqIO"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val buildLibrary by tasks.registering(JavaExec::class) {
    main = "io.repseq.maven.CompileLibraryGradleStage"
    classpath = sourceSets["main"].runtimeClasspath
    args(project.rootDir)
}

tasks.classes {
    finalizedBy(buildLibrary)
}

publishing {
    publications.create<MavenPublication>("maven") {
        from(components["java"])
    }
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}
