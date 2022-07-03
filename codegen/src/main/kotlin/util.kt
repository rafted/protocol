import io.github.kraftedmc.protocol.common.Direction
import io.github.kraftedmc.protocol.common.State

object util {

    fun getState(name: String): State? {
        return State.values()
            .filter { it.name.equals(name, true) }
            .firstOrNull()
    }

    fun getDirection(name: String): Direction? {
        return Direction.values()
            .filter { it.name.equals(name, true) }
            .firstOrNull()
    }

}