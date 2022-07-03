import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File

fun main(args: Array<String>) {
    if(args.size < 2) {
        println("Usage: <however you ran the program> <input file> <output file>")
        return
    }

    val dataFilePath = args[0]
    val outPath = args[1]

    val dataFile = File(dataFilePath)
    val data = ObjectMapper().readValue<List<Map<String, Any>>>(dataFile)[0]

    // read metadata (lord forgive me)
    val protocol = (data["version"]!! as Map<*, *>)["protocol"]!! as Int

    // read packets
    val packets = (data["packets"]!! as Map<*, *>)["packet"]!! as Map<*, *>

    packets.values
        .map { it as Map<String, *> }
        .forEach {
            val className = (it["class"]!! as String).split(".")[0]

            val file = FileSpec.builder("protocol_$protocol", className)
                .addType(
                    TypeSpec.classBuilder(className)
                        .build()
                )
                .build()

            file.writeTo(System.out)
        }
}
