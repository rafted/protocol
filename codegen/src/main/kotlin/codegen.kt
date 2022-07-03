import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.github.kraftedmc.protocol.common.Packet

typealias PacketDef = Map<String, *>

@OptIn(DelicateKotlinPoetApi::class)
object codegen {

    fun generatePacketClass(`package`: String, packet: PacketDef): FileSpec {
        val className = (packet["class"]!! as String).split(".")[0]

        val file = FileSpec.builder(`package`, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .superclass(Packet::class.java)
                    .build()
            )
            .build()

        return file
    }

}
