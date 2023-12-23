package lc.lcaccounts.utilidades;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;

public class Util {

    public static String color(String string){
        return ChatColor.translateAlternateColorCodes('&', string);
    }

    public static void sendMessage(CommandSender sender, String msg){
        //noinspection deprecation
        sender.sendMessage(color(msg));
    }

    public static void console(String msg){
        //noinspection deprecation
        ProxyServer.getInstance().getConsole().sendMessage(color(msg));
    }
}
