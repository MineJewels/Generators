package org.minejewels.jewelsgens.listeners;

import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;

import java.util.List;
import java.util.UUID;

public class PlaceListener extends AbyssListener<JewelsGens> {

    private final List<String> worlds;

    public PlaceListener(final JewelsGens plugin) {
        super(plugin);

        this.worlds = plugin.getSettingsConfig().getStringList("whitelisted-worlds");
    }

    @EventHandler
    public void onPlace(final BlockPlaceEvent event) {

        final Player player = event.getPlayer();
        final ItemStack item = event.getItemInHand();

        if (!NBTUtils.get().contains(item, "GENERATOR")) return;

        final String generatorType = NBTUtils.get().getString(item, "GENERATOR");

        if (!this.plugin.getGeneratorRegistry().containsKey(generatorType)) {
            this.plugin.getMessageCache().sendMessage(player, "messages.generator-doesnt-exist", new PlaceholderReplacer().addPlaceholder("%type%", WordUtils.formatText(generatorType.replace("_", " "))));
            Utils.removeItemsFromHand(player, 1, false);
            event.setCancelled(true);
            return;
        }

        final Block block = event.getBlockPlaced();
        final Location location = block.getLocation();

        if (!this.worlds.contains(location.getWorld().getName())) {
            this.plugin.getMessageCache().sendMessage(player, "messages.blacklisted-world");
            event.setCancelled(true);
            return;
        }

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-placed", new PlaceholderReplacer().addPlaceholder("%type%", WordUtils.formatText(generatorType.toLowerCase().replace("_", " "))));

        final Generator generator = this.plugin.getGeneratorRegistry().get(generatorType).get();
        final GeneratorData generatorData = new GeneratorData(UUID.randomUUID(), LocationSerializer.serialize(location));

        generatorData.setGenerator(generatorType);
        generatorData.setTask(new GeneratorTask(this.plugin, generatorData));

        generator.spawnGenerator(generatorData);

        this.plugin.getGeneratorStorage().cache().register(generatorData.getUuid(), generatorData);
    }
}
