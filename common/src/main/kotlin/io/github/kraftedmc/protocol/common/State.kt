package io.github.kraftedmc.protocol.common

enum class State {
    Handshake,
    Status,
    Login,
    Play,
    Closed;

    companion object {
        fun get(name: String): State? {
            return when (name.uppercase()) {
                "HANDSHAKING", "HANDSHAKE" -> Handshake
                "LOGIN" -> Login
                "STATUS" -> Status
                "PLAY" -> Play
                "CLOSED" -> Closed
                else -> null
            }
        }
    }
}
