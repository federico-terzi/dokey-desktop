package app.control_panel.dialog.command_edit_dialog.builder.annotation;

import model.command.Command;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RegisterBuilder {
    Class<? extends Command> type();
}
