package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import io.github.Cnly.Crafter.Crafter.utils.CompatUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils
{
    
    private Utils()
    {
        throw new AssertionError("This is a utility class");
    }
    
    public static void broadcastCleaningNotification(String msg, boolean clickable)
    {
        if(clickable)
        {
            for(Player p : CompatUtils.getOnlinePlayers())
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(
                        "tellraw %s {\"text\":\"%s\",\"extra\":[{\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/wsc showcase\"}}]}",
                        p.getName(), msg));
            }
        }
        else
        {
            Bukkit.broadcastMessage(msg);
        }
    }
    
}
