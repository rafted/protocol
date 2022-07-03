import com.squareup.kotlinpoet.*
import io.github.kraftedmc.protocol.common.Direction
import io.github.kraftedmc.protocol.common.Packet
import io.github.kraftedmc.protocol.common.State

typealias PacketDef = Map<String, *>

@OptIn(DelicateKotlinPoetApi::class)
object codegen {

    fun generatePacketClass(`package`: String, packet: PacketDef): FileSpec {
        val className = (packet["class"]!! as String).split(".")[0]

        // properties
        val idProperty = PropertySpec.builder("id", Int::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", packet["id"]!!)
            .build()

        val directionProperty = PropertySpec.builder("direction", Direction::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", util.getDirection(packet["direction"]!! as String))
            .build()

        val stateProperty = PropertySpec.builder("state", State::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", util.getState(packet["state"]!! as String))
            .build()

        // create the file
        val file = FileSpec.builder(`package`, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .superclass(Packet::class.java)
                    .addProperty(idProperty)
                    .addProperty(directionProperty)
                    .addProperty(stateProperty)
                    .build()
            )
            .build()

        return file
    }

}
