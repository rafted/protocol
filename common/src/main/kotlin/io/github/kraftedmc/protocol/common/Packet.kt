package io.github.kraftedmc.protocol.common

import io.netty.buffer.ByteBuf

interface Packet {
    val id: Int
    val state: State
    val direction: Direction

    fun unpack(buffer: ByteBuf) {}
    fun pack(buffer: ByteBuf) {}
}
