package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import io.github.Cnly.Crafter.Crafter.utils.CompatUtils;
import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Utils
{
    
    private Utils()
    {
        throw new AssertionError("This is a utility class");
    }
    
    public static void broadcastGlobalNotification(String msg, boolean clickable)
    {
        if(clickable)
        {
            for(Player p : CompatUtils.getOnlinePlayers())
            {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(
                        "tellraw %s {\"text\":\"%s\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/wsc showcase\"}}",
                        p.getName(), msg));
            }
        }
        else
        {
            Bukkit.broadcastMessage(msg);
        }
    }
    
    public static void broadcastRegionalNotification(ApproximateBoxRegion region, String msg, boolean clickable)
    {
        if(clickable)
        {
            for(Player p : CompatUtils.getOnlinePlayers())
            {
                if(!region.isInRegion(p))
                {
                    continue;
                }
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), String.format(
                        "tellraw %s {\"text\":\"%s\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/wsc showcase\"}}",
                        p.getName(), msg));
            }
        }
        else
        {
            for(Player p : CompatUtils.getOnlinePlayers())
            {
                if(!region.isInRegion(p))
                {
                    continue;
                }
                p.sendMessage(msg);
            }
        }
    }
    
}
