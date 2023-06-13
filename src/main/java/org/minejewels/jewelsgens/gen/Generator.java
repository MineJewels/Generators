package org.minejewels.jewelsgens.gen;

import eu.decentsoftware.holograms.api.DHAPI;
import lombok.Data;
import net.abyssdev.abysslib.builders.ItemBuilder;
import net.abyssdev.abysslib.config.AbyssConfig;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.gen.data.GeneratorData;

import java.util.List;

@Data
public class Generator {

    private final String identifier;
    private final boolean upgradeEnabled;
    private String upgradeValue;
    private int upgradeCost;
    private final int generationQuantity, generationSpeed;
    private final double yOffset;
    private final List<String> hologram;
    private final ItemStack item;

    public Generator(final AbyssConfig config, final String identifier) {
        this.identifier = identifier;
        this.upgradeEnabled = config.getBoolean("generator." + identifier + ".upgrade.enabled");
        this.generationSpeed = config.getInt("generator." + identifier + ".generation-speed");
        this.generationQuantity = config.getInt("generator." + identifier + ".generation-quantity");
        this.yOffset = config.getInt("generator." + identifier + ".y-offset");
        this.hologram = config.getColoredStringList("generator." + identifier + ".display-hologram");
        this.item = NBTUtils.get().setString(
                config.getItemBuilder("generator." + identifier + ".item").parse(),
                "GENERATOR",
                identifier.toUpperCase()
        );

        if (this.upgradeEnabled) {
            this.upgradeCost = config.getInt("generator." + identifier + ".upgrade.cost");
            this.upgradeValue = config.getString("generator." + identifier + ".upgrade.value");
        }
    }

    public void spawnGenerator(final GeneratorData data) {
        final Location newLocation = LocationSerializer.deserialize(data.getLocation()).add(0.5, this.yOffset, 0.5);

        DHAPI.createHologram(data.getUuid().toString() + "-GENERATOR", newLocation, false, this.hologram);
    }

    public void despawnGenerator(final GeneratorData data) {
        DHAPI.removeHologram(data.getUuid() + "-GENERATOR");
    }

    public void upgradeGenerator(final GeneratorData oldData, final Generator newGenerator) {
        this.despawnGenerator(oldData);

        final Location newLocation = LocationSerializer.deserialize(oldData.getLocation()).add(0.5, this.yOffset, 0.5);

        DHAPI.createHologram(oldData.getUuid().toString() + "-GENERATOR", newLocation, false, newGenerator.getHologram());
    }
}
