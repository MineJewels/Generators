package org.minejewels.jewelsgens.listeners;

import com.plotsquared.core.PlotSquared;
import com.plotsquared.core.plot.Plot;
import com.plotsquared.core.plot.PlotArea;
import com.sun.tools.javac.jvm.Gen;
import net.abyssdev.abysslib.economy.provider.Economy;
import net.abyssdev.abysslib.economy.registry.impl.DefaultEconomyRegistry;
import net.abyssdev.abysslib.listener.AbyssListener;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.nbt.NBTUtils;
import net.abyssdev.abysslib.placeholder.PlaceholderReplacer;
import net.abyssdev.abysslib.plotsquared.PlotSquaredUtils;
import net.abyssdev.abysslib.team.utils.TeamUtils;
import net.abyssdev.abysslib.utils.Utils;
import net.abyssdev.abysslib.utils.WordUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.minejewels.jewelsgens.JewelsGens;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;

import java.util.ArrayList;
import java.util.List;

public class InteractListener extends AbyssListener<JewelsGens> {

    public InteractListener(final JewelsGens plugin) {
        super(plugin);
    }

    @EventHandler
    public void onInteract(final PlayerInteractEvent event) {

        if (event.getClickedBlock() == null) return;

        final Location location = event.getClickedBlock().getLocation();

        if (this.plugin.getGeneratorStorage().allValues().isEmpty()) return;

        final List<GeneratorData> generatorDataList = new ArrayList<>(this.plugin.getGeneratorStorage().cache().values());

        for (final GeneratorData generatorData : generatorDataList) {
            if (!generatorData.getLocation().equalsIgnoreCase(LocationSerializer.serialize(location))) continue;
            if (!this.plugin.getGeneratorRegistry().containsKey(generatorData.getGenerator())) continue;

            if (!TeamUtils.get().canInteract(event.getPlayer(), location)) {
                this.plugin.getMessageCache().sendMessage(event.getPlayer(), "messages.cannot-interact");
                return;
            }

            if (Bukkit.getPluginManager().getPlugin("PlotSquared") != null) {

                final com.plotsquared.core.location.Location plotLocation = com.plotsquared.core.location.Location.at(location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());

                if (PlotSquared.get().getPlotAreaManager().getApplicablePlotArea(plotLocation).getPlot(plotLocation) == null) {
                    this.plugin.getMessageCache().sendMessage(event.getPlayer(), "messages.cannot-interact");
                    return;
                }

                final Plot plot = PlotSquared.get().getPlotAreaManager().getApplicablePlotArea(plotLocation).getPlot(plotLocation);

                if (plot == null) {
                    this.plugin.getMessageCache().sendMessage(event.getPlayer(), "messages.cannot-interact");
                    return;
                }

                if (!plot.getMembers().contains(event.getPlayer().getUniqueId()) && !plot.getOwners().contains(event.getPlayer().getUniqueId()) && !plot.getTrusted().contains(event.getPlayer().getUniqueId())) {
                    this.plugin.getMessageCache().sendMessage(event.getPlayer(), "messages.cannot-interact");
                    return;
                }
            }

            final Generator generator = this.plugin.getGeneratorRegistry().get(generatorData.getGenerator()).get();

            event.setCancelled(true);

            this.attemptBreak(event, generator, generatorData);
            this.attemptToken(event, generator, generatorData);
            this.attemptUpgrade(event, generator, generatorData);
        }
    }

    private void attemptToken(final PlayerInteractEvent event, final Generator generator, final GeneratorData data) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (player.getItemInHand().getType() == Material.AIR) return;

        final ItemStack item = player.getItemInHand();

        if (!NBTUtils.get().contains(item, "TIMEWARP_TOKEN")) return;

        final int minutes = Integer.parseInt(NBTUtils.get().getString(item, "TIMEWARP_TOKEN"));

        final int seconds = minutes * 60;
        final int procTimes = seconds/generator.getGenerationSpeed();

        final int procAmount = procTimes * generator.getGenerationQuantity();

        Utils.removeItemsFromHand(player, 1, true);

        final Location dropLocation = LocationSerializer.deserialize(data.getLocation()).add(0, 1, 0);
        final ItemStack dropItem = new ItemStack(Material.getMaterial(generator.getIdentifier().toUpperCase()));

        dropItem.setAmount(procAmount);

        dropLocation.getWorld().dropItemNaturally(dropLocation, dropItem);

        this.plugin.getMessageCache().sendMessage(player, "messages.used-token", new PlaceholderReplacer().addPlaceholder("%amount%", Utils.format(procAmount)));
    }

    private void attemptUpgrade(final PlayerInteractEvent event, final Generator generator, final GeneratorData data) {

        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

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

    private void attemptBreak(final PlayerInteractEvent event, final Generator generator, final GeneratorData data) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        final Player player = event.getPlayer();

        if (player.isSneaking()) return;
        if (player.getItemInHand().getType() != Material.AIR) return;

        generator.despawnGenerator(data);

        this.plugin.getGeneratorStorage().remove(data.getUuid());

        this.plugin.getMessageCache().sendMessage(player, "messages.generator-broken");
        player.getInventory().addItem(generator.getItem());

        final Location location = LocationSerializer.deserialize(data.getLocation());

        location.getBlock().setType(Material.AIR);
    }
}
