package org.minejewels.jewelsgens.gen.task;

import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.runnable.AbyssTask;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
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

        if (!this.plugin.getGeneratorStorage().contains(generatorData.getUuid())) {
            this.cancel();
            return;
        }

        final Location location = LocationSerializer.deserialize(this.generatorData.getLocation()).add(0, 1, 0);
        final ItemStack dropItem = new ItemStack(Material.getMaterial(generator.getIdentifier().toUpperCase()));

        dropItem.setAmount(this.generator.getGenerationQuantity());

        location.getWorld().dropItemNaturally(location, dropItem);
    }
}
