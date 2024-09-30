package com.tanishisherewith.dynamichud.utils.contextmenu.skinsystem;

import com.mojang.blaze3d.systems.RenderSystem;
import com.tanishisherewith.dynamichud.helpers.DrawHelper;
import com.tanishisherewith.dynamichud.utils.contextmenu.ContextMenu;
import com.tanishisherewith.dynamichud.utils.contextmenu.Option;
import com.tanishisherewith.dynamichud.utils.contextmenu.options.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ButtonTextures;
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
    public static MinecraftClient mc = MinecraftClient.getInstance();
    public Identifier BACKGROUND_PANEL;
    private int scrollOffset = 0;
    private int maxScrollOffset = 0;
    private final int panelWidth;
    private final int panelHeight;
    private final PanelColor panelColor;
    public static final ButtonTextures TEXTURES = new ButtonTextures(Identifier.ofVanilla("widget/button"), Identifier.ofVanilla("widget/button_disabled"), Identifier.ofVanilla("widget/button_highlighted"));
    private double scrollVelocity = 0;

    public enum PanelColor {
        DARK_PANEL(0.2f, 0.2f, 0.2f, 0.9f),
        CREAMY(1.0f, 0.9f, 0.8f, 0.9f),
        LIGHT_BLUE(0.6f, 0.8f, 1.0f, 0.9f),
        SOFT_GREEN(0.6f, 1.0f, 0.6f, 0.9f),
        WARM_YELLOW(1.0f, 1.0f, 0.6f, 0.9f),
        SUNSET_ORANGE(1.0f, 0.5f, 0.0f, 0.9f),
        OCEAN_BLUE(0.0f, 0.5f, 1.0f, 0.9f),
        FOREST_GREEN(0.0f, 0.6f, 0.2f, 0.9f),
        MIDNIGHT_PURPLE(0.3f, 0.0f, 0.5f, 0.9f),
        GOLDEN_YELLOW(1.0f, 0.8f, 0.0f, 0.9f),
        ROSE_PINK(1.0f, 0.4f, 0.6f, 0.9f),
        SKY_BLUE(0.5f, 0.8f, 1.0f, 0.9f),
        LIME_GREEN(0.7f, 1.0f, 0.3f, 0.9f),
        COFFEE_BROWN(0.6f, 0.3f, 0.1f, 0.9f),
        LAVENDER(0.8f, 0.6f, 1.0f, 0.9f),
        CUSTOM(0.0f, 0.0f, 0.0f, 0.0f); // Placeholder for custom colors

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

        public void applyColor() {
            RenderSystem.setShaderColor(red, green, blue, alpha);
        }

        public static PanelColor custom(float red, float green, float blue, float alpha) {
            PanelColor custom = CUSTOM;
            custom.red = red;
            custom.green = green;
            custom.blue = blue;
            custom.alpha = alpha;
            return custom;
        }
    }

    public MinecraftSkin(PanelColor color, Identifier backgroundPanel, int panelWidth, int panelHeight) {
        super();
        this.panelColor = color;
        addRenderer(BooleanOption.class, new MinecraftBooleanRenderer());
        addRenderer(DoubleOption.class, new MinecraftDoubleRenderer());
        addRenderer(EnumOption.class, new MinecraftEnumRenderer());
        addRenderer(ListOption.class, new MinecraftListRenderer());
        addRenderer(SubMenuOption.class, new MinecraftSubMenuRenderer());
        addRenderer(RunnableOption.class, new MinecraftRunnableRenderer());
        addRenderer(ColorOption.class, new MinecraftColorOptionRenderer());

        this.panelHeight = panelHeight;
        this.panelWidth = panelWidth;
        this.BACKGROUND_PANEL = backgroundPanel;

        if(contextMenu != null){
            contextMenu.newScreenFlag = true;
        }
    }
    public MinecraftSkin(PanelColor color) {
        this(color,Identifier.of("minecraft","textures/gui/demo_background.png"),248,165);
    }

    @Override
    public void renderContextMenu(DrawContext drawContext, ContextMenu contextMenu, int mouseX, int mouseY) {
        this.contextMenu = contextMenu;
        if (!contextMenu.newScreenFlag) {
            contextMenu.newScreenFlag = true;
            return;
        }

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        contextMenu.set(centerX,centerY,0);

        // Calculate the top-left corner of the image
        int imageX = centerX - panelWidth / 2;
        int imageY = centerY - panelHeight / 2;

        RenderSystem.enableBlend();
        RenderSystem.enableDepthTest();
        panelColor.applyColor();
        drawContext.drawTexture(BACKGROUND_PANEL, imageX, imageY, 0, 0, panelWidth, panelHeight);
        RenderSystem.setShaderColor(1.0f,1.0f,1.0f,1.0f);
        drawContext.drawGuiTexture(TEXTURES.get(true, isMouseOver(mouseX,mouseY,imageX + 3,imageY + 3,14,14)), imageX + 3,imageY + 3,14,14);
        drawContext.drawText(mc.textRenderer,"X",imageX + 10 - mc.textRenderer.getWidth("X")/2 ,imageY + 6,-1,true);

        DrawHelper.enableScissor(imageX,imageY + 2,screenWidth,panelHeight - 4);
        int yOffset = imageY + 10 - scrollOffset;
        contextMenu.setWidth(panelWidth - 4);
        contextMenu.setFinalWidth(panelWidth - 4);
        contextMenu.y = imageY;
        int maxYOffset = imageY + panelHeight - 4;

        for (Option<?> option : contextMenu.getOptions()) {
            if (!option.shouldRender()) continue;

            if (yOffset >= imageY && yOffset <= maxYOffset - option.height + 4) {
                option.render(drawContext, imageX + 4, yOffset, mouseX, mouseY);
            }

            yOffset += option.height + 1;
        }

        contextMenu.height = (yOffset - imageY) + 25;

        applyMomentum();

        // Calculate max scroll offset
        maxScrollOffset = Math.max(0, contextMenu.height - panelHeight + 10);
        scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScrollOffset);

        // Disable scissor after rendering
        DrawHelper.disableScissor();
    }
    private boolean isMouseOver(double mouseX, double mouseY, double x, double y,double width, double height){
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    private void applyMomentum() {
        if (scrollVelocity != 0) {
            scrollOffset += (int) scrollVelocity;
            scrollOffset = MathHelper.clamp(scrollOffset, 0, maxScrollOffset);
            scrollVelocity *= 0.9; //Friction

            // Stop the scrolling if the velocity is very low
            if (Math.abs(scrollVelocity) < 0.12) {
                scrollVelocity = 0;
            }
        }
    }

    @Override
    public void mouseScrolled(ContextMenu menu, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollVelocity += verticalAmount;
        super.mouseScrolled(menu, mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(ContextMenu menu, double mouseX, double mouseY, int button) {
        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        // Calculate the top-left corner of the image
        int imageX = (screenWidth - panelWidth) / 2;
        int imageY = (screenHeight - panelHeight) / 2;

        if(button == GLFW.GLFW_MOUSE_BUTTON_LEFT && isMouseOver(mouseX,mouseY,imageX + 3,imageY + 3,14,14)){
            contextMenu.close();
        }

        return super.mouseClicked(menu, mouseX, mouseY, button);
    }

    public class MinecraftBooleanRenderer implements SkinRenderer<BooleanOption> {
        @Override
        public void render(DrawContext drawContext, BooleanOption option, int x, int y, int mouseX, int mouseY) {
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - 75;
            option.y = y;

            int width = 50;
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX,mouseY)), option.x,y,width,20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Text text = option.getBooleanType().getText(option.value);
            int color = option.value ? Color.GREEN.getRGB() : Color.RED.getRGB();
            drawContext.drawText(mc.textRenderer,text,(int) (option.x + (width/2.0f) -(mc.textRenderer.getWidth(text)/2.0f)) ,y + 5,color,true);
            //drawContext.drawTexture(BOOLEAN_TEXTURE);

            option.height = 25;

            //Widths dont matter in this skin.
            option.width = width;
        }

        @Override
        public boolean mouseClicked(BooleanOption option, double mouseX, double mouseY, int button) {
            if(mouseX >= option.x && mouseX <= option.x + 50 && mouseY >= option.y && mouseY <= option.y + option.height){
                option.set(!option.get());
            }
            return SkinRenderer.super.mouseClicked(option, mouseX, mouseY, button);
        }
    }

    public class MinecraftColorOptionRenderer implements SkinRenderer<ColorOption> {
        @Override
        public void render(DrawContext drawContext, ColorOption option, int x, int y, int mouseX, int mouseY) {
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - 75;
            option.y = y;

            int width = 20;
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(!option.isVisible, option.isMouseOver(mouseX,mouseY)), option.x,y,width,20);
            int shadowOpacity = Math.min(option.value.getAlpha(),45);
            DrawHelper.drawRectangleWithShadowBadWay(drawContext.getMatrices().peek().getPositionMatrix(),
                    option.x + 4,
                    y + 4,
                    width - 8,
                    20 - 8,
                    option.value.getRGB(),
                    shadowOpacity,
                    1,
                    1);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);


            option.height = 25;
            option.width = width;

            option.getColorPicker().render(drawContext, x + panelWidth + 10, y - 10);
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
            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25/2 - 5, -1, true);

            option.width = panelWidth - 150;
            option.height = 25;

            option.x = x + panelWidth - 122;
            option.y = y;
            double sliderX = option.x + (option.value - option.minValue) / (option.maxValue - option.minValue) * (option.width - 8);
            boolean isMouseOverHandle = isMouseOver(mouseX,mouseY, sliderX, y);

            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.drawGuiTexture(option.isMouseOver(mouseX,mouseY)? HIGHLIGHTED_TEXTURE : TEXTURE, option.x, y, option.width, 20);
            drawContext.drawGuiTexture(isMouseOverHandle ? HANDLE_HIGHLIGHTED_TEXTURE : HANDLE_TEXTURE , (int) Math.round(sliderX), y, 8, 20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            drawContext.drawText(mc.textRenderer, String.valueOf(option.value),option.x + option.width/2 - mc.textRenderer.getWidth(option.value.toString())/2 ,y + 5,16777215,false);
        }
        private boolean isMouseOver(double mouseX, double mouseY, double x, double y){
            return mouseX >= x && mouseX <= x + 10 && mouseY >= y && mouseY <= y + 20;
        }

        @Override
        public boolean mouseClicked(DoubleOption option, double mouseX, double mouseY, int button) {
            return isMouseOver(mouseX,mouseY,option.x,option.y);
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
            option.height = 25;
            option.width = maxWidth;

            drawContext.drawText(mc.textRenderer, option.name + ": ", x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - maxWidth - 25;
            option.y = y;

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX,mouseY)), option.x,y,maxWidth,20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = option.get().toString();
            drawContext.drawText(mc.textRenderer,text,option.x + maxWidth/2 - mc.textRenderer.getWidth(text)/2 ,y + 5,Color.CYAN.getRGB(),true);
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
            option.height = 25;
            option.width = maxWidth;

            drawContext.drawText(mc.textRenderer, option.name + ": ", x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - maxWidth - 25;
            option.y = y;

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX,mouseY)), option.x,y,maxWidth,20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = option.get().toString();
            drawContext.drawText(mc.textRenderer,text,option.x + maxWidth/2 - mc.textRenderer.getWidth(text)/2 ,y + 5,Color.CYAN.getRGB(),true);
        }
    }

    public class MinecraftSubMenuRenderer implements SkinRenderer<SubMenuOption> {
        @Override
        public void render(DrawContext drawContext, SubMenuOption option, int x, int y, int mouseX, int mouseY) {
            option.height = 20;
            option.width = 30;

            drawContext.drawText(mc.textRenderer, option.name, x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - 75;
            option.y = y;

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(true, option.isMouseOver(mouseX,mouseY)), option.x,y, option.width,20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            String text = "Open";
            drawContext.drawText(mc.textRenderer,text,option.x +  option.width/2 - mc.textRenderer.getWidth(text)/2 ,y + 5,Color.YELLOW.getRGB(),true);

            option.getSubMenu().render(drawContext, x + option.getParentMenu().finalWidth, y, mouseX, mouseY);
        }
    }

    public class MinecraftRunnableRenderer implements SkinRenderer<RunnableOption> {
        Color DARK_RED = new Color(116, 0, 0);
        Color DARK_GREEN = new Color(24, 132, 0, 226);

        @Override
        public void render(DrawContext drawContext, RunnableOption option, int x, int y, int mouseX, int mouseY) {
            option.height = 25;
            option.width = 26;

            drawContext.drawText(mc.textRenderer, option.name.replaceFirst("Run: ","") + ": ", x + 15, y + 25/2 - 5, -1, true);

            option.x = x + panelWidth - 75;
            option.y = y;

            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.enableBlend();
            RenderSystem.enableDepthTest();
            drawContext.drawGuiTexture(TEXTURES.get(!option.value, option.isMouseOver(mouseX,mouseY)), option.x,y, option.width,20);
            drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            drawContext.drawText(mc.textRenderer,"Run",option.x +  option.width/2 - mc.textRenderer.getWidth("Run")/2 ,y + 5, option.value ? DARK_GREEN.getRGB() : DARK_RED.getRGB(),true);
        }
    }


}
