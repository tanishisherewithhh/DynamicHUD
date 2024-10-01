package com.tanishisherewith.dynamichud.utils.contextmenu;

import com.tanishisherewith.dynamichud.DynamicHUD;
import com.tanishisherewith.dynamichud.helpers.DrawHelper;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.ContextMenuScreen;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.ContextMenuScreenFactory;
import com.tanishisherewith.dynamichud.utils.contextmenu.contextmenuscreen.DefaultContextMenuScreenFactory;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.math.MathHelper;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ContextMenu {

    //The properties of a context menu
    protected final ContextMenuProperties properties;

    private final List<Option<?>> options = new ArrayList<>(); // The list of options in the context menu
    public int x, y;
    // Width is counted while the options are being rendered.
    // FinalWidth is the width at the end of the count.
    private int width = 0;
    private int finalWidth = 0;
    private int height = 0, widgetHeight = 0;
    //Todo: Add padding around the rectangle instead of just one side.
    private boolean shouldDisplay = false;
    private float scale = 0.0f;
    public final Color darkerBackgroundColor;
    private Screen parentScreen = null;
    private final ContextMenuScreenFactory screenFactory;
    private boolean newScreenFlag = false;

    public ContextMenu(int x, int y, ContextMenuProperties properties) {
        this(x, y, properties, new DefaultContextMenuScreenFactory());
    }

    public ContextMenu(int x, int y, ContextMenuProperties properties, ContextMenuScreenFactory screenFactory) {
        this.x = x;
        this.y = y + properties.getHeightOffset();
        this.properties = properties;
        this.screenFactory = screenFactory;
        darkerBackgroundColor = properties.getBackgroundColor().darker().darker().darker().darker().darker().darker();
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
        properties.getSkin().renderContextMenu(drawContext,this,mouseX,mouseY);

        DrawHelper.stopScaling(drawContext.getMatrices());
    }
    public boolean isMouseOver(int mouseX, int mouseY, int x, int y, int width, int height){
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public void update() {
        if(!properties.enableAnimations()){
            scale = 1.0f;
            return;
        }

        // Update the scale
        if(shouldDisplay){
           scale += 0.1f;
        } else{
           scale -= 0.1f;
        }

        scale = MathHelper.clamp(scale,0,1.0f);
    }

    public void close() {
        if(properties.getSkin().shouldCreateNewScreen() && scale <= 0 && parentScreen != null){
            shouldDisplay = false;
            newScreenFlag = false;
            DynamicHUD.MC.setScreen(parentScreen);
        }
        for(Option<?> option: options){
            option.onClose();
        }
        shouldDisplay = false;
        newScreenFlag = false;
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

    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.mouseClicked(mouseX, mouseY, button);
        }
        properties.getSkin().mouseClicked(this,mouseX,mouseY,button);
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.mouseReleased(mouseX, mouseY, button);
        }
        properties.getSkin().mouseReleased(this,mouseX,mouseY,button);
    }

    public void mouseDragged(double mouseX, double mouseY, int button,double deltaX, double deltaY) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.mouseDragged(mouseX, mouseY, button,deltaX,deltaY);
        }
        properties.getSkin().mouseDragged(this,mouseX,mouseY,button,deltaX,deltaY);
    }

    public void keyPressed(int key, int scanCode, int modifiers) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.keyPressed(key);
        }

        properties.getSkin().keyPressed(this,key,scanCode,modifiers);
    }

    public void keyReleased(int key, int scanCode, int modifiers) {
        if (!shouldDisplay) return;
        for (Option<?> option : options) {
            option.keyReleased(key);
        }
        properties.getSkin().keyReleased(this,key,scanCode,modifiers);

    }

    public void mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for(Option<?> option: options){
            option.mouseScrolled(mouseX,mouseY,horizontalAmount,verticalAmount);
        }
        properties.getSkin().mouseScrolled(this,mouseX,mouseY,horizontalAmount,verticalAmount);
    }

    public void set(int x,int y, int widgetHeight){
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

    public int getFinalWidth() {
        return finalWidth;
    }
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setFinalWidth(int finalWidth) {
        this.finalWidth = finalWidth;
    }

    public ContextMenuProperties getProperties() {
        return properties;
    }
    public void setWidgetHeight(int widgetHeight) {
        this.widgetHeight = widgetHeight;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public float getScale() {
        return scale;
    }

    public void setVisible(boolean shouldDisplay) {
        this.shouldDisplay = shouldDisplay;
    }

    public boolean isVisible() {
        return shouldDisplay;
    }
}
