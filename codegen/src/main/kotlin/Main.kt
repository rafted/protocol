import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Paths

fun main(args: Array<String>) {
    // read arguments
    if (args.size < 2) {
        println("Usage: <however you ran the program> <input file> <output module name>")
        return
    }

    val dataFilePath = args[0]
    val moduleName = args[1]

    //
    // create module structure
    //

    // create directory
    val moduleDir = Paths.get(moduleName)
        .apply { this.toFile().mkdirs() }

    // create build.gradle.kts
    val buildFile = File("$moduleDir/build.gradle.kts")
    buildFile.writeText("""
        plugins {
            id("java")
            kotlin("jvm") version "1.7.0"
        }

        dependencies {
            implementation(project(":common"))
        }
    """.trimIndent())

    // make directory structure
    val mainDirectory = moduleDir.resolve("src").resolve("main")
        .apply { this.toFile().mkdirs() }

    mainDirectory.resolve("resources").toFile().mkdirs()

    val sourceDirectory = mainDirectory.resolve("kotlin")
        .apply { this.toFile().mkdirs() }

    //
    // read data file
    //
    val dataFile = File(dataFilePath)
    val data = ObjectMapper().readValue<List<Map<String, Any>>>(dataFile)[0]

    // read metadata (lord forgive me)
    val protocol = (data["version"]!! as Map<*, *>)["protocol"]!! as Int

    // read packets
    val packets = (data["packets"]!! as Map<*, *>)["packet"]!! as Map<*, *>

    packets.values
        .map { it as Map<String, *> }
        .get(1).apply {
            val className = (this["class"]!! as String).split(".")[0]

            codegen.generatePacketClass(moduleName, this).writeTo(sourceDirectory)
        }

//    packets.values
//        .map { it as Map<String, *> }
//        .forEach {
//            codegen.generatePacketClass(moduleName, it).writeTo(System.out)
//        }
}
