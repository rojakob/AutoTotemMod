package ru.tlauncher.safewalker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
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
    public static final String VERSION = "1.1";

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
        if (event.phase != TickEvent.Phase.END) return;

        // 1. Отсекаем клиент. Мы работаем ТОЛЬКО на сервере!
        if (event.player.world.isRemote) {
            return;
        }

        EntityPlayer player = event.player;
        if (!isActive) return;

        // 2. Проверяем левую руку. Если там уже тотем - ничего не делаем (оптимизация).
        ItemStack offHand = player.getHeldItem(EnumHand.OFF_HAND);
        if (offHand.getItem() == Items.TOTEM_OF_UNDYING) {
            return;
        }

        // 3. Ищем тотем в инвентаре на сервере
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (!stack.isEmpty() && stack.getItem() == Items.TOTEM_OF_UNDYING) {
                
                // Перемещаем тотем на сервере
                player.setHeldItem(EnumHand.OFF_HAND, stack.copy());
                
                // Очищаем слот с помощью метода setInventorySlotContents (важно для синхронизации)
                player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);

                // Принудительно синхронизируем инвентарь с клиентом, чтобы игрок увидел изменения
                if (player instanceof EntityPlayerMP) {
                    ((EntityPlayerMP) player).sendContainerToPlayer(player.inventoryContainer);
                }
                
                break; // Выходим из цикла, как только нашли и переместили тотем
            }
        }
    }
}
