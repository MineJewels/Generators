package org.minejewels.jewelsgens.commands.warptoken.subcommands;

import net.abyssdev.abysslib.builders.ItemBuilder;
import net.abyssdev.abysslib.command.AbyssSubCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.eclipse.collections.api.factory.Sets;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.gen.Generator;

import java.util.Optional;

public class GiveTokenSubCommand extends AbyssSubCommand<JewelsGens> {

    private final ItemBuilder tokenItem;

    public GiveTokenSubCommand(final JewelsGens plugin) {
        super(plugin, 2, Sets.immutable.of("give"));

        this.tokenItem = plugin.getSettingsConfig().getItemBuilder("timewarp-token");
    }

    @Override
    public void execute(final CommandContext<?> context) {
        final CommandSender sender = context.getSender();

        if (!sender.hasPermission("generators.admin")) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.no-permission");
            return;
        }

        final Optional<Player> optionalPlayer = Optional.ofNullable(context.asPlayer(0));

        if (!optionalPlayer.isPresent()) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.player-doesnt-exist");
            return;
        }

        final Player player = optionalPlayer.get();

        if (!Utils.isInteger(String.valueOf(context.asInt(1)))) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.invalid-number");
            return;
        }

        final int minutes = context.asInt(1);

        final PlaceholderReplacer replacer = new PlaceholderReplacer().addPlaceholder("%time%", Utils.format(minutes));

        final ItemStack item = NBTUtils.get().setString(
                this.tokenItem.parse(replacer),
                "TIMEWARP_TOKEN",
                String.valueOf(minutes)
        );

        player.getInventory().addItem(item);

        this.plugin.getMessageCache().sendMessage(player, "messages.timewarp-token-given", replacer);
    }
}
