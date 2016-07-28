package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.util.ArrayList;

import io.github.Cnly.Crafter.Crafter.utils.Timer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.activecleaning.ActiveCleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

public class ActiveCleaner
{
    
    private Main main = Main.getInstance();
    private ActiveCleaningConfig activeCleaningConfig = main.getActiveCleaningConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    private boolean isAuction = activeCleaningConfig.isAuction();
    private int generousDelayInTicks = activeCleaningConfig.getGenerousDelayInTicks();
    
    private Timer timer = new Timer(main, activeCleaningConfig.getIntervalInSeconds(), true, new Timer.TimerListener()
    {
    
        @Override
        public void onStart(final Timer timer)
        {
            Bukkit.getScheduler().runTask(main, new Runnable()
            {
                @Override
                public void run()
                {
                    if(activeCleaningConfig.getNotifyTimes().contains((int)timer.getCurrent()))
                    {
                        Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.preNotify")
                                                             .replace("{time}", String.valueOf(timer.getCurrent())));
                    }
                }
            });
        }
    
        @Override
        public void onRepeatStart(Timer timer)
        {
            onTick(timer);
        }
    
        @Override
        public void onTick(Timer timer)
        {
            if(activeCleaningConfig.getNotifyTimes().contains((int)timer.getCurrent()))
            {
                Bukkit.broadcastMessage(localeManager.getLocalizedString("cleaning.preNotify")
                                                     .replace("{time}", String.valueOf(timer.getCurrent())));
            }
        }
    
        @Override
        public void onReachGoal(Timer timer)
        {
            
            ArrayList<Item> itemsToClean = new ArrayList<>();
            ArrayList<ItemStack> itemsToAuction = isAuction ? new ArrayList<ItemStack>() : null;
    
            for(World w : Bukkit.getWorlds())
            {
                for(Entity e : w.getEntities())
                {
                    if(e instanceof Item)
                    {
                        
                        if(e.getTicksLived() < generousDelayInTicks) continue;
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
    
                String message = localeManager.getLocalizedString("cleaning.cleanNotify")
                                              .replace("{count}", String.valueOf(ipce.getItemsToClean().size()))
                                              .replace("{auctionCount}", String.valueOf(
                                                      isAuction ? ipce.getItemsToAuction().size() : 0));
                Utils.broadcastCleaningNotification(message, activeCleaningConfig.isClickableCleaningNotification());
        
            }
            
        }
        
    });
    
    public void start()
    {
        timer.start();
    }
    
}
