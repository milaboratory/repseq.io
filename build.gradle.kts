import com.palantir.gradle.gitversion.VersionDetails
import java.util.Base64
import groovy.lang.Closure

plugins {
    `java-library`
    `maven-publish`
    signing
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
    id("com.palantir.git-version") version "0.12.3"
}


val miRepoAccessKeyId: String by project
val miRepoSecretAccessKey: String by project

val versionDetails: Closure<VersionDetails> by extra
val gitDetails = versionDetails()

val longTests: String? by project

group = "io.repseq"
val gitLastTag = gitDetails.lastTag.removePrefix("v")
version =
    if (gitDetails.commitDistance == 0) gitLastTag
    else "${gitLastTag}-${gitDetails.commitDistance}-${gitDetails.gitHash}"
description = "RepSeqIO"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

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

val milibVersion = "1.14-1-2fdb618468"
val jacksonVersion = "2.12.3"

dependencies {
    api("com.milaboratory:milib:$milibVersion")

    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("org.reflections:reflections:0.9.10")
    implementation("commons-io:commons-io:2.7")
    implementation("org.apache.httpcomponents:httpclient:4.5.10")
    implementation("ch.qos.logback:logback-classic:1.3.0-alpha5")
    implementation("com.beust:jcommander:1.72")
    implementation("net.sf.trove4j:trove4j:3.0.3")

    testImplementation("junit:junit:4.13.2")
    testImplementation(testFixtures("com.milaboratory:milib:$milibVersion"))
    testImplementation("org.mockito:mockito-all:1.10.19")
}

val buildLibrary by tasks.registering(JavaExec::class) {
    main = "io.repseq.maven.CompileLibraryGradleStage"
    classpath = sourceSets["main"].runtimeClasspath
    args(project.rootDir)
}

tasks.classes {
    finalizedBy(buildLibrary)
}

publishing {
    repositories {
        maven {
            name = "mipub"
            url = uri("s3://milaboratory-artefacts-public-files.s3.eu-central-1.amazonaws.com/maven")

            authentication {
                credentials(AwsCredentials::class) {
                    accessKey = miRepoAccessKeyId
                    secretKey = miRepoSecretAccessKey
                }
            }
        }
    }

    publications.create<MavenPublication>("mavenJava") {
        from(components["java"])
        pom {
            withXml {
                asNode().apply {
                    appendNode("name", "RepseqIO")
                    appendNode(
                        "description",
                        "Command line helper to manipulate RepSeq.IO formatted V/D/J/C reference data."
                    )
                    appendNode("url", "https://milaboratory.com/")
                }
            }
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }
            developers {
                developer {
                    id.set("dbolotin")
                    name.set("Dmitry Bolotin")
                    email.set("bolotin.dmitriy@gmail.com")
                }
                developer {
                    id.set("PoslavskySV")
                    name.set("Stanislav Poslavsky")
                    email.set("stvlpos@mail.ru")
                }
            }
            scm {
                url.set("scm:git:https://github.com/repseq/repseqio.git")
            }
        }
    }
}

val signingKey: String? by project
if (signingKey != null) {
    signing {
        useInMemoryPgpKeys(
            Base64.getMimeDecoder().decode(signingKey).decodeToString(),
            ""
        )
        sign(publishing.publications["mavenJava"])
    }
}

tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).addStringOption("Xdoclint:none", "-quiet")
}

tasks.withType<JavaCompile>() {
    options.encoding = "UTF-8"
}

val checkMiLibNotSnapshot by tasks.registering {
    doLast {
        // if (milibVersion.contains('-'))
        //     throw GradleException("Can't publish to maven central with snapshot dependencies.")
    }
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

afterEvaluate {
    tasks.named("publishToSonatype") {
        dependsOn(checkMiLibNotSnapshot)
    }
}
