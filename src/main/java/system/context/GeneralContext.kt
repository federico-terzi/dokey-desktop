package system.context

import system.keyboard.KeyboardManager

interface GeneralContext : SearchContext, CommandTemplateContext, ImageSourceContext {
    val keyboardManager : KeyboardManager
}