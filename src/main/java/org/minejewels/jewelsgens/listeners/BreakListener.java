package org.minejewels.jewelsgens.listeners;

import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.gen.data.GeneratorData;

import java.util.List;

public class BreakListener extends AbyssListener<JewelsGens> {

    public BreakListener(final JewelsGens plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBreak(final BlockBreakEvent event) {

        final Location location = event.getBlock().getLocation();

        for (final GeneratorData generatorData : this.plugin.getGeneratorStorage().allValues()) {
            if (!generatorData.getLocation().equalsIgnoreCase(LocationSerializer.serialize(location))) continue;
            if (!this.plugin.getGeneratorRegistry().containsKey(generatorData.getGenerator())) continue;

            event.setCancelled(true);
        }
    }
}
