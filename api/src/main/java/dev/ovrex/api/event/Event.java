package dev.ovrex.api.event;

import lombok.Getter;

public abstract class Event {
    @Getter
    private final boolean async;

    protected Event() {
        this(false);
    }

    protected Event(boolean async) {
        this.async = async;
    }
}
