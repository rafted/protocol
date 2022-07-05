import Operation.*
import com.squareup.kotlinpoet.*
import io.github.kraftedmc.protocol.common.*
import io.netty.buffer.ByteBuf
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

typealias PacketDef = Map<String, *>

enum class Type(val writeFun: KFunction<*>, val readFun: KFunction<*>, val type: KClass<*>) {
    Varint(ByteBuf::writeVarInt, ByteBuf::readVarInt, Int::class),
    Varlong(ByteBuf::writeVarLong, ByteBuf::readVarLong, Long::class),
    Short(ByteBuf::writeShort, ByteBuf::readShort, Int::class),
    String(ByteBuf::writeString, ByteBuf::readString, kotlin.String::class);

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
            .initializer("%L", packet["id"]!!)
            .build()

        // direction property
        val direction = Direction.get(packet["direction"]!! as String)

        val directionProperty = PropertySpec.builder("direction", Direction::class.java)
            .addModifiers(KModifier.FINAL)
            .initializer("%L", direction)
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
                PropertySpec.builder(
                    it["field"]!! as String,
                    Type.get(it["type"]!!.toString())!!.type
                )
                .build()
            }
            .apply { properties = this }

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

        packFunBuilder = generatePackOrUnpackFun(packFunBuilder, packet, true)
        unpackFunBuilder = generatePackOrUnpackFun(unpackFunBuilder, packet, false)

        // create the file
        val file = FileSpec.builder(`package`, className)
            .addType(
                TypeSpec.classBuilder(className)
                    .superclass(Packet::class.java)
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
            val field = it["field"]!!

            when (operation) {
                Write, Read -> {
                    var location: String = ""
                    var member: MemberName

                    when (operation) {
                        Write -> {
                            location = type.writeFun.javaMethod?.declaringClass?.canonicalName!!
                            member = MemberName(location, type.writeFun.name)
                        }
                        Read -> {
                            location = type.readFun.javaMethod?.declaringClass?.canonicalName!!
                            member = MemberName(location, type.readFun.name)
                        }

                        else -> TODO("are you stupid")
                    }
//
                    builder = builder.addStatement("buffer.%M(%L)", member, field)
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
