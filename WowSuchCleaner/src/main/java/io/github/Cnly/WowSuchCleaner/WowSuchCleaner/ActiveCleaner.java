package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.activecleaning.ActiveCleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

public class ActiveCleaner extends BukkitRunnable
{
    
    private Main main = Main.getInstance();
    private ActiveCleaningConfig activeCleaningConfig = main.getActiveCleaningConfig();
    private CrafterLocaleManager localeManager = main.getLocaleManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    private int secondsRemaining = activeCleaningConfig.getIntervalInSeconds();
    
    @Override
    public void run()
    {
        
        secondsRemaining--;
        
        if(activeCleaningConfig.getNotifyTimes().contains(secondsRemaining))
        {
            Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.preNotify")
                    .replace("{time}", String.valueOf(secondsRemaining)));
        }
        
        if(0 == secondsRemaining)
        {
            
            int count = 0;
            int auctionCount = 0;
            
            for(World w : Bukkit.getWorlds())
            {
                for(Entity e : w.getEntities())
                {
                    if(e instanceof Item)
                    {
                        Item item = (Item)e;
                        if(activeCleaningConfig.getPreservedItems().contains(item.getItemStack())) continue;
                        boolean auction = auctionDataManager.addLot(item.getItemStack());
                        item.remove();
                        count++;
                        if(auction) auctionCount++;
                    }
                }
            }
            
            Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.cleanNotify")
                    .replace("{count}", String.valueOf(count))
                    .replace("{auctionCount}", String.valueOf(auctionCount)));
            
            secondsRemaining = activeCleaningConfig.getIntervalInSeconds();
            
            return;
        }
        
    }
    
}
