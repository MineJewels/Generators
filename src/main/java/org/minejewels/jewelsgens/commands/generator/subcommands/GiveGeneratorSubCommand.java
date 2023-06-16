package org.minejewels.jewelsgens.commands.generator.subcommands;

import net.abyssdev.abysslib.command.AbyssSubCommand;
import net.abyssdev.abysslib.command.context.CommandContext;
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

public class GiveGeneratorSubCommand extends AbyssSubCommand<JewelsGens> {

    public GiveGeneratorSubCommand(final JewelsGens plugin) {
        super(plugin, 3, Sets.immutable.of("give"));
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

        final Optional<Generator> optionalGenerator = this.plugin.getGeneratorRegistry().get(context.asString(1).toUpperCase());

        if (!optionalGenerator.isPresent()) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.generator-doesnt-exist", new PlaceholderReplacer().addPlaceholder("%type%", WordUtils.formatText(context.asString(1).replace("_", " "))));
            return;
        }

        final Generator generator = optionalGenerator.get();

        if (!Utils.isInteger(String.valueOf(context.asInt(2)))) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.invalid-number");
            return;
        }

        final int amount = context.asInt(2);

        if (amount <= 0) {
            this.plugin.getMessageCache().sendMessage(sender, "messages.invalid-number");
            return;
        }

        final ItemStack item = generator.getItem();

        item.setAmount(amount);

        player.getInventory().addItem(item);

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-given", new PlaceholderReplacer().addPlaceholder("%amount%", Utils.format(amount)).addPlaceholder("%type%", WordUtils.formatText(generator.getIdentifier().toLowerCase().replace("_", " "))));
    }
}
