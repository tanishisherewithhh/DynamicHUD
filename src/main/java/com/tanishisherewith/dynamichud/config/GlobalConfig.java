package com.tanishisherewith.dynamichud.config;

import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;

public final class GlobalConfig {
    public static final ConfigClassHandler<GlobalConfig> HANDLER = ConfigClassHandler.createBuilder(GlobalConfig.class)
            .id(Identifier.of("dynamichud", "dynamichud_config"))
            .serializer(config -> GsonConfigSerializerBuilder.create(config)
                    .setPath(FabricLoader.getInstance().getConfigDir().resolve("dynamichud.json5"))
                    .setJson5(true)
                    .build())
            .build();

    private static final GlobalConfig INSTANCE = new GlobalConfig();
    /**
     * Common scale for all widgets.
     */
    @SerialEntry
    private float scale = 1.0f;

    @SerialEntry
    private boolean displayDescriptions = false;

    @SerialEntry
    private boolean showColorPickerPreview = true;

    @SerialEntry
    private boolean renderInDebugScreen = false;

    @SerialEntry
    private final boolean forceSameContextMenuSkin = true;

    //These package names are getting seriously long
    @SerialEntry
    private com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity complexity = com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity.Simple;

    @SerialEntry
    private int snapSize = 100;

    @SerialEntry
    private Color hudActiveColor = new Color(0, 0, 0, 128);

    @SerialEntry
    private Color hudInactiveColor = new Color(255, 0, 0, 128);

    public static GlobalConfig get() {
        return INSTANCE;
    }

    public Screen createYACLGUI() {
        return YetAnotherConfigLib.createBuilder()
                .title(Text.literal("DynamicHUD config screen."))
                .category(ConfigCategory.createBuilder()
                        .name(Text.literal("General"))
                        .tooltip(Text.literal("Set the general settings for all widgets."))
                        .group(OptionGroup.createBuilder()
                                .name(Text.literal("Global"))
                                .description(OptionDescription.of(Text.literal("Global settings for all widgets.")))
                                .option(Option.<Float>createBuilder()
                                        .name(Text.literal("Scale"))
                                        .description(OptionDescription.of(Text.literal("Set scale for all widgets.")))
                                        .binding(1.0f, () -> this.scale, newVal -> this.scale = newVal)
                                        .controller(floatOption -> FloatSliderControllerBuilder.create(floatOption).range(0.1f, 2.5f).step(0.1f))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Render in debug screen"))
                                        .description(OptionDescription.of(Text.literal("Renders widgets even when the debug screen is on")))
                                        .binding(true, () -> this.renderInDebugScreen, newVal -> this.renderInDebugScreen = newVal)
                                        .controller(booleanOption -> BooleanControllerBuilder.create(booleanOption).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show Color picker preview"))
                                        .description(OptionDescription.of(Text.literal("Shows the preview below your mouse pointer on selecting color from the screen. Note: You may drop some frames with the preview on.")))
                                        .binding(true, () -> this.showColorPickerPreview, newVal -> this.showColorPickerPreview = newVal)
                                        .controller(booleanOption -> BooleanControllerBuilder.create(booleanOption).yesNoFormatter())
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Text.literal("Show widget descriptions/tooltips"))
                                        .description(OptionDescription.of(Text.literal("Shows the description of widgets as tooltips.")))
                                        .binding(true, () -> this.displayDescriptions, newVal -> this.displayDescriptions = newVal)
                                        .controller(booleanOption -> BooleanControllerBuilder.create(booleanOption).yesNoFormatter())
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Text.literal("Snap Size"))
                                        .description(OptionDescription.of(Text.literal("Grid size for snapping widgets")))
                                        .binding(100, () -> this.snapSize, newVal -> this.snapSize = newVal)
                                        .controller(integerOption -> IntegerFieldControllerBuilder.create(integerOption).range(10, 500))
                                        .build())
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Widget HUD Active Background Color"))
                                .description(OptionDescription.of(Text.literal("Color of the background of the widget when it will be rendered")))
                                .binding(new Color(0, 0, 0, 128), () -> this.hudActiveColor, newVal -> this.hudActiveColor = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<Color>createBuilder()
                                .name(Text.literal("Widget HUD Inactive Background Color"))
                                .description(OptionDescription.of(Text.literal("Color of the background of the widget when it will NOT be rendered")))
                                .binding(new Color(255, 0, 0, 128), () -> this.hudInactiveColor, newVal -> this.hudInactiveColor = newVal)
                                .controller(ColorControllerBuilder::create)
                                .build())
                        .option(Option.<com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity>createBuilder()
                                .name(Text.literal("Settings Complexity"))
                                .description(OptionDescription.of(Text.literal("The level of options to display. Options equal to or below this level will be displayed")))
                                .binding(com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity.Simple, () -> this.complexity, newVal -> this.complexity = newVal)
                                .controller((option) -> EnumControllerBuilder.create(option)
                                        .enumClass(com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity.class)
                                        .formatValue(value -> Text.of(value.name()))
                                )
                                .build())
                        .build())
                .save(HANDLER::save)
                .build()
                .generateScreen(MinecraftClient.getInstance().currentScreen);
    }

    public float getScale() {
        return scale;
    }

    public boolean showColorPickerPreview() {
        return showColorPickerPreview;
    }

    public boolean shouldDisplayDescriptions() {
        return displayDescriptions;
    }

    public boolean renderInDebugScreen() {
        return renderInDebugScreen;
    }

    public int getSnapSize() {
        return snapSize;
    }

    public Color getHudInactiveColor() {
        return hudInactiveColor;
    }

    public Color getHudActiveColor() {
        return hudActiveColor;
    }

    public com.tanishisherewith.dynamichud.utils.contextmenu.options.Option.Complexity complexity() {
        return complexity;
    }
}
