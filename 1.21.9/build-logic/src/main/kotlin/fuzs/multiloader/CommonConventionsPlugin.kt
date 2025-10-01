package fuzs.multiloader

import dev.architectury.plugin.ArchitectPluginExtension
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

class CommonConventionsPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        plugins.apply("dev.architectury.loom")

        extensions.configure<ArchitectPluginExtension> {
            common(
                rootProject.subprojects
                    .map { it.name.lowercase() }
                    .filterNot { it.contains("common") }
            )
        }

        extensions.configure<LoomGradleExtensionAPI> {
            accessWidenerPath.set(
                layout.projectDirectory.file("src/main/resources/${property("modId")}.accesswidener")
            )
        }

        dependencies.add("modCompileOnly", "net.fabricmc:fabric-loader:0.16.7")
    }
}
