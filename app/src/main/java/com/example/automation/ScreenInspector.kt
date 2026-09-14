package com.example.automation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ScreenUiNode(
    val id: String,
    val text: String,
    val type: String, // "BUTTON", "INPUT", "TEXT", "IMAGE", "LIST_ITEM"
    val isClickable: Boolean,
    val bounds: String, // e.g. "[120, 450][480, 520]"
    val confidence: Float = 0.98f
)

data class ScreenInspectionState(
    val currentApp: String = "Instagram",
    val status: String = "Idle",
    val isAnalyzing: Boolean = false,
    val nodes: List<ScreenUiNode> = emptyList(),
    val focusedNodeId: String? = null,
    val lastActionReport: String? = null
)

class ScreenInspector {

    private val _state = MutableStateFlow(ScreenInspectionState())
    val state: StateFlow<ScreenInspectionState> = _state.asStateFlow()

    suspend fun analyzeScreen(appName: String): List<ScreenUiNode> {
        _state.value = _state.value.copy(
            currentApp = appName,
            status = "Scanning UI hierarchy nodes...",
            isAnalyzing = true
        )
        delay(600)

        val detectedNodes = when (appName.lowercase()) {
            "instagram" -> listOf(
                ScreenUiNode("ig_search", "Search & Explore", "INPUT", true, "[64, 180][980, 260]"),
                ScreenUiNode("ig_install", "Install / Get", "BUTTON", true, "[720, 310][960, 390]"),
                ScreenUiNode("ig_feed", "Home Feed", "LIST_ITEM", true, "[0, 400][1080, 1800]"),
                ScreenUiNode("ig_dm", "Direct Messages (3)", "BUTTON", true, "[920, 80][1040, 160]")
            )
            "youtube" -> listOf(
                ScreenUiNode("yt_search", "Search YouTube", "INPUT", true, "[120, 80][880, 160]"),
                ScreenUiNode("yt_filter", "Relaxing Music", "BUTTON", true, "[40, 180][320, 240]"),
                ScreenUiNode("yt_card_1", "Deep Sleep & Relaxing Ambient Lo-Fi (Live 24/7)", "LIST_ITEM", true, "[0, 260][1080, 780]"),
                ScreenUiNode("yt_card_2", "Calm Piano Meditation Sounds", "LIST_ITEM", true, "[0, 800][1080, 1320]")
            )
            "play store" -> listOf(
                ScreenUiNode("ps_search", "Search for apps & games", "INPUT", true, "[48, 90][920, 170]"),
                ScreenUiNode("ps_install", "Install", "BUTTON", true, "[700, 340][980, 420]"),
                ScreenUiNode("ps_verified", "Verified by Play Protect", "TEXT", false, "[120, 430][540, 470]")
            )
            else -> listOf(
                ScreenUiNode("node_1", "Primary Action", "BUTTON", true, "[80, 300][400, 380]"),
                ScreenUiNode("node_2", "Search or Query Input", "INPUT", true, "[80, 180][920, 260]"),
                ScreenUiNode("node_3", "Content List View", "LIST_ITEM", true, "[40, 420][1040, 1200]")
            )
        }

        _state.value = _state.value.copy(
            status = "Identified ${detectedNodes.size} interactive elements",
            isAnalyzing = false,
            nodes = detectedNodes,
            lastActionReport = "Screen layout verified. Ready for dynamic interaction."
        )
        return detectedNodes
    }

    suspend fun executeSimulatedTap(nodeId: String): Boolean {
        val node = _state.value.nodes.find { it.id == nodeId } ?: return false
        _state.value = _state.value.copy(
            focusedNodeId = nodeId,
            status = "Executing tap on [${node.text}] at ${node.bounds}..."
        )
        delay(500)
        _state.value = _state.value.copy(
            status = "Tapped '${node.text}' successfully. State verified.",
            lastActionReport = "Action verified: target element reacted to input."
        )
        return true
    }

    suspend fun executeSimulatedScroll(down: Boolean) {
        _state.value = _state.value.copy(
            status = if (down) "Simulating swipe down gesture..." else "Simulating swipe up gesture..."
        )
        delay(400)
        _state.value = _state.value.copy(
            status = "Screen viewport updated. Recalculating visible items.",
            lastActionReport = "Scroll complete. New items loaded into viewport."
        )
    }
}
