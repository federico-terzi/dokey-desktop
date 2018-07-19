package system.context

import system.keyboard.KeyboardManager

interface GeneralContext : SearchContext, CommandTemplateContext {
    val keyboardManager : KeyboardManager
}