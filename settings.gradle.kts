rootProject.name = "protocol"
include("common", "codegen")

java.nio.file.Paths.get("./").toFile()
    .listFiles()!!
    .filter { it.isDirectory }
    .filter { it.name.startsWith("protocol_") }
    .forEach { include(it.name) }
