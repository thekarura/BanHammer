package net.tkarura.banhammer;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class BanHammer extends JavaPlugin implements Listener {

    private NamespacedKey namespacedKey;

    @Override
    public void onEnable() {

        try {
            namespacedKey = new NamespacedKey(this, "banhammer");
            getServer().getPluginManager().registerEvents(this, this);
            getLogger().log(Level.INFO, "plugin has enabled.");
        } catch (Exception e) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().log(Level.SEVERE, "plugin enable error. {0}", e.getLocalizedMessage());
        }

    }

    @Override
    public void onDisable() {
        getLogger().log(Level.INFO ,"plugin has disabled.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("in game only.");
            return true;
        }

        // 第一引数以降から理由を構成
        String reason = ChatColor.RED + "You has been banned.";
        if (args.length > 0) {
            reason = Arrays.stream(args).skip(1).collect(Collectors.joining("\n"));
        }

        Player player = (Player) sender;
        player.getInventory().addItem(createBanHammerItem(reason));
        player.sendMessage(ChatColor.BOLD + "" + ChatColor.DARK_RED + "give item. " + ChatColor.UNDERLINE + "'BanHammer'");

        return true;
    }

    private ItemStack createBanHammerItem(String reason) {
        ItemStack itemStack = new ItemStack(Material.ANVIL);
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {

            itemMeta.setDisplayName(ChatColor.DARK_RED + "" + ChatColor.BOLD + "The BanHammer");
            itemMeta.setUnbreakable(true);

            itemMeta.addEnchant(Enchantment.LOOT_BONUS_MOBS, 1, true);
            itemMeta.addEnchant(Enchantment.LUCK, 1, true);
            itemMeta.addEnchant(Enchantment.THORNS, 1, true);
            itemMeta.addEnchant(Enchantment.DAMAGE_UNDEAD, 1, true);

            PersistentDataContainer container = itemMeta.getPersistentDataContainer();
            container.set(namespacedKey, PersistentDataType.STRING, reason);

            itemStack.setItemMeta(itemMeta);
        }
        return itemStack;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void damage(EntityDamageByEntityEvent event) {

        Player attacker = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
        Player target = event.getEntity() instanceof Player ? (Player) event.getEntity() : null;

        if (attacker == null || target == null) {
            return;
        }

        ItemStack itemStack = attacker.getInventory().getItemInMainHand();
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null) {
            return;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.has(namespacedKey, PersistentDataType.STRING);
        String reason = container.get(namespacedKey, PersistentDataType.STRING);
        if (reason == null) {
            return;
        }

        BanList banList = Bukkit.getBanList(BanList.Type.NAME);
        banList.addBan(target.getName(), reason, null, null);
        target.kickPlayer(reason);

        attacker.sendMessage(ChatColor.RED + "BanHammer attacked! " + target.getName() + " reason:" + reason);

    }

}
