package system.context

import system.commands.validator.CommandValidationContext
import system.external.photoshop.PhotoshopManager
import system.keyboard.KeyboardManager
import system.system.SystemManager

interface GeneralContext : SearchContext, CommandTemplateContext, ImageSourceContext, MobileServerContext,
    CommandValidationContext{
    val keyboardManager : KeyboardManager
    val systemManager : SystemManager
    val photoshopManager : PhotoshopManager
}