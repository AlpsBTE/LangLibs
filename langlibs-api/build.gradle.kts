plugins {
    `java-library`
    id("buildlogic.java-conventions")
    `maven-publish`
}

version = "1.6.0"

dependencies {
    api(libs.org.mariadb.jdbc.mariadb.java.client)
    api(libs.com.zaxxer.hikaricp) {
        exclude(group = "org.slf4j")
    }
    api(libs.org.spongepowered.configurate.yaml)
    api(libs.alpslib.io) // For the Database Utility Methods
    compileOnly(libs.io.papermc.paper.paper.api)
    compileOnly(libs.commons.io.commons.io)
}

val alpsMavenUser: String? = project.findProperty("alpsMavenUser") as String?
val alpsMavenPassword: String? = project.findProperty("alpsMavenPassword") as String?

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            credentials {
                username = alpsMavenUser
                password = alpsMavenPassword
            }
            url = uri("https://mvn.alps-bte.com/repository/alps-bte/")
        }
    }
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21

java {
    withSourcesJar()
    withJavadocJar()
}