package org.minejewels.jewelsgens.gen.task;

import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.runnable.AbyssTask;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.events.GeneratorItemGenerateEvent;
import org.minejewels.jewelsgens.events.GeneratorPlaceEvent;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;

public class GeneratorTask extends AbyssTask<JewelsGens> {

    private final GeneratorData generatorData;
    private final Generator generator;

    public GeneratorTask(final JewelsGens plugin, final GeneratorData generatorData) {
        super(plugin);

        this.generatorData = generatorData;
        this.generator = plugin.getGeneratorRegistry().get(generatorData.getGenerator()).get();

        this.runTaskTimer(plugin, 20L, generator.getGenerationSpeed() * 20L);
    }

    @Override
    public void run() {

        if (!this.plugin.getGeneratorStorage().contains(this.generatorData.getUuid())) {
            this.cancel();
            return;
        }

        final Location location = LocationSerializer.deserialize(this.generatorData.getLocation()).add(0, 1, 0);

        final GeneratorItemGenerateEvent generateEvent = new GeneratorItemGenerateEvent(location, generator, generator.getGeneratedItem());

        Events.call(generateEvent);

        if (generateEvent.isCancelled()) return;

        final ItemStack dropItem = NBTUtils.get().setString(this.generator.getGeneratedItem().getItem(), "GENERATOR-LOOT", generator.getIdentifier().toUpperCase());

        dropItem.setAmount(this.generator.getGeneratedItem().getAmount());

        location.getWorld().dropItemNaturally(location, dropItem);
    }
}
