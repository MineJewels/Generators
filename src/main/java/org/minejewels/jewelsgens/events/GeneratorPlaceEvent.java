package org.minejewels.jewelsgens.events;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.minejewels.jewelsgens.gen.Generator;
import org.minejewels.jewelsgens.gen.data.GeneratorData;

@Getter
@Setter
public class GeneratorPlaceEvent extends Event implements Cancellable {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Location location;
    private final Generator generator;
    private final GeneratorData generatorData;
    private boolean cancelled;

    public GeneratorPlaceEvent(final Player player, final Location location, final Generator generator, final GeneratorData generatorData) {
        this.player = player;
        this.location = location;
        this.generator = generator;
        this.generatorData = generatorData;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
