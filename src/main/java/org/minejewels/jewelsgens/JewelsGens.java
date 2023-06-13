package org.minejewels.jewelsgens;

import eu.decentsoftware.holograms.api.DHAPI;
import lombok.Getter;
import net.abyssdev.abysslib.command.AbyssCommand;
import net.abyssdev.abysslib.config.AbyssConfig;
import net.abyssdev.abysslib.location.LocationSerializer;
import net.abyssdev.abysslib.patterns.registry.Registry;
import net.abyssdev.abysslib.plugin.AbyssPlugin;
import net.abyssdev.abysslib.runnable.AbyssTask;
import net.abyssdev.abysslib.storage.Storage;
import net.abyssdev.abysslib.text.MessageCache;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.minejewels.jewelsgens.commands.generator.GeneratorCommand;
import org.minejewels.jewelsgens.commands.generator.subcommands.GiveGeneratorSubCommand;
import org.minejewels.jewelsgens.commands.warptoken.WarpTokenCommand;
import org.minejewels.jewelsgens.commands.warptoken.subcommands.GiveTokenSubCommand;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;
import org.minejewels.jewelsgens.gen.registry.GeneratorRegistry;
import org.minejewels.jewelsgens.gen.storage.GeneratorStorage;
import org.minejewels.jewelsgens.gen.task.GeneratorTask;
import org.minejewels.jewelsgens.listeners.BreakListener;
import org.minejewels.jewelsgens.listeners.InteractListener;
import org.minejewels.jewelsgens.listeners.PlaceListener;

import java.util.UUID;

@Getter
public final class JewelsGens extends AbyssPlugin {

    private static JewelsGens api;

    private final AbyssConfig langConfig = this.getAbyssConfig("lang");
    private final AbyssConfig generatorsConfig = this.getAbyssConfig("generators");
    private final AbyssConfig settingsConfig = this.getAbyssConfig("settings");

    private final MessageCache messageCache = new MessageCache(langConfig);

    private final Registry<String, Generator> generatorRegistry = new GeneratorRegistry();
    private final Storage<UUID, GeneratorData> generatorStorage = new GeneratorStorage(this);

    private final AbyssCommand<JewelsGens, CommandSender> generatorCommand = new GeneratorCommand(this);
    private final AbyssCommand<JewelsGens, CommandSender> warptokenCommand = new WarpTokenCommand(this);

    @Override
    public void onEnable() {
        JewelsGens.api = this;

        this.loadMessages(this.messageCache, langConfig);
        this.loadGenerators();

        new PlaceListener(this);
        new InteractListener(this);
        new BreakListener(this);

        this.generatorCommand.register();
        this.generatorCommand.register(new GiveGeneratorSubCommand(this));

        this.warptokenCommand.register();
        this.warptokenCommand.register(new GiveTokenSubCommand(this));
    }

    @Override
    public void onDisable() {
        this.generatorStorage.write();
    }

    private void loadGenerators() {
        for (final String key : this.generatorsConfig.getSectionKeys("generator")) {
            this.generatorRegistry.register(key.toUpperCase(), new Generator(this.generatorsConfig, key));

            System.out.println("Successfully registered " + key.toUpperCase() + " generator!");
        }

        for (final GeneratorData generatorData : this.generatorStorage.allValues()) {

            final Generator generator = this.generatorRegistry.get(generatorData.getGenerator()).get();
            generatorData.setTask(new GeneratorTask(this, generatorData));

            new AbyssTask<JewelsGens>(this) {
                @Override
                public void run() {
                    final Location newLocation = LocationSerializer.deserialize(generatorData.getLocation()).add(0.5, generator.getYOffset(), 0.5);
                    DHAPI.createHologram(generatorData.getUuid().toString() + "-GENERATOR", newLocation, false, generator.getHologram());
                }
            }.runTaskLater(this, 100L);
        }
    }

    public static JewelsGens get() {
        return JewelsGens.api;
    }
}
