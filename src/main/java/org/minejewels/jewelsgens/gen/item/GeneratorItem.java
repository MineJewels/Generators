package org.minejewels.jewelsgens.gen.item;

import lombok.Data;
import org.bukkit.inventory.ItemStack;

@Data
public class GeneratorItem {

    private final ItemStack item;
    private final int amount;

}
