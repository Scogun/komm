plugins {
    `maven-publish`
    signing
}

val libraryData = extensions.create("libraryData", PublishingExtension::class)

val stubJavadoc by tasks.creating(Jar::class) {
    archiveClassifier.set("javadoc")
}

publishing {
    publications.configureEach  {
        if (this is MavenPublication) {
            if (name != "kotlinMultiplatform") {
                artifact(stubJavadoc)
            }
            pom {
                name.set(libraryData.name)
                description.set(libraryData.description)
                url.set("https://github.com/Scogun/komm")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("Scogun")
                        name.set("Sergey Antonov")
                        email.set("SAntonov@ucasoft.com")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Scogun/komm.git")
                    developerConnection.set("scm:git:ssh://github.com:Scogun/komm.git")
                    url.set("https://github.com/Scogun/komm")
                }
            }
        }
    }
    repositories {
        maven {
            name = "MavenCentral"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
        }
    }
}

signing {
    sign(publishing.publications)
}