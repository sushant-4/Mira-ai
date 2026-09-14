package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class MiraAccessibilityService : AccessibilityService() {

    companion object {
        var instance: MiraAccessibilityService? = null
        val isServiceConnected: Boolean get() = instance != null

        fun performGlobal(action: Int): Boolean {
            return instance?.performGlobalAction(action) ?: false
        }

        fun pressHome(): Boolean {
            return performGlobal(GLOBAL_ACTION_HOME)
        }

        fun pressBack(): Boolean {
            return performGlobal(GLOBAL_ACTION_BACK)
        }

        fun openNotifications(): Boolean {
            return performGlobal(GLOBAL_ACTION_NOTIFICATIONS)
        }

        fun openRecents(): Boolean {
            return performGlobal(GLOBAL_ACTION_RECENTS)
        }

        fun getActiveScreenNodes(): List<String> {
            val root = instance?.rootInActiveWindow ?: return emptyList()
            val nodes = mutableListOf<String>()
            traverseNodes(root, nodes, 0)
            return nodes
        }

        private fun traverseNodes(node: AccessibilityNodeInfo, list: MutableList<String>, depth: Int) {
            val text = node.text?.toString() ?: node.contentDescription?.toString()
            if (!text.isNullOrBlank()) {
                val cleanClass = node.className?.toString()?.substringAfterLast(".") ?: "View"
                list.add("[$cleanClass] $text")
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { traverseNodes(it, list, depth + 1) }
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        serviceInfo = serviceInfo.apply {
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS or
                    AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) instance = null
    }
}
