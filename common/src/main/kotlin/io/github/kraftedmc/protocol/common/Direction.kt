package io.github.kraftedmc.protocol.common

enum class Direction {
    /**
     * Server to Client
     */
    Clientbound,

    /**
     * Client to Server
     */
    Serverbound;

    companion object {
        fun get(name: String): Direction? {
            return Direction.values()
                .filter { it.name.equals(name, true) }
                .firstOrNull()
        }
    }
}
