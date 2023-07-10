package org.minejewels.jewelsgens.listeners;

import net.abyssdev.abysslib.economy.provider.Economy;
import net.abyssdev.abysslib.economy.registry.impl.DefaultEconomyRegistry;
import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import net.abyssdev.me.lucko.helper.Events;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.events.GeneratorBreakEvent;
import org.minejewels.jewelsgens.events.GeneratorUpgradeEvent;
import org.minejewels.jewelsgens.events.TimewarpEvent;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;
import org.minejewels.jewelsrealms.events.RealmInteractEvent;

import java.util.ArrayList;
import java.util.List;

public class InteractListener extends AbyssListener<JewelsGens> {

    public InteractListener(final JewelsGens plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInteract(final RealmInteractEvent event) {

        if(event.getEvent().getHand() != EquipmentSlot.HAND) return;
        if (event.getEvent().getClickedBlock() == null) return;

        final Location location = event.getEvent().getClickedBlock().getLocation();

        if (this.plugin.getGeneratorStorage().allValues().isEmpty()) return;

        final List<GeneratorData> generatorDataList = new ArrayList<>(this.plugin.getGeneratorStorage().cache().values());

        for (final GeneratorData generatorData : generatorDataList) {
            if (!generatorData.getLocation().equalsIgnoreCase(LocationSerializer.serialize(location))) continue;
            if (!this.plugin.getGeneratorRegistry().containsKey(generatorData.getGenerator())) continue;

            final Generator generator = this.plugin.getGeneratorRegistry().get(generatorData.getGenerator()).get();

            event.setCancelled(true);

            this.attemptBreak(event, generator, generatorData);
            this.attemptToken(event, generator, generatorData);
            this.attemptUpgrade(event, generator, generatorData);
        }
    }

    private void attemptToken(final RealmInteractEvent event, final Generator generator, final GeneratorData data) {

        if (event.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (player.getItemInHand().getType() == Material.AIR) return;

        final ItemStack item = player.getItemInHand();

        if (!NBTUtils.get().contains(item, "TIMEWARP_TOKEN")) return;

        final int minutes = Integer.parseInt(NBTUtils.get().getString(item, "TIMEWARP_TOKEN"));

        final TimewarpEvent timewarpEvent = new TimewarpEvent(player, minutes, generator);

        Events.call(timewarpEvent);

        if (timewarpEvent.isCancelled()) return;

        final int seconds = minutes * 60;
        final int procTimes = seconds/generator.getGenerationSpeed();

        final int procAmount = procTimes * generator.getGeneratedItem().getAmount();

        Utils.removeItemsFromHand(player, 1, true);

        final Location dropLocation = LocationSerializer.deserialize(data.getLocation()).add(0, 1, 0);
        final ItemStack dropItem = generator.getGeneratedItem().getItem();

        dropItem.setAmount(procAmount);

        dropLocation.getWorld().dropItemNaturally(dropLocation, dropItem);

        this.plugin.getMessageCache().sendMessage(player, "messages.used-token", new PlaceholderReplacer().addPlaceholder("%amount%", Utils.format(procAmount)));
    }

    private void attemptUpgrade(final RealmInteractEvent event, final Generator generator, final GeneratorData data) {

        if (event.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (!player.isSneaking()) return;

        if (!generator.isUpgradeEnabled()) {
            this.plugin.getMessageCache().sendMessage(player, "messages.generator-max-upgraded");
            return;
        }

        final Economy economy = DefaultEconomyRegistry.get().getEconomy("VAULT");

        if (!economy.hasBalance(player, generator.getUpgradeCost())) {
            this.plugin.getMessageCache().sendMessage(player, "messages.not-enough-money", new PlaceholderReplacer().addPlaceholder("%cost%", Utils.format(generator.getUpgradeCost())));
            return;
        }

        final GeneratorUpgradeEvent upgradeEvent = new GeneratorUpgradeEvent(player, LocationSerializer.deserialize(data.getLocation()), generator, generator);

        Events.call(upgradeEvent);

        if (upgradeEvent.isCancelled()) return;

        economy.withdrawBalance(player, generator.getUpgradeCost());

        final Generator newGenerator = this.plugin.getGeneratorRegistry().get(generator.getUpgradeValue()).get();

        generator.upgradeGenerator(data, newGenerator);

        this.plugin.getGeneratorStorage().get(data.getUuid()).setGenerator(newGenerator.getIdentifier());
        this.plugin.getGeneratorStorage().get(data.getUuid()).getTask().cancel();
        this.plugin.getGeneratorStorage().get(data.getUuid()).setTask(new GeneratorTask(plugin, this.plugin.getGeneratorStorage().get(data.getUuid())));

        final Location location = LocationSerializer.deserialize(data.getLocation());

        location.getBlock().setType(Material.getMaterial(newGenerator.getIdentifier().toUpperCase()));

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-upgraded", new PlaceholderReplacer().addPlaceholder("%upgrade%", WordUtils.formatText(newGenerator.getIdentifier().replace("_", " "))));
    }

    private void attemptBreak(final RealmInteractEvent event, final Generator generator, final GeneratorData data) {
        if (event.getEvent().getAction() != Action.LEFT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (player.getItemInHand().getType() != Material.AIR) return;

        final GeneratorBreakEvent breakEvent = new GeneratorBreakEvent(player, LocationSerializer.deserialize(data.getLocation()), generator, data);

        Events.call(breakEvent);

        if (breakEvent.isCancelled()) return;

        generator.despawnGenerator(data);

        this.plugin.getGeneratorStorage().remove(data.getUuid());

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-broken");
        player.getInventory().addItem(generator.getItem());

        final Location location = LocationSerializer.deserialize(data.getLocation());

        location.getBlock().setType(Material.AIR);
    }
}
