package com.tanishisherewith.dynamichud.screens;

import com.tanishisherewith.dynamichud.config.GlobalConfig;
import com.tanishisherewith.dynamichud.widget.Widget;
import com.tanishisherewith.dynamichud.widget.WidgetRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public abstract class AbstractMoveableScreen extends Screen {
    public final WidgetRenderer widgetRenderer;
    public int snapSize = 100;
    /**
     * Constructs a AbstractMoveableScreen object.
     */
    public AbstractMoveableScreen(Text title, WidgetRenderer renderer) {
        super(title);
        this.widgetRenderer = renderer;
    }

    @Override
    public void onDisplayed() {
        super.onDisplayed();
        widgetRenderer.isInEditor = true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        widgetRenderer.mouseDragged(mouseX, mouseY, button, snapSize);
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(widgetRenderer.mouseClicked(mouseX, mouseY, button)){
            handleClickOnWidget(widgetRenderer.selectedWidget,mouseX,mouseY,button);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        widgetRenderer.mouseReleased(mouseX, mouseY, button);
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        widgetRenderer.keyPressed(keyCode);
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        widgetRenderer.keyReleased(keyCode);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        widgetRenderer.mouseScrolled(mouseX, mouseY, verticalAmount, horizontalAmount);
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    /**
     * Renders this screen and its widgets on the screen.
     *
     * @param drawContext The matrix stack used for rendering
     * @param mouseX      The current x position of the mouse cursor
     * @param mouseY      The current y position of the mouse cursor
     * @param delta       The time elapsed since the last frame in seconds
     */
    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        assert this.client != null;
        if (this.client.world == null) {
            this.renderBackgroundTexture(drawContext);
        }
        drawContext.drawText(client.textRenderer,title,client.getWindow().getScaledWidth()/2 - client.textRenderer.getWidth(title.getString())/2,textRenderer.fontHeight/2,-1,true);

        // Draw each widget
        widgetRenderer.renderWidgets(drawContext, mouseX, mouseY);

        if(widgetRenderer.selectedWidget != null && GlobalConfig.get().shouldDisplayDescriptions() && widgetRenderer.selectedWidget.DATA.description() != null){
            drawContext.drawTooltip(client.textRenderer,Text.of(widgetRenderer.selectedWidget.DATA.description()),mouseX,mouseY);
        }
    }
    public void handleClickOnWidget(Widget widget, double mouseX, double mouseY, int button){

    }

    @Override
    public void close() {
        widgetRenderer.isInEditor = false;
        widgetRenderer.onCloseScreen();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    public void setSnapSize(int size) {
        this.snapSize = size;
    }
}

