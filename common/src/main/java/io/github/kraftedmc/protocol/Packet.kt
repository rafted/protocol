package io.github.kraftedmc.protocol

interface Packet {
    val id: Int
    val state: State
    val direction: Direction

    fun unpack(buffer: ByteBuf) {}
    fun pack(buffer: ByteBuf) {}

    fun createEvent(): Event? {
        return null
    }
}