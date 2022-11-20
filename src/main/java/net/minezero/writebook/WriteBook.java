package net.minezero.writebook;

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

import java.awt.*;
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

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        config = getConfig();
        loadBooks();
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
                    sender.sendMessage("/wbook add 他と被らないわかりやすい名前 : 手に持ってる書かれた本を誰でもゲットできるようにします");
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
                p.getInventory().addItem(books.get(args[1]));
                sender.sendMessage("ほい");
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
            meta.setTitle(section.getString("title"));
            meta.setAuthor(section.getString("author"));
            for (String str : section.getStringList("pages")) {
                meta.addPages(ComponentUtil.makeC(str));
            }
            item.setItemMeta(meta);
            books.put(name,item);
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
