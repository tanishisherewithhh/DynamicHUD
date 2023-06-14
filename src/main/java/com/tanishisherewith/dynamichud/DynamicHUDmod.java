package com.tanishisherewith.dynamichud;

import com.tanishisherewith.dynamichud.helpers.TextureHelper;
import com.tanishisherewith.dynamichud.huds.MoveableScreen;
import com.tanishisherewith.dynamichud.util.DynamicUtil;
import com.tanishisherewith.dynamichud.util.TextGenerator;
import com.tanishisherewith.dynamichud.util.WidgetLoading;
import com.tanishisherewith.dynamichud.widget.IWigdets;
import com.tanishisherewith.dynamichud.widget.Widget;
import com.tanishisherewith.dynamichud.widget.armor.ArmorWidget;
import com.tanishisherewith.dynamichud.widget.item.ItemWidget;
import com.tanishisherewith.dynamichud.widget.text.TextWidget;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static com.tanishisherewith.dynamichud.DynamicHUD.WIDGETS_FILE;

public class DynamicHUDmod implements ClientModInitializer, IWigdets,WidgetLoading {
    MinecraftClient mc = MinecraftClient.getInstance();
    protected List<Widget> widgets = new ArrayList<>();
    private DynamicUtil dynamicutil;


    @Override
    public void onInitializeClient() {
        dynamicutil = DynamicHUD.getDynamicUtil();

        widgets.clear();

        DynamicHUD.setAbstractScreen(new MoveableScreen(Text.of("Editor Screen"),dynamicutil));

        DynamicHUD.setIWigdets(new DynamicHUDmod());
    }

    @Override
    public void addWigdets(DynamicUtil dynamicUtil) {
        System.out.println("Added widgets");
            widgets.add(new TextWidget(mc, "FPS: ", () -> mc.fpsDebugString.split(" ")[0], 0.5f, 0.5f, true, true, false, -1, -1, true));
            widgets.add(new TextWidget(mc, "Dynamic", () -> "HUD", 0.7f, 0.3f, false, false, false, -1, -1, true));
            widgets.add(new TextWidget(mc, "Ping: ", () -> "", 0.08f, 0.5f, false, false, false, -1, -1, true));
            widgets.add(new TextWidget(mc, "Position: ", () -> "", 0.4f, 0.8f, false, false, false, -1, -1, true));
            widgets.add(new TextWidget(mc, "Day/Night: ", () -> "", 0.83f, 0.8f, false, false, false, -1, -1, true));

            // Add an armor widget to the custom HUD
            String text="Text";
            widgets.add(new ArmorWidget(mc, EquipmentSlot.CHEST, 0.01f, 0.01f, true, TextureHelper.Position.ABOVE, () -> text, ()->Color.RED,true));
            widgets.add(new ArmorWidget(mc, EquipmentSlot.LEGS, 0.05f, 0.01f, true, TextureHelper.Position.LEFT, () -> String.valueOf(MinecraftClient.getInstance().getCurrentFps()),()->Color.WHITE,true));

            widgets.add(new ItemWidget(mc,() -> mc.player != null ? mc.player.getInventory().getStack(3) : Items.DIAMOND_AXE.getDefaultStack(), 0.15f, 0.15f, true, TextureHelper.Position.ABOVE, () -> "",()-> Color.RED,true));
            for (Widget wigdet : widgets) {
                dynamicUtil.getWidgetManager().addWidget(wigdet);
            }
    }

    @Override
    public void loadWigdets(DynamicUtil dynamicUtil) {
        if (mc.player!=null) {
            List<Widget> widgets = dynamicUtil.getWidgetManager().loadWigdets(WIDGETS_FILE);
            int textIndex = 0;
            int armorIndex = 0;
            TextGenerator[] TextWidgettext = new TextGenerator[]{
                    () -> String.valueOf(mc.getCurrentFps()),
                    () -> "PLAINS",
                    () -> "",
                    () -> "",
                    () -> ""
            };
            TextGenerator[] ArmorWidgettext = new TextGenerator[]{
                    () -> String.valueOf(mc.getCurrentFps()),
                    () -> "",
                    () -> ""
            };
            for (Widget widget : widgets) {
                if (widget instanceof TextWidget textWidget) {
                    TextGenerator textGenerator = TextWidgettext[textIndex++];
                    textWidget.setDataTextGenerator(textGenerator);
                    dynamicUtil.getWidgetManager().addWidget(textWidget);
                }
                if (widget instanceof ArmorWidget armorWidget) {
                    TextGenerator textGenerator = ArmorWidgettext[armorIndex++];
                    armorWidget.setTextGenerator(textGenerator);
                    dynamicUtil.getWidgetManager().addWidget(armorWidget);
                }
                if (widget instanceof ItemWidget itemWidget) {
                    dynamicUtil.getWidgetManager().addWidget(itemWidget);
                }
            }
        }
    }

    @Override
    public Widget loadWidgetsFromTag(String className, NbtCompound widgetTag) {
        //SAMPLE CODE EXAMPLE :
        /*if (className.equals(ItemWidget.class.getName())) {
            ItemWidget widget = new ItemWidget(MinecraftClient.getInstance(), ItemStack.EMPTY, 0, 0, true, TextureHelper.Position.ABOVE, () -> "", Color.BLUE);
            widget.readFromTag(widgetTag);
            return widget;
        }*/
        return WidgetLoading.super.loadWidgetsFromTag(className, widgetTag);
    }
      /**
       * The following code in comments (Line 100-272) code is from SMP-HACK client (not present in the latest version)
       * This code shows how one can implement the library using just one class (or more, if you plan to add more widgets which I have done in that mod)
       * >>
       * I will add Documentation to understand this code better as it looks awful as a comment
       * This is basically using advanced dynamic ItemWidgets and ArmorWidgets
       * The code rn is kind of complicated for first time using this library (Dont worry, its gonna improve), But this is better than writing 10 different classes from scratch. (Not an excuss for being a bad coder)
       * As an excuse, I will write a really good documentation explaining how to use even the smallest feature. (I will also setup a template with each of the widgets with dynamic texts and colors)
       */
      private void NoDocumentationDangling(){}
  /*protected List<Widget> widgets = new ArrayList<>();
    protected boolean WidgetAdded = false;
    protected boolean WidgetLoaded = false;
    MinecraftClient mc = MinecraftClient.getInstance();
    net.minecraft.item.ItemStack[] ItemStack = new ItemStack[3];
    private DynamicUtil dynamicutil;
    private String ArmorDurabilityDisplay;
    private Color color;

    private void InventoryChange() {
        // Update the values of the ItemStack objects in the array
        ItemStack[0] = mc.player.getInventory().get;
        ItemStack[1] = mc.player.getInventory().getStack(1);
        ItemStack[2] = mc.player.getInventory().getStack(3);
    }
    //To change the stack if the player changes his inventory
    private void changeStack(WidgetManager widgetManager)
    {
        int stackIndex=0;
        for (Widget widget: widgetManager.getWidgets())
        {
            if (widget instanceof ItemWidget itemWidget)
            {
                ItemStack itemStack = ItemStack[stackIndex++];
                itemWidget.setItemStack(()->itemStack);
            }
        }
    }

    @Override
    public void onInitializeClient() {
        dynamicutil = new DynamicUtil(mc);
        widgets.clear();

        // Add default widgets if this is the first run
        ClientTickEvents.START_CLIENT_TICK.register(server -> {
            if (!WIDGETS_FILE.exists() && mc.player != null && !WidgetAdded) {
                addWigdets(dynamicutil);
            }
            if (mc.player != null) InventoryChange();
            if (WIDGETS_FILE.exists() && mc.player != null && !WidgetLoaded) {
                loadWigdets(dynamicutil);
                WidgetLoaded = true;
            }
            changeStack(dynamicutil.getWidgetManager());
        });

        DynamicHUD.setAbstractScreen(new MoveableScreenExtension(Text.of("Editor Screen"), dynamicutil));

        ServerLifecycleEvents.SERVER_STOPPED.register(client -> {
            dynamicutil.getWidgetManager().saveWidgets(WIDGETS_FILE);
        });
        HudRenderCallback.EVENT.register((matrices, tickDelta) -> {
            dynamicutil.render(matrices, tickDelta);
        });
    }

    public void addWigdets(DynamicUtil dynamicUtil) {
        assert mc.player != null;

        widgets.add(new ArmorWidget(mc, EquipmentSlot.CHEST, 0.01f, 0.01f, true, TextureHelper.Position.ABOVE, () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.CHEST)), () -> color));
        widgets.add(new ArmorWidget(mc, EquipmentSlot.HEAD, 0.03f, 0.01f, true, TextureHelper.Position.BELOW, () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.HEAD)), () -> color));
        widgets.add(new ArmorWidget(mc, EquipmentSlot.LEGS, 0.05f, 0.01f, true, TextureHelper.Position.LEFT, () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.LEGS)), () -> color));

        widgets.add(new ItemWidget(mc, () -> mc.player.getInventory().getStack(4), 0.05f, 0.05f, true, TextureHelper.Position.ABOVE, () -> String.valueOf(getItemCount(4)), () -> Color.YELLOW));
        widgets.add(new ItemWidget(mc, () -> mc.player.getInventory().getStack(1), 0.1f, 0.1f, true, TextureHelper.Position.ABOVE, () -> String.valueOf(getItemCount(1)), () -> Color.YELLOW));
        widgets.add(new ItemWidget(mc, () -> mc.player.getInventory().getStack(3), 0.15f, 0.15f, true, TextureHelper.Position.ABOVE, () -> String.valueOf(getItemCount(3)), () -> Color.YELLOW));

        widgets.add(new Muppet(mc, 0.9f, 0.8f, true));

        for (Widget wigdet : widgets) {
            dynamicutil.getWidgetManager().addWidget(wigdet);
        }
        WidgetAdded = true;
    }

    private int getItemCount(int slot) {
        return mc.player.getInventory().count(mc.player.getInventory().getStack(slot).getItem());
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private String getDurabilityForStack(ItemStack stack) {
        String durabilityText = null;
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            ArmorDurabilityDisplay = String.valueOf(ConfigUtil.config.ArmorDurability);
            double durabilityPercentage = ((double) stack.getMaxDamage() - (double) stack.getDamage()) / (double) stack.getMaxDamage();
            durabilityText = (int) (durabilityPercentage * 100.0D) + "%";
            if (durabilityPercentage > 0.65D) setColor(Color.GREEN); // green
            else if (durabilityPercentage > 0.25D) setColor(Color.YELLOW); // yellow
            else setColor(Color.RED);
            switch (ArmorDurabilityDisplay) {
                case "Percent" -> {
                    //Percent Display
                    return (stack.getDamage() > 0) ? durabilityText : null;
                }
                case "Bar" -> {
                    // Bar display
                    int barWidth = (int) (durabilityPercentage * 16);
                    return (stack.getDamage() > 0) ? String.valueOf(barWidth) : null;
                }
//                  DrawHelper.fill(matrices,Wodge,mc.textRenderer.fontHeight*0.5f,);
//                  DrawableHelper.drawBorder(matrices, x + i * 20 -1 , (y-30)*2 , x + i * 20  + barWidth , (y-26)*2 , color);
                case "Number" -> {
                    //Number display
                    return (stack.getDamage() > 0) ? stack.getMaxDamage() - stack.getDamage() + "/" + stack.getMaxDamage() : null;
                }
            }
        }
        return durabilityText;
    }

    public void loadWigdets(DynamicUtil dynamicUtil) {
        dynamicUtil.getWidgetManager().setWidgetLoading(new DynamicHUDLoading());
        List<Widget> widgets = dynamicUtil.getWidgetManager().loadWigdets(WIDGETS_FILE);
        int armorIndex = 0;
        int itemTextIndex = 0;
        int itemStackIndex = 0;
        assert mc.player != null;
        TextGenerator[] ArmorWidgetText = new TextGenerator[]{
                () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.CHEST)),
                () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.HEAD)),
                () -> getDurabilityForStack(mc.player.getEquippedStack(EquipmentSlot.LEGS))
        };

        TextGenerator[] ItemWidgetText = new TextGenerator[]{
                () -> String.valueOf(getItemCount(4)),
                () -> String.valueOf(getItemCount(1)),
                () -> String.valueOf(getItemCount(3))
        };

        for (Widget widget : widgets) {
            if (widget instanceof ArmorWidget armorWidget) {
                if (armorIndex<3) {
                    TextGenerator textGenerator = ArmorWidgetText[armorIndex++];
                  //  TextGenerator textGenerator = ()-> String.valueOf(mc.player.getInventory().count(Items.TOTEM_OF_UNDYING));
                    armorWidget.setTextGenerator(textGenerator);
                    armorWidget.setColor(() -> color);
                }
                dynamicutil.getWidgetManager().addWidget(armorWidget);
            }
            if (widget instanceof ItemWidget itemWidget) {
                TextGenerator textGenerator = ItemWidgetText[itemTextIndex++];
                itemWidget.setTextGenerator(textGenerator);
                ItemStack itemStack = ItemStack[itemStackIndex++];
                itemWidget.setItemStack(() -> itemStack);
                dynamicutil.getWidgetManager().addWidget(itemWidget);
            }
            if (widget instanceof Muppet muppet) {
                dynamicutil.getWidgetManager().addWidget(muppet);
            }
        }
    }

    @Override
    public Widget loadWidgetsFromTag(String className, NbtCompound widgetTag) {

        if (className.equals(Muppet.class.getName())) {
            Muppet widget = new Muppet(MinecraftClient.getInstance(), 0, 0, true);
            widget.readFromTag(widgetTag);
            return widget;
        }
        return WidgetLoading.super.loadWidgetsFromTag(className, widgetTag);
    }
*/
}


