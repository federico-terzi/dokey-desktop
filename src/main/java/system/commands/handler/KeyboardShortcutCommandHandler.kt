package system.commands.handler

import system.DokeyContext
import system.commands.annotations.RegisterHandler
import system.commands.general.KeyboardShortcutCommand

@RegisterHandler(commandType = KeyboardShortcutCommand::class)
class KeyboardShortcutCommandHandler(context: DokeyContext) : CommandHandler<KeyboardShortcutCommand>(context) {
    override fun handleInternal(command: KeyboardShortcutCommand) {
        command.shortcut?.let {
            context.keyboardManager.sendKeystroke(it)
        }
    }
}