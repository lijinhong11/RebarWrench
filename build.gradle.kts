import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

import com.vanniktech.maven.publish.DeploymentValidation
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SourcesJar

plugins {
    java
    idea
    signing
    id("com.gradleup.shadow") version "9.4.2"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("io.freefair.lombok") version "9.5.0"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "io.github.lijinhong11"
version = project.properties["version"] as String

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") {
        name = "papermc"
    }
    maven("https://jitpack.io") {
        name = "JitPack"
    }
    maven("https://repo.xenondevs.xyz/releases")
}

val rebarVersion = project.properties["rebar.version"] as String
val pylonVersion = project.properties["pylon.version"] as String

dependencies {
    compileOnly("io.papermc.paper:paper-api:[26.1.2-build.+,)")
    compileOnly("io.github.pylonmc:rebar:$rebarVersion")
    compileOnly("io.github.pylonmc:pylon:$pylonVersion")
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

// Configuration for the output JAR
tasks.shadowJar {
    archiveClassifier = ""
}

// Generate the plugin.yml file using the bukkit gradle plugin
bukkit {
    name = project.name
    main = project.properties["main-class"] as String
    version = project.version.toString()
    apiVersion = "26.1"
    depend = listOf("Rebar")
    softDepend = listOf("Pylon")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}

// Run a server using the run server gradle plugin
tasks.runServer {
    doFirst {
        // Remove the plugins folder. This is so any changes to language files etc are propagated.
        val runFolder = project.projectDir.resolve("run")
        val pluginsDir = runFolder.resolve("plugins")
        pluginsDir.deleteRecursively()
    }

    // Download pylon core and add it to the plugins folder
    downloadPlugins {
        github("pylonmc", "rebar", rebarVersion, "rebar-$rebarVersion.jar")
    }

    // Download pylon base and add it to the plugins folder
    downloadPlugins {
        github("pylonmc", "pylon", pylonVersion, "pylon-$pylonVersion.jar")
    }

    maxHeapSize = "2G"
    minecraftVersion("26.1.2")
}

mavenPublishing {
    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)

    signAllPublications()

    coordinates("io.github.lijinhong11", "RebarWrench", project.version.toString())

    pom {
        name.set(project.name)
        description.set("Wrench for rebar addons")
        url.set("https://github.com/lijinhong11/RebarWrench")

        licenses {
            license {
                name.set("GPL 3.0 License")
                url.set("https://www.gnu.org/licenses/gpl-3.0.en.html")
            }
        }

        developers {
            developer {
                id.set("lijinhong11")
                name.set("Jinhong Li")
                email.set("tygfhk@outlook.com")
            }
        }

        scm {
            connection.set("scm:git:git://github.com/lijinhong11/RebarWrench.git")
            developerConnection.set("scm:git:ssh://github.com:lijinhong11/RebarWrench.git")
            url.set("https://github.com/lijinhong11/RebarWrench")
        }
    }

    configureBasedOnAppliedPlugins(
        // configures the -javadoc artifact, possible values:
        // - `JavadocJar.None()` don't publish this artifact
        // - `JavadocJar.Empty()` publish an empty jar
        // - `JavadocJar.Javadoc()` to publish standard javadocs
        // - `JavadocJar.Dokka("dokkaHtml")` when using Kotlin with Dokka, where `dokkaHtml` is the name of the Dokka task that should be used as input
        javadocJar = JavadocJar.Javadoc(),
        // configures the -sources artifact, possible values:
        // - `SourcesJar.None()` don't publish this artifact
        // - `SourcesJar.Empty()` publish an empty jar
        // - `SourcesJar.Sources()` publish the sources
        sourcesJar = SourcesJar.Sources()
    )
}

signing {
    useGpgCmd()
}