package com.tanishisherewith.dynamichud.utils.contextmenu.options;

import com.tanishisherewith.dynamichud.DynamicHUD;
import com.tanishisherewith.dynamichud.config.GlobalConfig;
import com.tanishisherewith.dynamichud.utils.Input;
import com.tanishisherewith.dynamichud.utils.contextmenu.ContextMenuProperties;
import com.tanishisherewith.dynamichud.utils.contextmenu.skinsystem.interfaces.SkinRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class Option<T> implements Input {
    public Text name, description = Text.empty();
    public T value = null;
    protected int x, y;
    protected int width = 0;
    protected int height = 0;
    protected Supplier<Boolean> shouldRender;
    protected Supplier<T> getter;
    protected Consumer<T> setter;
    protected T defaultValue = null;
    protected MinecraftClient mc = MinecraftClient.getInstance();
    protected ContextMenuProperties properties;
    protected SkinRenderer<Option<T>> renderer;
    protected Complexity complexity = Complexity.Simple;

    public Option(Text name, Supplier<T> getter, Consumer<T> setter) {
        this(name, getter, setter, () -> true);
    }

    public Option(Text name, Supplier<T> getter, Consumer<T> setter, Supplier<Boolean> shouldRender, ContextMenuProperties properties) {
        this.name = name;
        this.getter = getter;
        this.setter = setter;
        this.shouldRender = shouldRender;
        this.value = get();
        this.defaultValue = get();
        updateProperties(properties);
    }

    public Option(Text name, Supplier<T> getter, Consumer<T> setter, Supplier<Boolean> shouldRender) {
        this(name, getter, setter, shouldRender, ContextMenuProperties.createGenericSimplified());
    }

    public T get() {
        return getter.get();
    }

    public void set(T value) {
        this.value = value;
        setter.accept(value);
    }

    public void updateProperties(ContextMenuProperties properties) {
        this.properties = properties;
        this.renderer = properties.getSkin().getRenderer((Class<Option<T>>) this.getClass());
        if (renderer == null) {
            DynamicHUD.logger.error("Renderer not found for class: {} in the following skin: {}", this.getClass().getName(), properties.getSkin());
            throw new RuntimeException();
        }
    }

    public void render(DrawContext drawContext, int x, int y, int mouseX, int mouseY) {
        this.x = x;
        this.y = y;
        this.value = get();

        // Retrieve the renderer and ensure it is not null
        renderer.render(drawContext, this, x, y, mouseX, mouseY);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return isMouseOver(mouseX, mouseY);
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return isMouseOver(mouseX, mouseY);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return isMouseOver(mouseX, mouseY);
    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
    }

    @Override
    public void charTyped(char c, int modifiers) {
    }

    @Override
    public void keyReleased(int key, int scanCode, int modifiers) {
    }

    /**
     * Called when the context menu closes
     */
    public void onClose() {
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public Option<T> renderWhen(Supplier<Boolean> shouldRender) {
        this.shouldRender = shouldRender;
        return this;
    }

    public boolean shouldRender() {
        return shouldRender.get() && GlobalConfig.get().complexity().ordinal() >= complexity.ordinal();
    }

    public void reset() {
        this.value = get();
    }

    public ContextMenuProperties getProperties() {
        return properties;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
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

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Option<T> description(Text description) {
        this.description = description;
        return this;
    }

    public Option<T> withComplexity(Complexity complexity) {
        this.complexity = complexity;
        return this;
    }

    public SkinRenderer<Option<T>> getRenderer() {
        return renderer;
    }

    public Text getName() {
        return name;
    }

    public Text getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "{" +
                ", name=" + name +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    /**
     * How complex do you think this option is for a daily normal user.
     * For example, some options may be advanced and not needed for casual players but are helpful in customisation
     */
    public enum Complexity {
        Simple,
        Enhanced,
        Pro
    }
}
