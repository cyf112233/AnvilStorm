package top.cxkcxkckx.yahu;
        
import org.jetbrains.annotations.NotNull;
import top.mrxiaom.pluginbase.BukkitPlugin;
import top.mrxiaom.pluginbase.EconomyHolder;
import top.cxkcxkckx.yahu.func.AnvilThrow;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ClickEvent;

public class yahu extends BukkitPlugin {
    public static yahu getInstance() {
        return (yahu) BukkitPlugin.getInstance();
    }

    public yahu() {
        super(options()
                .bungee(false)
                .adventure(false)
                .database(false)
                .reconnectDatabaseWhenReloadConfig(false)
                .vaultEconomy(false)
                .scanIgnore("top.mrxiaom.example.libs")
        );
    }

    @Override
    protected void afterEnable() {
        // 保存默认配置文件
        saveDefaultConfig();
        // 加载配置文件
        reloadConfig();
        
        // ANSI 颜色代码
        String GREEN = "\u001B[32m";
        String PURPLE = "\u001B[35m";
        String BLUE = "\u001B[34m";
        String YELLOW = "\u001B[33m";
        String RESET = "\u001B[0m";
        
        // 创建带颜色的 ASCII 艺术字
        String asciiArt = "\n" +
                GREEN + "----------------------------------------------------------------" + RESET + "\n" +
                PURPLE + "                       _  _" + BLUE + "     _                               " + RESET + "\n" +
                PURPLE + "   /\\                 (_)| |" + BLUE + "   | |    _                         " + RESET + "\n" +
                PURPLE + "  /  \\   ____   _   _  _ | |" + BLUE + "    \\ \\  | |_    ___    ____  ____  " + RESET + "\n" +
                PURPLE + " / /\\ \\ |  _ \\ | | | || || |" + BLUE + "     \\ \\ |  _)  / _ \\  / ___)|    \\ " + RESET + "\n" +
                PURPLE + "| |__| || | | | \\ V / | || |" + BLUE + " _____) )| |__ | |_| || |    | | | |" + RESET + "\n" +
                PURPLE + "|______||_| |_|  \\_/  |_||_|" + BLUE + "(______/  \\___) \\___/ |_|    |_|_|_|" + RESET + "\n" +
                PURPLE + "                                                                " + RESET + "\n" +
                YELLOW + "AnvilStorm 已加载 - 铁砧风暴来袭！" + RESET + "\n" +
                GREEN + "----------------------------------------------------------------" + RESET;
        
        getLogger().info(asciiArt);
        // 注册铁砧投掷功能
        new AnvilThrow(this).onEnable();
    }
}
