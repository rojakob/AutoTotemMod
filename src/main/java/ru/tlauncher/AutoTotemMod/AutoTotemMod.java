package ru.tlauncher.safewalker; // Или твой пакет

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;

@Mod(modid = AutoTotemMod.MODID, name = AutoTotemMod.NAME, version = AutoTotemMod.VERSION)
public class AutoTotemMod {

    public static final String MODID = "autototem";
    public static final String NAME = "Auto Totem";
    public static final String VERSION = "1.0";

    private static KeyBinding keyBind;
    private static boolean isActive = false;

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        keyBind = new KeyBinding("key.autototem.activate", Keyboard.KEY_B, "key.categories.movement");
        ClientRegistry.registerKeyBinding(keyBind);
        MinecraftForge.EVENT_BUS.register(this);
        System.out.println("AutoTotem Mod загружен! Нажми B для активации.");
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) return;

        if (keyBind.isPressed()) {
            isActive = !isActive;
            String message = "AutoTotem: " + (isActive ? "§aВключен" : "§cВыключен");
            mc.player.sendMessage(new TextComponentString(message));
        }
    }

    @SubscribeEvent
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Работаем только на клиенте, в конце тика, для локального игрока
        if (event.phase != TickEvent.Phase.END || !event.player.world.isRemote) return;

        EntityPlayer player = event.player;
        if (player != Minecraft.getMinecraft().player || !isActive) return;

        // 1. Быстрая проверка: если в левой руке уже есть тотем — ничего не делаем
        ItemStack offHand = player.getHeldItem(EnumHand.OFF_HAND);
        if (offHand.getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        // 2. Если левая рука пуста, ищем тотем в инвентаре
        // В 1.12.2 mainInventory включает и хотбар, и основной инвентарь
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                // Нашли тотем! Перемещаем в левую руку.
                // Создаём копию стека (обычно 1 шт.), чтобы не потерять данные
                ItemStack totemToEquip = stack.copy();

                // Устанавливаем в оффхенд
                player.setHeldItem(EnumHand.OFF_HAND, totemToEquip);

                // Очищаем исходный слот
                player.inventory.mainInventory.set(i, ItemStack.EMPTY);

                // Выходим из цикла, так как рука уже занята
                break;
            }
        }
    }
}