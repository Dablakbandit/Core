package me.dablakbandit.core.command.config.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CommandBase {
	//@formatter:off
	String[] value() default {
		"This config file handles all commands for this plugin allowing all commands to be dynamically changed",
		"",
		"Command/Argument Node parameters",
		"Required: Command",
		"Optional: Permission, Aliases, Info, Cooldown",
		"",
		"Command node structure is:",
		"Base: | Changing this node does nothing",
		"  Command: <command> 			| Changing this changes the command/argument for this node",
		"  Permission: <permission> 	| Adds permission to this command/argument",
		"  Aliases: 					| Add direct command alias to this node",
		"  - alias1",
		"  - alias2",
		"  Info: 						| Add typeahead's for this command",
		"  - typeahead1",
		"  - typeahead2",
		"  Enabled: <true/false>		| Enable/disable this command",
		"  Cooldown: <seconds>			| Not yet implemented",
	};
	//@formatter:on
}
