package net.author

import net.botwithus.rs3.imgui.ImGui
import net.botwithus.rs3.script.ScriptConsole
import net.botwithus.rs3.script.ScriptGraphicsContext

class ExampleGraphicsContext(
    private val script: ExampleScript,
    console: ScriptConsole
) : ScriptGraphicsContext (console) {

    override fun drawSettings() {
        super.drawSettings()
        ImGui.Begin("Emmas World hopper", 0)
        ImGui.SetWindowSize(250f, -1f)
        ImGui.Text("Players in area: " + script.playersAround)
        if (ImGui.Button("Start")) {
            script.botState = ExampleScript.BotState.SCANFORPLAYERS;
        }
        ImGui.SameLine()
        if (ImGui.Button("Stop")) {
            script.botState = ExampleScript.BotState.IDLE
        }
        ImGui.End()
    }

    override fun drawOverlay() {
        super.drawOverlay()
    }

}