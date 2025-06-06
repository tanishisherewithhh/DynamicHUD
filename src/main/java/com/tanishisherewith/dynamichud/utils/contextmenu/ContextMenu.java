package com.tanishisherewith.dynamichud.utils.contextmenu;

import com.tanishisherewith.dynamichud.DynamicHUD;
import com.tanishisherewith.dynamichud.helpers.DrawHelper;
import com.tanishisherewith.dynamichud.internal.System;
import com.tanishisherewith.dynamichud.utils.Input;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.ContextMenuScreenFactory;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.ContextMenuScreenRegistry;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.DefaultContextMenuScreenFactory;
import com.tanishisherewith.dynamichud.utils.contextmenu.options.Option;
import com.tanishisherewith.dynamichud.widget.WidgetBox;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ContextMenu<T extends ContextMenuProperties> implements Input {
    public final Color darkerBackgroundColor;
    //The properties of a context menu
    @NotNull
    protected final T properties;
    protected final List<Option<?>> options = new ArrayList<>(); // The list of options in the context menu
    protected final ContextMenuScreenFactory screenFactory;
    public int x, y;
    // Width is counted while the options are being rendered.
    // FinalWidth is the width at the end of the count.
    protected int width = 0;
    protected int height = 0, widgetHeight = 0;
    protected boolean shouldDisplay = false;
    protected float scale = 0.0f;
    protected Screen parentScreen = null;
    protected boolean newScreenFlag = false;

    @Nullable
    private final ContextMenu<?> parentMenu;

    public ContextMenu(int x, int y, T properties) {
        this(x, y, properties, new DefaultContextMenuScreenFactory());
    }

    public ContextMenu(int x, int y, T properties, ContextMenuScreenFactory screenFactory) {
        this(x, y, properties, screenFactory, null);
    }

    public ContextMenu(int x, int y, @NotNull T properties, ContextMenuScreenFactory screenFactory, @Nullable ContextMenu<?> parentMenu) {
        Objects.requireNonNull(screenFactory, "ContextMenuScreenFactory cannot be null!");
        Objects.requireNonNull(properties, "ContextMenu Properties cannot be null!");

        this.x = x;
        this.y = y + properties.getHeightOffset();
        this.properties = properties;
        this.screenFactory = screenFactory;
        this.darkerBackgroundColor = properties.getBackgroundColor().darker().darker().darker().darker().darker().darker();
        this.parentMenu = parentMenu;
        this.properties.getSkin().setContextMenu(this);

        Screen dummy = screenFactory.create(this, properties);
        System.registerInstance(new ContextMenuScreenRegistry(dummy.getClass()), DynamicHUD.MOD_ID);
    }

    public void addOption(Option<?> option) {
        option.updateProperties(this.getProperties());
        options.add(option);
    }

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        if (newScreenFlag && screenFactory != null) {
            DynamicHUD.MC.setScreen(screenFactory.create(this, properties));
            return;
        }

        this.x = x;
        this.y = y + properties.getHeightOffset() + widgetHeight;
        update();
        if (scale <= 0.0f || newScreenFlag) return;

        DrawHelper.scaleAndPosition(drawContext.getMatrices(), x, y, scale);

        properties.getSkin().setContextMenu(this);
        properties.getSkin().renderContextMenu(drawContext, this, mouseX, mouseY);

        DrawHelper.stopScaling(drawContext.getMatrices());
    }

    public void update() {
        if (!properties.enableAnimations()) {
            scale = 1.0f;
            return;
        }

        // Update the scale
        if (shouldDisplay) {
            scale += 0.1f;
        } else {
            scale -= 0.1f;
        }

        scale = MathHelper.clamp(scale, 0, 1.0f);
    }

    public void close() {
        shouldDisplay = false;
        newScreenFlag = false;
        for (Option<?> option : options) {
            option.onClose();
        }
        if (properties.getSkin().shouldCreateNewScreen() && scale <= 0 && parentScreen != null) {
            DynamicHUD.MC.setScreen(parentScreen);
        }
    }

    public void open() {
        shouldDisplay = true;
        update();
        parentScreen = DynamicHUD.MC.currentScreen;
        if (properties.getSkin().shouldCreateNewScreen()) {
            newScreenFlag = true;
        }
    }

    public void toggleDisplay() {
        if (shouldDisplay) {
            close();
        } else {
            open();
        }
    }

    public void toggleDisplay(WidgetBox widgetBox, double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT && widgetBox.isMouseOver(mouseX, mouseY)) {
            toggleDisplay();
        }
    }

    public void resetAllOptions() {
        for (Option<?> option : options) {
            option.reset();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!shouldDisplay) return false;
        for (Option option : options) {
            option.getRenderer().mouseClicked(option, mouseX, mouseY, button);
        }
        return properties.getSkin().mouseClicked(this, mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!shouldDisplay) return false;
        for (Option option : options) {
            option.getRenderer().mouseReleased(option, mouseX, mouseY, button);
        }
        return properties.getSkin().mouseReleased(this, mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!shouldDisplay) return false;
        for (Option option : options) {
            option.getRenderer().mouseDragged(option, mouseX, mouseY, button, deltaX, deltaY);
        }
        return properties.getSkin().mouseDragged(this, mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (!shouldDisplay) return;
        for (Option option : options) {
            option.getRenderer().keyPressed(option, key, scanCode, modifiers);
        }

        properties.getSkin().keyPressed(this, key, scanCode, modifiers);
    }

    @Override
    public void keyReleased(int key, int scanCode, int modifiers) {
        if (!shouldDisplay) return;
        for (Option option : options) {
            option.getRenderer().keyReleased(option, key, scanCode, modifiers);
        }
        properties.getSkin().keyReleased(this, key, scanCode, modifiers);

    }

    @Override
    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!shouldDisplay) return;
        for (Option option : options) {
            option.getRenderer().mouseScrolled(option, mouseX, mouseY, horizontalAmount, verticalAmount);
        }
        properties.getSkin().mouseScrolled(this, mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void charTyped(char c, int modifiers) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.charTyped(c, modifiers);
        }
    }

    public void set(int x, int y, int widgetHeight) {
        this.x = x;
        this.y = y;
        this.widgetHeight = widgetHeight;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public List<Option<?>> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public @NotNull T getProperties() {
        return properties;
    }

    public void setWidgetHeight(int widgetHeight) {
        this.widgetHeight = widgetHeight;
    }

    @Nullable
    public ContextMenu<?> getParentMenu() {
        return parentMenu;
    }

    public <K extends ContextMenuProperties> ContextMenu<K> createSubMenu(int x, int y, K properties) {
        return new ContextMenu<>(x, y, properties, screenFactory, this);
    }

    public float getScale() {
        return scale;
    }

    public boolean isVisible() {
        return shouldDisplay;
    }

    public void setVisible(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
    }
}
