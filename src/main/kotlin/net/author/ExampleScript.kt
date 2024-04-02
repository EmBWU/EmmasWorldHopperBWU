package net.author

import net.botwithus.internal.scripts.ScriptDefinition
import net.botwithus.rs3.game.Client
import net.botwithus.rs3.game.scene.entities.characters.player.Player
import net.botwithus.rs3.script.Execution
import net.botwithus.rs3.script.LoopingScript
import net.botwithus.rs3.script.config.ScriptConfig
import net.botwithus.rs3.game.Area
import net.botwithus.rs3.game.Coordinate
import net.botwithus.rs3.game.hud.interfaces.Interfaces
import net.botwithus.rs3.game.minimenu.MiniMenu
import net.botwithus.rs3.game.minimenu.actions.ComponentAction
import net.botwithus.rs3.game.queries.builders.characters.PlayerQuery
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery
import java.util.*


class ExampleScript(
    name: String,
    scriptConfig: ScriptConfig,
    scriptDefinition: ScriptDefinition
) : LoopingScript (name, scriptConfig, scriptDefinition) {

    val random: Random = Random()
    var botState: BotState = BotState.IDLE
    var playersAround = 0
    var timeNearby = 0

    val premiumWorlds = arrayOf(
        1, 5, 6, 9, 10, 12, 14, 15, 16, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 35, 36, 37, 39, 40, 44, 45,
        46, 49, 50, 51, 53, 54, 58, 59, 60, 62, 63, 64, 65, 67, 68, 69, 70, 71, 72, 73, 74, 76, 77, 78, 79,
        82, 83, 85, 88, 89, 91, 92, 97, 98, 99, 100, 103, 104, 105, 106, 116, 117, 119, 123, 124, 134, 138,
        140, 139, 252, 257, 258, 259
    )
    val lastWorlds: LinkedList<Int> = LinkedList()


    enum class BotState {
        IDLE,
        SCANFORPLAYERS,
        HOPPING
    }

    override fun initialize(): Boolean {
        super.initialize()
        // Set the script graphics context to our custom one
        this.sgc = ExampleGraphicsContext(this, console)
        println("Emmas World Hopper!")
        this.isBackgroundScript = true
        return true;
    }



    override fun onLoop() {
        val player = Client.getLocalPlayer();
        if (Client.getGameState() != Client.GameState.LOGGED_IN || player == null || botState == BotState.IDLE) {
            Execution.delay(random.nextLong(2500,5500))
            return
        }
        when (botState) {
            BotState.SCANFORPLAYERS -> {
                Execution.delay(handleSkilling(player))
                return
            }
            BotState.HOPPING -> {
                if (hopWorlds()) {
                    botState = BotState.SCANFORPLAYERS
                    playersAround = 0
                }
                return
            }
            else -> {
                println("Unexpected bot state, report to author!")
            }
        }
        Execution.delay(random.nextLong(2000,4000))
        return
    }

    fun hopWorlds(): Boolean {
        return openHopWorldsMenu()
            .takeIf { it }
            ?.let { openWorldList() }
            ?.let { chooseAndSwitchWorld() }
            ?: false.also { println("Cannot switch world right now.") }
    }

    private fun openHopWorldsMenu(): Boolean = interactWithComponent(1, 7, 93782016)
        .conditionalDelay(Interfaces::isOpen, 1433, "Hop Worlds Button not found.")

    private fun openWorldList(): Boolean = interactWithSubcomponent(1433, 65)
        .conditionalDelay(Interfaces::isOpen, 1587)

    private fun chooseAndSwitchWorld(): Boolean {
        val nextWorld = getNextWorld()
        val switchWorld = takeIf { validateWorld(nextWorld) }
            ?.let {
                interactWithComponent(2, nextWorld, 104005640)
                    .also { if (it) println("Hopping to world $nextWorld") }
            } ?: false

        Execution.delay(random.nextLong(5000, 6000)) // Delay to account for the loading screen
        return switchWorld
    }
    private fun validateWorld(world: Int): Boolean {
        lastWorlds.apply {
            addFirst(world)
            if (size > 6) {
                removeFirst().also { println("Removed first element (World $it) from lastWorlds") }
            }
        }
        return true
    }

    private fun getNextWorld(): Int {
        return generateSequence { premiumWorlds.random() }
            .first { it !in lastWorlds }
    }

    private fun interactWithComponent(type: Int, action: Int, id: Int): Boolean {
        MiniMenu.interact(ComponentAction.COMPONENT.type, type, action, id)
        return Execution.delay(random.nextLong(2000, 5000))
    }

    private fun interactWithSubcomponent(interfaceId: Int, componentIndex: Int): Boolean {
        val menuComponent = ComponentQuery.newQuery(interfaceId).componentIndex(componentIndex).results().firstOrNull()
        menuComponent?.interact(1) ?: println("Subcomponent not found in interface $interfaceId.")
        return menuComponent != null
    }

    private fun Boolean.conditionalDelay(condition: (Int) -> Boolean, id: Int, onError: String = ""): Boolean {
        val delayed = Execution.delayUntil(5000) { condition(id) }
        if (!delayed && onError.isNotBlank()) println(onError)
        return delayed
    }
    private fun handleSkilling(player: Player): Long {
        val playercorner1 = player.coordinate?.let {
            Coordinate(it.x + 5, it.y + 5, it.z)
        }
        val playercorner2 = Coordinate(player.coordinate?.x?.minus(5)!!, player.coordinate?.y?.minus(5)!!, player.coordinate?.z!!)
        val aroundPlayer : Area.Rectangular? = playercorner1?.let { pc ->
            playercorner2?.let { pc2 ->
                Area.Rectangular(pc, pc2)
            }
        }
        val otherPlayers = PlayerQuery.newQuery().inside(aroundPlayer).results().filter { it.name != player.name }
            if (otherPlayers.size > 0) {
                timeNearby += 1
                playersAround = otherPlayers.size
            } else {
                timeNearby = 0
                playersAround = 0
            }
            if (timeNearby > 5) {
                println("Hopping worlds, $playersAround players nearby")
                botState = BotState.HOPPING
            }
        return random.nextLong(1000,3000)
    }
}