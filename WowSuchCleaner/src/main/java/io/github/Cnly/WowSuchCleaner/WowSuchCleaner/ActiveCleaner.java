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
            
            ArrayList<Item> itemsToClean = new ArrayList<>();
            ArrayList<ItemStack> itemsToAuction = isAuction ? new ArrayList<ItemStack>() : null;
            
            for(World w : Bukkit.getWorlds())
            {
                for(Entity e : w.getEntities())
                {
                    if(e instanceof Item)
                    {
                        
                        Item item = (Item)e;
                        ItemStack is = item.getItemStack();
                        if(activeCleaningConfig.isPreservedItem(is)) continue;
                        itemsToClean.add(item);
                        
                        if(isAuction)
                        {
                            
                            if(activeCleaningConfig.isAutoMerge())
                            {
                                for(ItemStack cleanedItem : itemsToAuction)
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
                                itemsToAuction.add(is);
                            }
                            
                        }
                        
                    }
                }
            }
            
            ItemPreCleanEvent ipce = new ItemPreCleanEvent(itemsToClean, itemsToAuction);
            Bukkit.getPluginManager().callEvent(ipce);
            
            if(!ipce.isCancelled())
            {
                
                for(Item item : ipce.getItemsToClean())
                {
                    item.remove();
                }
                
                if(isAuction) auctionDataManager.addLots(ipce.getItemsToAuction());
                
                Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.cleanNotify")
                        .replace("{count}", String.valueOf(ipce.getItemsToClean().size()))
                        .replace("{auctionCount}", String.valueOf(isAuction ? ipce.getItemsToAuction().size() : 0)));
                
            }
            
            secondsRemaining = activeCleaningConfig.getIntervalInSeconds();
            
            return;
        }
        
    }
    
}
