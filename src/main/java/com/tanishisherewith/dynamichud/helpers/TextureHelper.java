package com.tanishisherewith.dynamichud.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

/**
 * This class provides helper methods for drawing textures on the screen.
 */
public class TextureHelper extends DrawContext {
    public TextureHelper(MinecraftClient client, VertexConsumerProvider.Immediate vertexConsumers) {
        super(client, vertexConsumers);
    }

    /**
     * Draws an item texture on the screen.
     *
     * @param itemStack   The item stack to render the texture for
     * @param x           The x position to draw the texture at
     * @param y           The y position to draw the texture at
     */
    public static void drawItemTexture(DrawContext drawContext,
                                       ItemStack itemStack,
                                       int x,
                                       int y) {
        drawContext.drawItem(itemStack,x,y);
    }

    /**
     * Draws the texture of the item in the player's main hand on the screen.
     *
     * @param client      The Minecraft client instance
     * @param x           The x position to draw the texture at
     * @param y           The y position to draw the texture at
     */
    public static void drawMainHandTexture(DrawContext drawContext,
                                           MinecraftClient client,
                                           int x,
                                           int y) {
        assert client.player != null;
        ItemStack mainHandItem = client.player.getMainHandStack();
        drawItemTexture(drawContext,mainHandItem, x, y);
    }
    /**
     * Draws a textured rectangle on the screen.
     *
     * @param x        The x position of the top left corner of the rectangle
     * @param y        The y position of the top left corner of the rectangle
     * @param u        The x position of the texture within the texture image
     * @param v        The y position of the texture within the texture image
     * @param width    The width of the rectangle
     * @param height   The height of the rectangle
     * @param textureWidth  The width of the texture image
     * @param textureHeight The height of the texture image
     */
    public static void drawTexture(DrawContext drawContext, Identifier texture, int x, int y, int u, int v, int width, int height, int textureWidth, int textureHeight) {
        drawContext.drawTexture(texture, x, y, u, v, width, height, textureWidth, textureHeight);
    }

    /**
     * Draws a textured rectangle on the screen with a specified color.
     *
     * @param x        The x position of the top left corner of the rectangle
     * @param y        The y position of the top left corner of the rectangle
     * @param u        The x position of the texture within the texture image
     * @param v        The y position of the texture within the texture image
     * @param width    The width of the rectangle
     * @param height   The height of the rectangle
     * @param color    The color to draw the rectangle with
     */
    public static void drawTexturedRect(DrawContext drawContext,Identifier texture, int x, int y, int u, int v, int width, int height, int color) {
        RenderSystem.setShaderColor((color >> 16 & 255) / 255.0F,
                (color >> 8 & 255) / 255.0F,
                (color & 255) / 255.0F,
                (color >> 24 & 255) / 255.0F);
        drawContext.drawTexture(texture, x, y, u, v, width, height);
    }
    /**
     * Draws an item texture on the screen with text at a specified position relative to it.
     *
     * @param matrices    The matrix stack used for rendering
     * @param itemRenderer The item renderer instance used for rendering the item texture
     * @param textRenderer The text renderer instance used for rendering the text
     * @param itemStack   The item stack to render the texture for
     * @param x           The x position to draw the texture at
     * @param y           The y position to draw the texture at
     * @param text        The text to draw relative to the texture
     * @param color       The color to draw the text with
     * @param position    The position of the text relative to the texture (ABOVE, BELOW, LEFT, or RIGHT)
     * @param scale       The scale factor to apply to the text (1.0 is normal size)
     */
    public static void drawItemTextureWithText(MatrixStack matrices,
                                               DrawContext drawContext,
                                               ItemRenderer itemRenderer,
                                               TextRenderer textRenderer,
                                               ItemStack itemStack,
                                               int x,
                                               int y,
                                               String text,
                                               int color,
                                               Position position,
                                               float scale,
                                               boolean textBackground
                                              ) {
        // Calculate the position of the text based on its size and the specified position
        int textWidth = (int) (textRenderer.getWidth(text) * scale);
        int textHeight = (int) (textRenderer.fontHeight * scale);
        int textX = 0;
        int textY = 0;
        switch (position) {
            case ABOVE -> {
                textX = x + (16 - textWidth) / 2;
                textY = y - textHeight;
            }
            case BELOW -> {
                textX = x + (17 - textWidth) / 2;
                textY = y + 16;
            }
            case LEFT -> {
                textX = x - textWidth - 2;
                textY = y + (16 - textHeight) / 2;
            }
            case RIGHT -> {
                textX = x + 18;
                textY = y + (16 - textHeight) / 2;
            }
        }

        // Draw semi-opaque black rectangle
        if(text!=null) {
            if (textBackground && !text.trim().isEmpty()) {
                int backgroundColor = 0x40000000; // ARGB format: 50% opaque black
                drawContext.fill(textX - 1, textY - 1, textX + textWidth + 1, textY + textHeight + 1, backgroundColor);
            }

            // Draw the scaled text at the calculated position
            matrices.push();
            matrices.scale(scale, scale, 1.0f);
            float scaledX = textX / scale;
            float scaledY = textY / scale;
            drawContext.drawText(textRenderer, text, (int) scaledX, (int) scaledY, color, false);
            matrices.pop();
        }
        // Draw the item texture
        drawContext.drawItem(itemStack, x, y);
    }


    public enum Position {
        ABOVE ("Above"),
        BELOW("Below"),
        LEFT("Left"),
        RIGHT("Right");
        private String name;

        Position(String name) {
            this.name = name;
        }

        public static Position getByUpperCaseName(String name) {
            if (name == null || name.isEmpty()) {
                return null;
            }

            return Position.valueOf(name.toUpperCase());
        }
    }


}
