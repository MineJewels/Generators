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

@Getter
@Setter
public class TimewarpEvent extends Event implements Cancellable {

    private final static HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final int minutes;
    private final Generator generator;
    private boolean cancelled;

    public TimewarpEvent(final Player player, final int minutes, final Generator generator) {
        this.player = player;
        this.minutes = minutes;
        this.generator = generator;
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
