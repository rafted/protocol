import Operation.*
import com.squareup.kotlinpoet.*
import io.github.kraftedmc.protocol.common.*
import io.netty.buffer.ByteBuf
import kotlin.reflect.KClass

typealias PacketDef = Map<String, *>

enum class Type(
    val writeFun: MemberName,
    val readFun: MemberName,
    val type: KClass<*>,
    val initializer: Any
) {
    Varint(
        MemberName("io.github.kraftedmc.protocol.common", "writeVarInt", true),
        MemberName("io.github.kraftedmc.protocol.common", "readVarInt", true),
        Int::class,
        0
    ),
    Varlong(
        MemberName("io.github.kraftedmc.protocol.common", "writeVarLong", true),
        MemberName("io.github.kraftedmc.protocol.common", "readVarLong", true),
        Long::class,
        0
    ),
    Short(
        MemberName("", "writeShort"),
        MemberName("", "readShort"),
        Int::class,
        0
    ),
    String(
        MemberName("io.github.kraftedmc.protocol.common", "writeString"),
        MemberName("io.github.kraftedmc.protocol.common", "readString"),
        kotlin.String::class,
        "\"\"" // HACK: This is a hack, because either I'm stupid, or KotlinPoet is very, very stupid.
    );

    companion object {
        fun get(name: kotlin.String): Type? {
            return when (name.lowercase()) {
                "varint" -> Varint
                "varlong" -> Varlong
                "string" -> String
                "short" -> Short
                else -> null
            }
        }
    }
}

enum class Operation {
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
        fun get(name: String): Operation? {
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
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%L", packet["id"]!!)
            .build()

        // direction property
        val direction = MemberName(
            "io.github.kraftedmc.protocol.common.Direction",
            Direction.get(packet["direction"]!! as String)!!.name
        )

        val directionProperty = PropertySpec.builder("direction", Direction::class.java)
            .addModifiers(KModifier.FINAL)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%M", direction)
            .build()

        // state property
        val state = MemberName(
            "io.github.kraftedmc.protocol.common.State",
            State.get(packet["state"]!! as String)!!.name
        )

        val stateProperty = PropertySpec.builder("state", State::class.java)
            .addModifiers(KModifier.FINAL)
            .addModifiers(KModifier.OVERRIDE)
            .initializer("%M", state)
            .build()

        // field properties
        val properties: List<PropertySpec>

        (packet["instructions"]!! as List<Map<String, Any>>)
            .filter { it["operation"] == "write" || it["operation"] == "read" }
            .map {
                mapOf(
                    "field" to (it["field"]!! as String).split(".")[0],
                    "type" to Type.get(it["type"]!! as String),
                )
            }
            .map {
                val type = Type.get(it["type"]!!.toString())!!
                PropertySpec.builder(
                    it["field"]!! as String,
                    type.type
                )
                    .initializer("%L", type.initializer)
                    .build()
            }
            .apply { properties = this }

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

        packFunBuilder = generatePackOrUnpackFun(packFunBuilder, packet, true)
        unpackFunBuilder = generatePackOrUnpackFun(unpackFunBuilder, packet, false)

        // create the file
        val file = FileSpec.builder(`package`, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .addSuperinterface(Packet::class.java)
                    .addProperty(idProperty)
                    .addProperty(directionProperty)
                    .addProperty(stateProperty)
                    .addProperties(properties)
                    .addFunction(packFunBuilder.build())
                    .addFunction(unpackFunBuilder.build())
                    .build()
            )
            .build()

        return file
    }

    fun generatePackOrUnpackFun(_builder: FunSpec.Builder, packet: Map<String, *>, pack: Boolean): FunSpec.Builder {
        val instructions: List<Map<String, String>> = (packet["instructions"] as List<Map<String, String>>?)!!
        var builder = _builder

        instructions.forEach {
            val operation = Operation.get(it["operation"]!!)!!
                .let {
                    if (pack) it else it.opposite
                }

            val type = Type.get(it["type"]!!)!!

            // remove function calls from field to read, because in minecraft's code... well, they're weird
            val field = (it["field"]!! as String).split(".")[0]

            when (operation) {
                Write, Read -> {

                    when (operation) {
                        Write -> {
                            builder = builder.addStatement("buffer.%M(%L)", type.writeFun, field)
                        }
                        Read -> {
                            builder = builder.addStatement("%L = buffer.%M()", field, type.readFun)
                        }

                        else -> TODO("are you stupid")
                    }

                }
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
