package com.tanishisherewith.dynamichud.utils.contextmenu.skinsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tanishisherewith.dynamichud.helpers.DrawHelper;
import com.tanishisherewith.dynamichud.utils.contextmenu.ContextMenu;
import com.tanishisherewith.dynamichud.utils.contextmenu.options.*;
import com.tanishisherewith.dynamichud.utils.contextmenu.skinsystem.interfaces.SkinRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

/**
 * This is one of the Skins provided by DynamicHUD featuring the minecraft-like style rendering.
 * It runs on a separate screen and provides more complex features like scrolling and larger dimension.
 * It tries to imitate the minecraft look and provides various form of panel shades {@link PanelColor}
 */
public class MinecraftSkin extends Skin {
    public static final ButtonTextures TEXTURES = new ButtonTextures(
            Identifier.ofVanilla("widget/button"),
            Identifier.ofVanilla("widget/button_disabled"),
            Identifier.ofVanilla("widget/button_highlighted")
    );
    private static final int DEFAULT_SCROLLBAR_WIDTH = 10;
    private static final int DEFAULT_PANEL_WIDTH = 248;
    private static final int DEFAULT_PANEL_HEIGHT = 165;
    private static final Identifier DEFAULT_BACKGROUND_PANEL = Identifier.of("minecraft", "textures/gui/demo_background.png");
    private static final Identifier SCROLLER_TEXTURE = Identifier.of("minecraft","textures/gui/sprites/widget/scroller.png");
    private static final Identifier SCROLL_BAR_BACKGROUND = Identifier.of("minecraft","textures/gui/sprites/widget/scroller_background.png");

    private final Identifier BACKGROUND_PANEL;
    private final int panelWidth;
    private final int panelHeight;
    private final PanelColor panelColor;

    private int imageX, imageY;
    private int scrollOffset = 0;
    private double scrollVelocity = 0;
    private long lastScrollTime = 0;
    private boolean isDragging = false;

    public MinecraftSkin(PanelColor color, Identifier backgroundPanel, int panelWidth, int panelHeight) {
        super();
        this.panelColor = color;
        addRenderer(BooleanOption.class, MinecraftBooleanRenderer::new);
        addRenderer(DoubleOption.class, MinecraftDoubleRenderer::new);
        addRenderer(EnumOption.class, MinecraftEnumRenderer::new);
        addRenderer(ListOption.class, MinecraftListRenderer::new);
        addRenderer(SubMenuOption.class, MinecraftSubMenuRenderer::new);
        addRenderer(RunnableOption.class, MinecraftRunnableRenderer::new);
        addRenderer(ColorOption.class, MinecraftColorOptionRenderer::new);

        this.panelHeight = panelHeight;
        this.panelWidth = panelWidth;
        this.BACKGROUND_PANEL = backgroundPanel;

        setCreateNewScreen(true);
    }

    public MinecraftSkin(PanelColor color) {
        this(color, DEFAULT_BACKGROUND_PANEL, DEFAULT_PANEL_WIDTH, DEFAULT_PANEL_HEIGHT);
    }

    private void enableContextMenuScissor() {
        DrawHelper.enableScissor(imageX, imageY + 3, mc.getWindow().getScaledWidth(), panelHeight - 8);
    }

    @Override
    public void renderContextMenu(DrawContext drawContext, ContextMenu contextMenu, int mouseX, int mouseY) {
        this.contextMenu = contextMenu;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        contextMenu.set(centerX, centerY, 0);

        // Calculate the top-left corner of the image
        imageX = (screenWidth - panelWidth) / 2;
        imageY = (screenHeight - panelHeight) / 2;


        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        panelColor.applyColor();
        drawContext.drawTexture(BACKGROUND_PANEL, imageX, imageY, 0, 0, panelWidth, panelHeight, 256, 254);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawContext.drawGuiTexture(TEXTURES.get(true, isMouseOver(mouseX, mouseY, imageX + 3, imageY + 3, 14, 14)), imageX + 3, imageY + 3, 14, 14);
        drawContext.drawText(mc.textRenderer, "X", imageX + 10 - mc.textRenderer.getWidth("X") / 2, imageY + 6, -1, true);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        this.enableContextMenuScissor();

        int yOffset = imageY + 10 - scrollOffset;
        contextMenu.setWidth(panelWidth - 4);
        contextMenu.y = imageY;
        int maxYOffset = imageY + panelHeight - 4;

        for (Option<?> option : getOptions(contextMenu)) {
            if (!option.shouldRender()) continue;

            if (yOffset >= imageY - 10 && yOffset <= maxYOffset - option.getHeight() + 10) {
                option.render(drawContext, imageX + 4, yOffset, mouseX, mouseY);
            }

            yOffset += option.getHeight() + 1;
        }

        contextMenu.setHeight(yOffset - imageY + 25);

        // Clamp the scroll offset
        applyMomentum();
        scrollOffset = MathHelper.clamp(scrollOffset, 0, Math.max(0, getMaxScrollOffset()));

        drawScrollbar(drawContext);

        // Disable scissor after rendering
        DrawHelper.disableScissor();
    }


    private void drawScrollbar(DrawContext drawContext) {
        if (getMaxScrollOffset() > 0) {
            int scrollbarX = imageX + panelWidth + 20;
            int scrollbarY = imageY;
            int handleHeight = (int) ((float) panelHeight * (panelHeight / (float) getContentHeight()));
            int handleY = scrollbarY + (int) ((float) (panelHeight - handleHeight) * ((float) scrollOffset / getMaxScrollOffset()));

            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();

            drawContext.drawTexture(SCROLL_BAR_BACKGROUND, scrollbarX, scrollbarY,0,0, 10, panelHeight);
            drawContext.drawTexture(SCROLLER_TEXTURE, scrollbarX, handleY,0,0, 10, handleHeight);

            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }
    }
    private int getMaxScrollOffset() {
        return getContentHeight() - panelHeight;
    }

    private int getContentHeight() {
        return contextMenu.getHeight() + 10;
    }

    private void applyMomentum() {
        long currentTime = System.currentTimeMillis();
        double timeDelta = (currentTime - lastScrollTime) / 1000.0;
        scrollOffset += (int) (scrollVelocity * timeDelta);
        scrollVelocity *= 0.9; // Decay factor
        scrollOffset = MathHelper.clamp(scrollOffset, 0, getMaxScrollOffset());
    }

    @Override
    public void mouseScrolled(ContextMenu menu, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollVelocity -= verticalAmount * 10;
        lastScrollTime = System.currentTimeMillis();
    }

    @Override
    public boolean mouseClicked(ContextMenu menu, double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if(isMouseOver(mouseX, mouseY, imageX + 3, imageY + 3, 14, 14)) {
                mc.getSoundManager().play(PositionedSoundInstance.master(
                        SoundEvents.UI_BUTTON_CLICK, 1.0F));

                contextMenu.close();
                return true;
            }
            if (isMouseOver(mouseX, mouseY, imageX + panelWidth + 10, imageY, 10, panelHeight)) {
                isDragging = true;
                return true;
            }
        }

        return super.mouseClicked(menu, mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(ContextMenu menu, double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            isDragging = false;
        }
        return super.mouseReleased(menu, mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(ContextMenu menu,double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (isDragging && button == 0) {
            float scrollPercentage = (float) (mouseY - imageY) / panelHeight;
            scrollOffset = (int) (getMaxScrollOffset() * scrollPercentage);
            scrollOffset = MathHelper.clamp(scrollOffset, 0, getMaxScrollOffset());
            lastScrollTime = System.currentTimeMillis();
            return true;
        }
        return super.mouseDragged(menu,mouseX, mouseY, button, deltaX, deltaY);
    }


    public enum PanelColor {
        COFFEE_BROWN(0.6f, 0.3f, 0.1f, 0.9f),
        CREAMY(1.0f, 0.9f, 0.8f, 0.9f),
        DARK_PANEL(0.2f, 0.2f, 0.2f, 0.9f),
        FOREST_GREEN(0.0f, 0.6f, 0.2f, 0.9f),
        GOLDEN_YELLOW(1.0f, 0.8f, 0.0f, 0.9f),
        LAVENDER(0.8f, 0.6f, 1.0f, 0.9f),
        LIGHT_BLUE(0.6f, 0.8f, 1.0f, 0.9f),
        LIME_GREEN(0.7f, 1.0f, 0.3f, 0.9f),
        MIDNIGHT_PURPLE(0.3f, 0.0f, 0.5f, 0.9f),
        OCEAN_BLUE(0.0f, 0.5f, 1.0f, 0.9f),
        ROSE_PINK(1.0f, 0.4f, 0.6f, 0.9f),
        SKY_BLUE(0.5f, 0.8f, 1.0f, 0.9f),
        SOFT_GREEN(0.6f, 1.0f, 0.6f, 0.9f),
        SUNSET_ORANGE(1.0f, 0.5f, 0.0f, 0.9f),
        WARM_YELLOW(1.0f, 1.0f, 0.6f, 0.9f),
        CUSTOM(0.0f, 0.0f, 0.0f, 0.0f); // PlaceHolder for custom colors

        private float red;
        private float green;
        private float blue;
        private float alpha;

        PanelColor(float red, float green, float blue, float alpha) {
            this.red = red;
            this.green = green;
            this.blue = blue;
            this.alpha = alpha;
        }

        public static PanelColor custom(float red, float green, float blue, float alpha) {
            PanelColor custom = CUSTOM;
            custom.red = red;
            custom.green = green;
            custom.blue = blue;
            custom.alpha = alpha;
            return custom;
        }

        public void applyColor() {
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }
    }

    public class MinecraftBooleanRenderer implements SkinRenderer<BooleanOption> {
        @Override
        public void render(DrawContext drawContext, BooleanOption option, int x, int y, int mouseX, int mouseY) {
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - 75, y);

            int width = 50;
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX, mouseY)), option.getX(), y, width, 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Text text = option.getBooleanType().getText(option.value);
            int color = option.value ? Color.GREEN.getRGB() : Color.RED.getRGB();
            drawContext.drawText(mc.textRenderer, text, (int) (option.getX() + (width / 2.0f) - (mc.textRenderer.getWidth(text) / 2.0f)), y + 5, color, true);

            option.setHeight(25);

            //Widths don't matter in this skin
            option.setWidth(width);
        }

        @Override
        public boolean mouseClicked(BooleanOption option, double mouseX, double mouseY, int button) {
            return SkinRenderer.super.mouseClicked(option, mouseX, mouseY, button);
        }
    }

    public class MinecraftColorOptionRenderer implements SkinRenderer<ColorOption> {
        @Override
        public void render(DrawContext drawContext, ColorOption option, int x, int y, int mouseX, int mouseY) {
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - 45, y);

            int width = 20;
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(!option.isVisible, option.isMouseOver(mouseX, mouseY)), option.getX(), y, width, 20);
            int shadowOpacity = Math.min(option.value.getAlpha(), 45);
            DrawHelper.drawRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(),
                    option.getX() + 4,
                    y + 4,
                    width - 8,
                    20 - 8,
                    option.value.getRGB(),
                    shadowOpacity,
                    1,
                    1);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


            option.setHeight(25);
            option.setWidth(width);

            DrawHelper.disableScissor(); // Disable scissor test for the color-picker
            if (option.getColorGradient().isDisplay()) {
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                panelColor.applyColor();
                int colorGradientWidth = 28 + option.getColorGradient().getBoxSize() + option.getColorGradient().getAlphaSlider().getWidth() + option.getColorGradient().getColorPickerButton().getWidth();
                int colorGradientHeight = 18 + option.getColorGradient().getBoxSize() + option.getColorGradient().getGradientBox().getSize() + option.getColorGradient().getGradientSlider().getHeight();
                drawContext.drawTexture(BACKGROUND_PANEL, x + panelWidth + 3, y - 6, 0, 0, colorGradientWidth, colorGradientHeight, colorGradientWidth, colorGradientHeight);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            }
            option.getColorGradient().render(drawContext, x + panelWidth + 10, y - 10,mouseX,mouseY);

            DrawHelper.enableScissor(imageX, imageY + 2, panelWidth, panelHeight - 4);
        }
    }

    public class MinecraftDoubleRenderer implements SkinRenderer<DoubleOption> {
        private static final Identifier TEXTURE = Identifier.ofVanilla("widget/slider");
        private static final Identifier HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_highlighted");
        private static final Identifier HANDLE_TEXTURE = Identifier.ofVanilla("widget/slider_handle");
        private static final Identifier HANDLE_HIGHLIGHTED_TEXTURE = Identifier.ofVanilla("widget/slider_handle_highlighted");

        @Override
        public void init(DoubleOption option) {
            SkinRenderer.super.init(option);
        }

        @Override
        public void render(DrawContext drawContext, DoubleOption option, int x, int y, int mouseX, int mouseY) {
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25 / 2 - 5, -1, true);

            option.setWidth(panelWidth - 150);
            option.setHeight(25);
            option.setPosition(x + panelWidth - 122, y);

            double sliderX = option.getX() + (option.value - option.minValue) / (option.maxValue - option.minValue) * (option.getWidth() - 8);
            boolean isMouseOverHandle = isMouseOver(mouseX, mouseY, sliderX, y, 10, 20);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.drawGuiTexture(option.isMouseOver(mouseX, mouseY) ? HIGHLIGHTED_TEXTURE : TEXTURE, option.getX(), y, option.getWidth(), 20);
            drawContext.drawGuiTexture(isMouseOverHandle ? HANDLE_HIGHLIGHTED_TEXTURE : HANDLE_TEXTURE, (int) Math.round(sliderX), y, 8, 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            drawContext.drawText(mc.textRenderer, String.valueOf(option.value), option.getX() + option.getWidth() / 2 - mc.textRenderer.getWidth(option.value.toString()) / 2, y + 5, 16777215, false);
        }

        @Override
        public boolean mouseClicked(DoubleOption option, double mouseX, double mouseY, int button) {
            return SkinRenderer.super.mouseClicked(option, mouseX, mouseY, button);
        }
    }

    public class MinecraftEnumRenderer<E extends Enum<E>> implements SkinRenderer<EnumOption<E>> {
        private int maxWidth = 50;

        private void calculateMaxWidth(EnumOption<E> option) {
            for (E enumConstant : option.getValues()) {
                int width = mc.textRenderer.getWidth(enumConstant.name()) + 5;
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
        }

        @Override
        public void render(DrawContext drawContext, EnumOption<E> option, int x, int y, int mouseX, int mouseY) {
            calculateMaxWidth(option);
            option.setHeight(25);
            option.setWidth(maxWidth);

            drawContext.drawText(mc.textRenderer, option.name + ": ", x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - maxWidth - 25, y);

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX, mouseY)), option.getX(), y, maxWidth, 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = option.get().toString();
            drawContext.drawText(mc.textRenderer, text, option.getX() + maxWidth / 2 - mc.textRenderer.getWidth(text) / 2, y + 5, Color.CYAN.getRGB(), true);
        }
    }

    public class MinecraftListRenderer<E> implements SkinRenderer<ListOption<E>> {
        private int maxWidth = 50;

        private void calculateMaxWidth(ListOption<E> option) {
            for (E listValues : option.getValues()) {
                int width = mc.textRenderer.getWidth(listValues.toString()) + 5;
                if (width > maxWidth) {
                    maxWidth = width;
                }
            }
        }

        @Override
        public void render(DrawContext drawContext, ListOption<E> option, int x, int y, int mouseX, int mouseY) {
            calculateMaxWidth(option);
            option.setHeight(25);
            option.setWidth(maxWidth);

            drawContext.drawText(mc.textRenderer, option.name + ": ", x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - maxWidth - 25, y);

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX, mouseY)), option.getX(), y, maxWidth, 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = option.get().toString();
            drawContext.drawText(mc.textRenderer, text, option.getX() + maxWidth / 2 - mc.textRenderer.getWidth(text) / 2, y + 5, Color.CYAN.getRGB(), true);
        }
    }

    public class MinecraftSubMenuRenderer implements SkinRenderer<SubMenuOption> {
        @Override
        public void render(DrawContext drawContext, SubMenuOption option, int x, int y, int mouseX, int mouseY) {
            option.setHeight(20);
            option.setWidth(30);

            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - 55, y);

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX, mouseY)), option.getX(), y, option.getWidth(), 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = "Open";
            drawContext.drawText(mc.textRenderer, text, option.getX() + option.getWidth() / 2 - mc.textRenderer.getWidth(text) / 2, y + 5, Color.YELLOW.getRGB(), true);

            option.getSubMenu().render(drawContext, x + option.getParentMenu().getWidth(), y, mouseX, mouseY);
        }
    }

    public class MinecraftRunnableRenderer implements SkinRenderer<RunnableOption> {
        Color DARK_RED = new Color(116, 0, 0);
        Color DARK_GREEN = new Color(24, 132, 0, 226);

        @Override
        public void render(DrawContext drawContext, RunnableOption option, int x, int y, int mouseX, int mouseY) {
            option.setHeight(25);
            option.setWidth(26);

            drawContext.drawText(mc.textRenderer, option.name + ": ", x + 15, y + 25 / 2 - 5, -1, true);

            option.setPosition(x + panelWidth - 51, y);

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(!option.value, option.isMouseOver(mouseX, mouseY)), option.getX(), y, option.getWidth(), 20);
            drawContext.drawText(mc.textRenderer, "Run", option.getX() + option.getWidth() / 2 - mc.textRenderer.getWidth("Run") / 2, y + 5, option.value ? DARK_GREEN.getRGB() : DARK_RED.getRGB(), true);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
        }
    }
}