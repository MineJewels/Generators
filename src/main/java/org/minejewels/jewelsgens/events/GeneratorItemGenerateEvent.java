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
import org.minejewels.jewelsgens.gen.item.GeneratorItem;

@Getter
@Setter
public class GeneratorItemGenerateEvent extends Event implements Cancellable {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Location location;
    private final Generator generator;
    private final GeneratorItem item;
    private boolean cancelled;

    public GeneratorItemGenerateEvent(final Location location, final Generator generator, final GeneratorItem item) {
        this.location = location;
        this.generator = generator;
        this.item = item;
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
