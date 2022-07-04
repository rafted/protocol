package io.github.kraftedmc.protocol.common

enum class State {
    Handshake,
    Status,
    Login,
    Play,
    Closed;

    companion object {
        fun get(name: String): State? {
            return when(name.uppercase()) {
                "HANDSHAKING", "HANDSHAKE" -> State.Handshake
                "LOGIN" -> State.Login
                "STATUS" -> State.Status
                "PLAY" -> State.Play
                "CLOSED" -> State.Closed
                else -> null
            }
        }
    }
}