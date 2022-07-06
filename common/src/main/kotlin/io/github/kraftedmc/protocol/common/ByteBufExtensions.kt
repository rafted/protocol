package io.github.kraftedmc.protocol.common

import io.netty.buffer.ByteBuf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*

private const val SEGMENT_BITS = 0x7F
private const val CONTINUE_BIT = 0x80

fun ByteBuf.readVarInt(): Int {
    var value = 0
    var position = 0
    var currentByte: Byte

    while (true) {
        currentByte = readByte()
        value = value or (currentByte.toInt() and SEGMENT_BITS shl position)

        if (currentByte.toInt() and CONTINUE_BIT == 0)
            break

        position += 7

        if (position >= 32)
            throw RuntimeException("VarInt is too big")
    }

    return value
}

fun ByteBuf.readVarLong(): Long {
    var value: Long = 0
    var position = 0
    var currentByte: Byte

    while (true) {
        currentByte = readByte()
        value = value or ((currentByte.toInt() and SEGMENT_BITS shl position).toLong())

        if (currentByte.toInt() and CONTINUE_BIT == 0)
            break

        position += 7

        if (position >= 64)
            throw RuntimeException("VarInt is too big")
    }

    return value
}

fun ByteBuf.readString(): String {
    val length = readVarInt()
    val bytes = readBytes(length)

    return bytes.toString(Charsets.UTF_8)
}

fun ByteBuf.writeString(data: String) {
    writeVarInt(data.length)
    this.writeBytes(data.toByteArray(Charsets.UTF_8))
}

fun ByteBuf.writeVarInt(data: Int) {
    var value = data
    while (true) {
        if (value and SEGMENT_BITS == value) {
            writeByte(value)
            return
        }

        writeByte(value and SEGMENT_BITS or CONTINUE_BIT)
        value = value ushr 7
    }
}

fun ByteBuf.writeVarLong(data: Long) {
    var value = data
    while (true) {
        if (value and SEGMENT_BITS.toLong() == value) {
            writeByte(value.toInt())
            return
        }

        writeByte((value and SEGMENT_BITS.toLong() or CONTINUE_BIT.toLong()).toInt())
        value = value ushr 7
    }
}

fun ByteBuf.readVarBoolean(): Boolean {
    return this.readByte() == 0x01.toByte()
}

fun ByteBuf.writeVarBoolean(boolean: Boolean) {
    this.writeByte(if (boolean) 0x01 else 0x00)
}

fun ByteBuf.readUniqueId(): UUID {
    return UUID(readVarLong(), readVarLong())
}

fun ByteBuf.readChatComponent(): Component {
    val string = this.readString()
    return GsonComponentSerializer.gson().deserialize(string)
}

fun ByteBuf.writeChatComponent(component: Component) {
    val string = GsonComponentSerializer.gson().serialize(component)
    this.writeString(string)
}