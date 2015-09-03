package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.activecleaning.ActiveCleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

public class ActiveCleaner extends BukkitRunnable
{
    
    private Main main = Main.getInstance();
    private ActiveCleaningConfig activeCleaningConfig = main.getActiveCleaningConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    private boolean isAuction = activeCleaningConfig.isAuction();
    
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
            ArrayList<ItemStack> cleanedItems = isAuction ? new ArrayList<ItemStack>() : null;
            
            for(World w : Bukkit.getWorlds())
            {
                for(Entity e : w.getEntities())
                {
                    if(e instanceof Item)
                    {
                        
                        Item item = (Item)e;
                        ItemStack is = item.getItemStack();
                        if(activeCleaningConfig.isPreservedItem(is)) continue;
                        item.remove();
                        count++;
                        
                        if(isAuction)
                        {
                            
                            if(activeCleaningConfig.isAutoMerge())
                            {
                                for(ItemStack cleanedItem : cleanedItems)
                                {
                                    if(cleanedItem.isSimilar(is) && cleanedItem.getAmount() < cleanedItem.getMaxStackSize())
                                    {
                                        
                                        int slotLeft = cleanedItem.getMaxStackSize() - cleanedItem.getAmount();
                                        
                                        if(is.getAmount() <= slotLeft)
                                        {
                                            cleanedItem.setAmount(cleanedItem.getAmount() + is.getAmount());
                                            is.setAmount(0);
                                            break;
                                        }
                                        else
                                        {
                                            cleanedItem.setAmount(cleanedItem.getMaxStackSize());
                                            is.setAmount(is.getAmount() - slotLeft);
                                            continue;
                                        }
                                        
                                    }
                                }
                            }
                            
                            if(is.getAmount() != 0)
                            {
                                cleanedItems.add(is);
                            }
                            
                            auctionCount++;
                            
                        }
                        
                    }
                }
            }
            
            if(isAuction) auctionDataManager.addLots(cleanedItems);
            
            Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.cleanNotify")
                    .replace("{count}", String.valueOf(count))
                    .replace("{auctionCount}", String.valueOf(auctionCount)));
            
            secondsRemaining = activeCleaningConfig.getIntervalInSeconds();
            
            return;
        }
        
    }
    
}
