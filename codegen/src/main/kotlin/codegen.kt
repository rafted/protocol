import Operation.*
import com.squareup.kotlinpoet.*
import io.github.kraftedmc.protocol.common.Direction
import io.github.kraftedmc.protocol.common.Packet
import io.github.kraftedmc.protocol.common.State
import io.netty.buffer.ByteBuf

typealias PacketDef = Map<String, *>

enum class Operation() {
    Write,
    Read,
    Store,
    If,
    Loop,
    Else,
    Switch;

    // there surely has to be a better way to do this, but i'm rushing
    val opposite: Operation
        get() = when (this) {
            Write -> Read
            Read -> Write
            else -> this
        }

    companion object {
        fun find(name: String): Operation? {
            return values().firstOrNull() { it.name.equals(name, true) }
        }
    }
}


@OptIn(DelicateKotlinPoetApi::class)
object codegen {

    fun generatePacketClass(`package`: String, packet: PacketDef): FileSpec {
        val className = (packet["class"]!! as String).split(".")[0]

        //
        // properties
        //

        // id property
        val idProperty = PropertySpec.builder("id", Int::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", packet["id"]!!)
            .build()

        // direction property
        val direction = Direction.get(packet["direction"]!! as String)

        val directionProperty = PropertySpec.builder("direction", Direction::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", direction)
            .build()

        // state property
        val state = State.get(packet["state"]!! as String)

        val stateProperty = PropertySpec.builder("state", State::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", state)
            .build()

        //
        // pack / unpack functions
        //

        var packFunBuilder: FunSpec.Builder = FunSpec.builder("pack")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("buffer", ByteBuf::class.java)
            .returns(Unit::class.java)

        var unpackFunBuilder: FunSpec.Builder = FunSpec.builder("unpack")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter("buffer", ByteBuf::class.java)
            .returns(Unit::class.java)

        // Server -> Client
        if(direction == Direction.Clientbound) {
            packFunBuilder = generatePackFun(packFunBuilder, packet)
        }

        // Client -> Server
        if(direction == Direction.Serverbound) { }

        // create the file

        val file = FileSpec.builder(`package`, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .superclass(Packet::class.java)
                    .addProperty(idProperty)
                    .addProperty(directionProperty)
                    .addProperty(stateProperty)
                    .addFunction(packFunBuilder.build())
                    .addFunction(unpackFunBuilder.build())
                    .build()
            )
            .build()

        return file
    }

    fun generatePackFun(builder: FunSpec.Builder, packet: Map<String, *>): FunSpec.Builder {
        val instructions: List<Map<String, String>> = (packet["instructions"] as List<Map<String, String>>?)!!

        instructions.forEach {
            val operation = Operation.find(it["operation"]!!)!!
                .opposite // <- we do this because we're building a server,
                          // and the instructions are from the client's side
                          // because they've been extracted from the client

            when (operation) {
                Write -> TODO()
                Read -> TODO()
                Store -> TODO()
                If -> TODO()
                Loop -> TODO()
                Else -> TODO()
                Switch -> TODO()
            }
        }

        return builder
    }

}
