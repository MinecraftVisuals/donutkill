package me.killgui;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class KillGUI extends JavaPlugin implements Listener {

    private static final int RED_SLOT = 9;
    private static final int CENTER_SLOT = 13;
    private static final int GREEN_SLOT = 17;

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("KillGUI enabled.");
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        if (isKillInput(event.getMessage())) {
            event.setCancelled(true);
            openGui(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        String text = PlainTextComponentSerializer.plainText().serialize(event.message());
        if (text.startsWith(".") && isKillInput(text)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            Bukkit.getScheduler().runTask(this, () -> openGui(player));
        }
    }

    private boolean isKillInput(String raw) {
        String s = raw.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith(".")) s = s.substring(1);
        if (s.startsWith("/")) s = s.substring(1);
        s = s.trim();
        return s.equals("kill") || s.equals("minecraft:kill");
    }

    private void openGui(Player player) {
        KillHolder holder = new KillHolder();
        Inventory inv = Bukkit.createInventory(holder, 27,
                Component.text("Are you sure?", NamedTextColor.DARK_RED));
        holder.setInventory(inv);

        inv.setItem(RED_SLOT, button(Material.RED_STAINED_GLASS,
                Component.text("Exit", NamedTextColor.RED)));
        inv.setItem(CENTER_SLOT, button(Material.SKELETON_SKULL,
                Component.text("Kill yourself?", NamedTextColor.GRAY)));
        inv.setItem(GREEN_SLOT, button(Material.LIME_STAINED_GLASS,
                Component.text("Kill me", NamedTextColor.GREEN)));

        player.openInventory(inv);
    }

    private ItemStack button(Material material, Component name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(name.decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof KillHolder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof KillHolder)) return;

        int slot = event.getSlot();
        if (slot == GREEN_SLOT) {
            player.closeInventory();
            player.setHealth(0.0);
        } else if (slot == RED_SLOT) {
            player.closeInventory();
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getView().getTopInventory().getHolder() instanceof KillHolder) {
            event.setCancelled(true);
        }
    }

    public static class KillHolder implements InventoryHolder {
        private Inventory inventory;

        public void setInventory(Inventory inventory) {
            this.inventory = inventory;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return inventory;
        }
    }
}
