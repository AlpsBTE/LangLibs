plugins {
    java
    id("buildlogic.java-conventions")
    alias(libs.plugins.palantir.git.version)
    alias(libs.plugins.gradleup.shadow)
}

dependencies {
    implementation(project(path = ":LangLibs-API"))
    compileOnly(libs.io.papermc.paper.paper.api)
}

val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
val details = versionDetails()
version = "1.5.3" //+ "-SNAPSHOT" + "+" + details.gitHash

tasks.shadowJar {
    archiveClassifier = ""
    relocate("li.cinnazeyy.langlibs.core", "li.cinnazeyy.langlibs.core")

    relocationPrefix = "li.cinnazeyy.langlibs.shaded"
    enableAutoRelocation = true
}

tasks.assemble {
    dependsOn(tasks.shadowJar) // Ensure that the shadowJar task runs before the build task
}

tasks.jar {
    archiveClassifier = "UNSHADED"
    enabled = false // Disable the default jar task since we are using shadowJar
}

tasks.processResources {
    // work around IDEA-296490
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
    with(copySpec {
        from("src/main/resources/paper-plugin.yml") {
            expand("version" to version)
        }
    })
}

java.sourceCompatibility = JavaVersion.VERSION_21
java.targetCompatibility = JavaVersion.VERSION_21
