package net.minezero.writebook;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WriteBook extends JavaPlugin {
    FileConfiguration config;
    //本一覧
    Map<String, ItemStack> books = new HashMap<>();
    //本の名前一覧
    List<String> bookNames = new ArrayList<>();
    VaultManager vault;
    //本の値段
    Map<String,Double> pri = new HashMap<>();

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        loadBooks();
        vault = new VaultManager();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equals("wbook")) {
            if (args.length == 0) {
                sender.sendMessage("/wbook get ここ選択制 : 本を入手します");
                if (sender.isOp()) {
                    sender.sendMessage("/wbook add 他と被らないわかりやすい名前 価格 : 手に持ってる書かれた本を誰でもゲットできるようにします");
                    sender.sendMessage("/wbook rem 選択制 : その本を削除します");
                }
                return true;
            }
            if (args[0].equals("get")) {
                if (args.length != 2) {
                    sender.sendMessage("値の数が違います");
                    return true;
                }
                if (!bookNames.contains(args[1])) {
                    sender.sendMessage("その本は見つかりませんでした");
                    return true;
                }
                Player p = (Player) sender;
                if (p.getInventory().firstEmpty() == -1) {
                    sender.sendMessage("インベントリに空きがありません");
                    return true;
                }
                if (vault.getBalance(p) < pri.get(args[1])) {
                    sender.sendMessage("所持金が足りません");
                    return true;
                }
                vault.withdraw(p,pri.get(args[1]));
                p.getInventory().addItem(books.get(args[1]));
                sender.sendMessage("ほい");
                return true;
            }
            if (!sender.isOp()) {
                return true;
            }
            if (args[0].equals("add")) {
                if (args.length != 3) {
                    sender.sendMessage("値の数が違います");
                    return true;
                }
                double price;
                try {
                    price = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("数字を入力してください");
                    return true;
                }
                Player p = (Player) sender;
                if (p.getInventory().getItemInMainHand().getType() != Material.WRITTEN_BOOK) {
                    sender.sendMessage("記入済みの本を持ってください");
                    return true;
                }
                bookNames.add(args[1]);
                pri.put(args[1],price);
                books.put(args[1],p.getInventory().getItemInMainHand());
                ItemStack item = p.getInventory().getItemInMainHand();
                BookMeta meta = (BookMeta) item.getItemMeta();
                ConfigurationSection section = config.getConfigurationSection("books");
                section.set(args[1]+".price",price);
                section.set(args[1]+".title",meta.getTitle());
                section.set(args[1]+".author",meta.getAuthor());
                List<String> list = new ArrayList<>();
                for (Component str : meta.pages()) {
                    list.add(ComponentUtil.componentToString(str));
                }
                section.set(args[1]+".pages",list);
                saveConfig();
                sender.sendMessage("保存しました");
                return true;
            }
            if (args[0].equals("rem")) {
                if (args.length != 2) {
                    sender.sendMessage("値の数が足りません");
                    return true;
                }
                if (!bookNames.contains(args[1])) {
                    sender.sendMessage("その本はありません");
                    return true;
                }
                bookNames.remove(args[1]);
                books.remove(args[1]);
                pri.remove(args[1]);
                config.set("books."+args[1],null);
                saveConfig();
                sender.sendMessage("削除しました");
                return true;
            }
        }
        return true;
    }

    void loadBooks() {
        ConfigurationSection section = config.getConfigurationSection("books");
        for (String name : section.getKeys(false)) {
            ItemStack item = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) item.getItemMeta();
            meta.setTitle(section.getString(name+".title"));
            Bukkit.getLogger().info(section.getString(name+".title"));
            meta.setAuthor(section.getString(name+".author"));
            for (String str : section.getStringList(name+".pages")) {
                meta.addPages(ComponentUtil.makeC(str.replaceAll("<br>", "\n")));
            }
            item.setItemMeta(meta);
            books.put(name,item);
            pri.put(name,section.getDouble(name+".price"));
        }
        bookNames.addAll(section.getKeys(false));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 2) {
            if (args[0].equals("get") || args[0].equals("rem")) {
                return bookNames;
            }
        }
        return null;
    }
}
