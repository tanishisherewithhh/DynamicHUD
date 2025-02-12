package com.tanishisherewith.dynamichud;

import com.tanishisherewith.dynamichud.config.GlobalConfig;
import com.tanishisherewith.dynamichud.internal.ModError;
import com.tanishisherewith.dynamichud.internal.WarningScreen;
import com.tanishisherewith.dynamichud.screens.AbstractMoveableScreen;
import com.tanishisherewith.dynamichud.utils.BooleanPool;
import com.tanishisherewith.dynamichud.widget.Widget;
import com.tanishisherewith.dynamichud.widget.WidgetManager;
import com.tanishisherewith.dynamichud.widget.WidgetRenderer;
import com.tanishisherewith.dynamichud.widgets.ItemWidget;
import com.tanishisherewith.dynamichud.widgets.TextWidget;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

@Environment(EnvType.CLIENT)
public class DynamicHUD implements ClientModInitializer {
    /**
     * This is a map to store the list of widgets for each widget file to be saved.
     * <p>
     * Allows saving widgets across different mods with same save file name.
     */
    public static final HashMap<String, List<Widget>> FILE_MAP = new HashMap<>();

    public static final Logger logger = LoggerFactory.getLogger("DynamicHud");
    private static final List<WidgetRenderer> widgetRenderers = new ArrayList<>();
    public static MinecraftClient MC = MinecraftClient.getInstance();
    public static String MOD_ID = "dynamichud";
    private static boolean enableTestIntegration = false;

    public static void addWidgetRenderer(WidgetRenderer widgetRenderer) {
        widgetRenderers.add(widgetRenderer);
    }

    public static List<WidgetRenderer> getWidgetRenderers() {
        return widgetRenderers;
    }

    public static void printInfo(String msg) {
        logger.info(msg);
    }

    public static void printWarn(String msg) {
        logger.warn(msg);
    }

    /**
     * Opens the MovableScreen when the specified key is pressed.
     *
     * @param key    The key to listen for
     * @param screen The AbstractMoveableScreen instance to use to set the screen
     */
    public static void openDynamicScreen(KeyBinding key, AbstractMoveableScreen screen) {
        if (key.wasPressed()) {
            MC.setScreen(screen);
        }
    }

    public void checkToEnableTestIntegration(){
        String[] args = FabricLoader.getInstance().getLaunchArguments(true);
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--dynamicHudTest") && i + 1 < args.length) {
                enableTestIntegration = Boolean.parseBoolean(args[i + 1]);
                break;
            }
        }
    }

    @Override
    public void onInitializeClient() {
        printInfo("Initialising DynamicHUD");

        // Add WidgetData of included widgets
        WidgetManager.registerCustomWidgets(
                TextWidget.DATA,
                ItemWidget.DATA
        );

        //YACL load
        GlobalConfig.HANDLER.load();

        checkToEnableTestIntegration();

        printInfo("Integrating mods...");
        List<EntrypointContainer<DynamicHudIntegration>> integrations = new ArrayList<>(getRegisteredIntegrations());

        if (enableTestIntegration) {
            EntrypointContainer<DynamicHudIntegration> testIntegration = getTestIntegration();
            if (testIntegration != null) {
                integrations.add(testIntegration);
                printInfo("Test integration enabled and loaded successfully.");
            }
        }

        List<ModError> invalid_implementations = new ArrayList<>();

        for (EntrypointContainer<DynamicHudIntegration> entrypoint : integrations) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            String modId = metadata.getId();

            printInfo(String.format("Supported mod with id %s was found!", modId));

            AbstractMoveableScreen screen;
            KeyBinding binding;
            WidgetRenderer widgetRenderer;
            File widgetsFile;
            try {
                DynamicHudIntegration DHIntegration = entrypoint.getEntrypoint();

                //Calls the init method
                DHIntegration.init();

                //Gets the widget file to save and load the widgets from
                widgetsFile = DHIntegration.getWidgetsFile();

                // Adds / loads widgets from file
                if (WidgetManager.doesWidgetFileExist(widgetsFile)) {
                    WidgetManager.loadWidgets(widgetsFile);
                } else {
                    DHIntegration.addWidgets();
                }

                //Calls the second init method
                DHIntegration.initAfter();

                // Get the instance of AbstractMoveableScreen
                screen = Objects.requireNonNull(DHIntegration.getMovableScreen(), "AbstractMovableScreen instance should not be null!");

                // Get the keybind to open the screen instance
                binding = DHIntegration.getKeyBind();

                //Register custom widget datas by WidgetManager.registerCustomWidgets();
                DHIntegration.registerCustomWidgets();

                //WidgetRenderer with widgets instance
                widgetRenderer = DHIntegration.getWidgetRenderer();
                addWidgetRenderer(widgetRenderer);

                List<Widget> widgets = FILE_MAP.get(widgetsFile.getName());

                if (widgets == null || widgets.isEmpty()) {
                    FILE_MAP.put(widgetsFile.getName(), widgetRenderer.getWidgets());
                } else {
                    widgets.addAll(widgetRenderer.getWidgets());
                    FILE_MAP.put(widgetsFile.getName(), widgets);
                }

                //Register events for rendering, saving, loading, and opening the hudEditor
                ClientTickEvents.START_CLIENT_TICK.register((client) -> openDynamicScreen(binding, screen));

                /* === Saving === */
                // Each mod is hooked to the fabric's event system to save its widget.

                //When a player exits a world (SinglePlayer worlds) or a server stops
                ServerLifecycleEvents.SERVER_STOPPING.register(server -> saveWidgetsSafely(widgetsFile, FILE_MAP.get(widgetsFile.getName())));

                // When a resource pack is reloaded.
                ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, s) -> saveWidgetsSafely(widgetsFile, FILE_MAP.get(widgetsFile.getName())));

                //When player disconnects
                ServerPlayConnectionEvents.DISCONNECT.register((handler, packetSender) -> saveWidgetsSafely(widgetsFile, FILE_MAP.get(widgetsFile.getName())));

                //When minecraft closes
                ClientLifecycleEvents.CLIENT_STOPPING.register((minecraftClient) -> saveWidgetsSafely(widgetsFile, FILE_MAP.get(widgetsFile.getName())));

                printInfo(String.format("Integration of mod %s was successful", modId));
            } catch (Throwable e) {
                if (e instanceof IOException) {
                    logger.warn("An error has occurred while loading widgets of mod {}", modId, e);
                } else {
                    logger.error("Mod {} has improper implementation of DynamicHUD", modId, e);
                }
                invalid_implementations.add(new ModError(modId, e.getLocalizedMessage().trim()));
            }
        }
        printInfo("(DynamicHUD) Integration of supported mods was successful");

        //In game screen render.
        HudRenderCallback.EVENT.register(new HudRender());

        if(!invalid_implementations.isEmpty()){
            BooleanPool.put("WarningScreen", false);
            ClientTickEvents.START_CLIENT_TICK.register((client)->{
                if(BooleanPool.get("WarningScreen")) return;

                if(MC.currentScreen instanceof TitleScreen) {
                    MC.executeTask(()->MC.setScreen(new WarningScreen(invalid_implementations)));
                    BooleanPool.put("WarningScreen", true);
                }
            });
        }
    }


    private void saveWidgetsSafely(File widgetsFile, List<Widget> widgets) {
        try {
            WidgetManager.saveWidgets(widgetsFile, widgets);
        } catch (IOException e) {
            logger.error("Failed to save widgets. Widgets passed: {}", widgets);
            throw new RuntimeException(e);
        }
    }

    private List<EntrypointContainer<DynamicHudIntegration>> getRegisteredIntegrations() {
        return new ArrayList<>(FabricLoader.getInstance()
                .getEntrypointContainers("dynamicHud", DynamicHudIntegration.class));
    }

    /**
     * This makes it so that if minecraft is launched with the program arguments
     * <p>
     * {@code --dynamicHudTest true}
     * </p>
     * then it will
     * load the {@link DynamicHudTest} class as an entrypoint, eliminating any errors due to human incapacity of
     * adding/removing a single line from the `fabric.mod.json`
     */
    private EntrypointContainer<DynamicHudIntegration> getTestIntegration() {
        DynamicHudIntegration testIntegration;
        try {
            Class<?> testClass = Class.forName("com.tanishisherewith.dynamichud.DynamicHudTest");
            testIntegration = (DynamicHudIntegration) testClass.getDeclaredConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            logger.info("DynamicHudTest class not found. Skipping test integration.");
            return null;
        } catch (Exception e) {
            logger.error("Error instantiating DynamicHudTest", e);
            return null;
        }

        return new EntrypointContainer<>() {
            @Override
            public DynamicHudIntegration getEntrypoint() {
                return testIntegration;
            }

            @Override
            public ModContainer getProvider() {
                return FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
            }
        };
    }

}
