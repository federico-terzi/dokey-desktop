package system.commands.handler

import system.context.GeneralContext
import system.commands.annotations.RegisterHandler
import system.commands.general.KeyboardShortcutCommand

@RegisterHandler(commandType = KeyboardShortcutCommand::class)
class KeyboardShortcutCommandHandler(context: GeneralContext) : CommandHandler<KeyboardShortcutCommand>(context) {
    override fun handleInternal(command: KeyboardShortcutCommand) {
        command.shortcut?.let {
            // If the shortcut is associated with an application, open it
            command.app?.let {
                context.applicationManager.openApplication(command.app)
            }

            context.keyboardManager.sendKeystroke(it)
        }
    }
}