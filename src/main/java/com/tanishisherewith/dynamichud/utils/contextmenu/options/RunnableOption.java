package com.tanishisherewith.dynamichud.utils.contextmenu.options;

import com.tanishisherewith.dynamichud.utils.BooleanPool;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class RunnableOption extends Option<Boolean> {
    private final Runnable task;
    public String name = "Empty";

    /**
     * Runnable option which runs a task when clicked on it.
     *
     * @param name   The name to be displayed
     * @param getter Get a default value to run the task by default
     * @param setter Return a boolean based on if the task is running or not.
     * @param task   The task to run
     */
    public RunnableOption(String name, Supplier<Boolean> getter, Consumer<Boolean> setter, Runnable task) {
        super(getter, setter);
        this.name = "Run: " + name; // prepend the "run" symbol to the name
        this.task = task;
        this.renderer.init(this);
    }

    public RunnableOption(String name, boolean defaultValue, Runnable task) {
        this(name, () -> BooleanPool.get(name), value -> BooleanPool.put(name, value), task);
        BooleanPool.put(name, defaultValue);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            value = !value;
            set(value);
            if (value) {
                task.run();
            }
        }
        return true;
    }
}
