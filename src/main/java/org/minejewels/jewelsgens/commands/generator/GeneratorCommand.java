package org.minejewels.jewelsgens.commands.generator;

import net.abyssdev.abysslib.command.AbyssCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.eclipse.collections.api.factory.Lists;
import org.minejewels.jewelsgens.JewelsGens;

public class GeneratorCommand extends AbyssCommand<JewelsGens, CommandSender> {

    public GeneratorCommand(final JewelsGens plugin) {
        super(
                plugin,
                "generator",
                "The main generator command!",
                Lists.mutable.of("gen", "generators", "gens"),
                CommandSender.class
        );
    }

    @Override
    public void execute(final CommandContext<CommandSender> context) {
        final CommandSender sender = context.getSender();

        if (!sender.hasPermission("generators.admin")) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.no-permission");
            return;
        }

        this.plugin.getMessageCache().sendMessage(sender, "messages.help");
    }
}
