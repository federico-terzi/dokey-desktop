package system.context

import system.commands.validator.CommandValidationContext
import system.keyboard.KeyboardManager

interface GeneralContext : SearchContext, CommandTemplateContext, ImageSourceContext, MobileServerContext,
    CommandValidationContext{
    val keyboardManager : KeyboardManager
}