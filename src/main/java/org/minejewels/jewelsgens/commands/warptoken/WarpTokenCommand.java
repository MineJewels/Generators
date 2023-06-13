package org.minejewels.jewelsgens.commands.warptoken;

import net.abyssdev.abysslib.command.AbyssCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
import org.bukkit.command.CommandSender;
import org.eclipse.collections.api.factory.Lists;
import org.minejewels.jewelsgens.JewelsGens;

public class WarpTokenCommand extends AbyssCommand<JewelsGens, CommandSender> {

    public WarpTokenCommand(final JewelsGens plugin) {
        super(
                plugin,
                "warptoken",
                "The main warp token command!",
                Lists.mutable.of("timewarp", "timewarptoken", "genwarp"),
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
