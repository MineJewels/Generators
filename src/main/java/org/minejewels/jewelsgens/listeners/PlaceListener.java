package org.minejewels.jewelsgens.listeners;

import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.events.GeneratorPlaceEvent;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;
import org.minejewels.jewelsrealms.JewelsRealms;
import org.minejewels.jewelsrealms.events.RealmPlaceEvent;
import org.minejewels.jewelsrealms.permission.RealmPermission;

import java.util.List;
import java.util.UUID;

public class PlaceListener extends AbyssListener<JewelsGens> {

    private final JewelsRealms realms;

    public PlaceListener(final JewelsGens plugin) {
        super(plugin);
        this.realms = JewelsRealms.get();
    }

    @EventHandler
    public void onPlace(final RealmPlaceEvent event) {

        final Player player = event.getPlayer();
        final ItemStack item = event.getEvent().getItemInHand();

        if (event.isCancelled()) return;

        if (!NBTUtils.get().contains(item, "GENERATOR")) return;

        final String generatorType = NBTUtils.get().getString(item, "GENERATOR");

        if (!this.plugin.getGeneratorRegistry().containsKey(generatorType)) {
            this.plugin.getMessageCache().sendMessage(player, "messages.generator-doesnt-exist", new PlaceholderReplacer().addPlaceholder("%type%", WordUtils.formatText(generatorType.replace("_", " "))));
            Utils.removeItemsFromHand(player, 1, false);
            event.setCancelled(true);
            return;
        }

        final Block block = event.getEvent().getBlockPlaced();
        final Location location = block.getLocation();

        final Generator generator = this.plugin.getGeneratorRegistry().get(generatorType).get();
        final GeneratorData generatorData = new GeneratorData(UUID.randomUUID(), LocationSerializer.serialize(location));

        final GeneratorPlaceEvent placeEvent = new GeneratorPlaceEvent(player, location, generator, generatorData);

        Events.call(placeEvent);

        if (placeEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        generatorData.setGenerator(generatorType);
        generatorData.setTask(new GeneratorTask(this.plugin, generatorData));

        generator.spawnGenerator(generatorData);

        this.plugin.getGeneratorStorage().cache().register(generatorData.getUuid(), generatorData);

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-placed", new PlaceholderReplacer().addPlaceholder("%type%", WordUtils.formatText(generatorType.toLowerCase().replace("_", " "))));
    }
}
