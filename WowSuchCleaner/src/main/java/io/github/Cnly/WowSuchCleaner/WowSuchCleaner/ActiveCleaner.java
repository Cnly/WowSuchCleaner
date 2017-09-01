package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.RegionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WorldRegion;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning.CleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;
import org.bukkit.scheduler.BukkitRunnable;

public class ActiveCleaner
{
    
    private Main main = Main.getInstance();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private RegionalConfigManager regionalConfigManager = main.getRegionalConfigManager();
    private ILocaleManager localeManager = main.getLocaleManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    private Map<ApproximateBoxRegion, RegionConfig> regionConfigMap = regionalConfigManager.getRegionConfigMap();
    private Map<String, Integer> profileInitialTimeMap = new HashMap<>(); // Store initial time to start with for each cleaning profile. initial time = interval - 1 (seconds)
    private Map<String, Integer> profileCurrentTimeMap = new HashMap<>(); // Store current countdown time for each cleaning profile.
    
    public ActiveCleaner()
    {
        load();
        setupTimer();
    }
    
    private void load()
    {
        for(Map.Entry<ApproximateBoxRegion, RegionConfig> e : regionConfigMap.entrySet())
        {
            CleaningConfig cleaningConfig = e.getValue().getCleaningConfig();
            if(cleaningConfig.isActiveCleaningEnabled())
            {
                profileInitialTimeMap.put(cleaningConfig.getProfileName(), cleaningConfig.getIntervalInSeconds() - 1);
                profileCurrentTimeMap.put(cleaningConfig.getProfileName(), cleaningConfig.getIntervalInSeconds());
            }
        }
    }
    
    public void regionCreated(RegionConfig regionConfig)
    {
        CleaningConfig cleaningConfig = regionConfig.getCleaningConfig();
        if(cleaningConfig.isActiveCleaningEnabled())
        {
            profileInitialTimeMap.put(cleaningConfig.getProfileName(), cleaningConfig.getIntervalInSeconds() - 1);
            profileCurrentTimeMap.put(cleaningConfig.getProfileName(), cleaningConfig.getIntervalInSeconds());
        }
    }
    
    public void regionRemoved(RegionConfig regionConfig)
    {
        CleaningConfig cleaningConfig = regionConfig.getCleaningConfig();
        if(cleaningConfig.isActiveCleaningEnabled())
        {
            profileInitialTimeMap.remove(cleaningConfig.getProfileName());
            profileCurrentTimeMap.remove(cleaningConfig.getProfileName());
        }
    }
    
    private void setupTimer()
    {
        new ActiveCleanerTimer().runTaskTimer(main, 0L, 20L);
    }
    
    private class ActiveCleanerTimer extends BukkitRunnable
    {
    
        @Override
        public void run()
        {
            doPreCleanNotifyAll();
            doClean();
            proceedTime();
        }
    
        private void doPreCleanNotifyAll()
        {
            for(Map.Entry<ApproximateBoxRegion, RegionConfig> e : regionConfigMap.entrySet())
            {
                CleaningConfig cleaningConfig = e.getValue().getCleaningConfig();
                if(!cleaningConfig.isActiveCleaningEnabled())
                {
                    continue;
                }
                String profileName = cleaningConfig.getProfileName();
                int time = profileCurrentTimeMap.get(profileName);
                if(0 == time)
                {// 0 is not a pre-clean state
                    continue;
                }
                String notification = cleaningConfig.getNotification(time);
                if(null != notification)
                {
                    if(cleaningConfig.isInRegionNotification())
                    {
                        Utils.broadcastRegionalNotification(e.getKey(), notification, cleaningConfig.isClickableCleaningNotification());
                    }
                    else
                    {
                        Utils.broadcastGlobalNotification(notification, cleaningConfig.isClickableCleaningNotification());
                    }
                }
            }
        }
    
        private void doClean()
        {
        
            Map<ApproximateBoxRegion, ArrayList<Item>> itemsToClean = new HashMap<>();
            Map<ApproximateBoxRegion, ArrayList<Item>> itemsToAuction = new HashMap<>();
        
            for(World w : Bukkit.getWorlds())
            {
    
                RegionConfig worldConfig = regionalConfigManager.getRegionConfig(new WorldRegion(w.getName()));
                if(null != worldConfig && !worldConfig.getCleaningConfig().isActiveCleaningEnabled())
                {
                    continue;
                }
                
                for(Entity e : w.getEntities())
                {
                    if(e instanceof Item)
                    {
                    
                        RegionConfig regionConfig = regionalConfigManager.getRegionConfig(e.getLocation());
                        ApproximateBoxRegion region = regionConfig.getRegion();
                        CleaningConfig cleaningConfig = regionConfig.getCleaningConfig();
                        
                        if(!cleaningConfig.isActiveCleaningEnabled() || profileCurrentTimeMap.get(cleaningConfig.getProfileName()) != 0)
                        {
                            continue;
                        }
                    
                        if(e.getTicksLived() < cleaningConfig.getGenerousDelayInTicks()) continue;
                        Item item = (Item)e;
                        ItemStack is = item.getItemStack();
                        if(cleaningConfig.isPreservedItem(is)) continue;
                        smartAddToMapList(itemsToClean, region, item);
                        ArrayList<Item> itemsToAuctionForRegion = smartGetFromMapList(itemsToAuction, region);
                    
                        if(cleaningConfig.isActiveCleaningAuction())
                        {
                        
                            if(cleaningConfig.isAutoMerge())
                            {
                                for(Item cleanedItemEntity : itemsToAuctionForRegion)
                                {
                                    ItemStack cleanedItem = cleanedItemEntity.getItemStack();
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
                                itemsToAuctionForRegion.add(item);
                            }
                        
                        }
                    
                    }
                }
            
            }
        
            ItemPreCleanEvent ipce = new ItemPreCleanEvent(itemsToClean, itemsToAuction);
            Bukkit.getPluginManager().callEvent(ipce);
        
            if(!ipce.isCancelled())
            {
            
                // Handle items on regional basis
            
                itemsToClean = ipce.getItemsToClean();
                itemsToAuction = ipce.getItemsToAuction();
                for(Map.Entry<ApproximateBoxRegion, RegionConfig> e : regionalConfigManager.getRegionConfigMap().entrySet())
                {
                    
                    ApproximateBoxRegion region = e.getKey();
                    ArrayList<Item> itemsToCleanForRegion = itemsToClean.get(region);
                    ArrayList<Item> itemsToAuctionForRegion = itemsToAuction.get(region);
                    CleaningConfig cleaningConfig = e.getValue().getCleaningConfig();
    
                    if(!cleaningConfig.isActiveCleaningEnabled() || profileCurrentTimeMap.get(cleaningConfig.getProfileName()) != 0)
                    {
                        continue;
                    }
    
                    if(null != itemsToCleanForRegion)
                    {
                        for(Item item : itemsToCleanForRegion)
                        {
                            item.remove();
                        }
                    }
    
                    boolean isAuction = cleaningConfig.isActiveCleaningAuction();
                    if(isAuction && null != itemsToAuctionForRegion)
                    {
                        auctionDataManager.addLots(itemsToAuctionForRegion);
                    }
    
                    String msg = cleaningConfig.getNotification(0)
                                               .replace("{count}", null != itemsToCleanForRegion ?
                                                                   String.valueOf(itemsToCleanForRegion.size()) : "0")
                                               .replace("{auctionCount}",
                                                        !isAuction || null == itemsToAuctionForRegion ? "0" :
                                                        String.valueOf(itemsToAuctionForRegion.size()));
    
                    if(cleaningConfig.isInRegionNotification())
                    {
                        Utils.broadcastRegionalNotification(region, msg, cleaningConfig.isClickableCleaningNotification());
                    }
                    else
                    {
                        Utils.broadcastGlobalNotification(msg, cleaningConfig.isClickableCleaningNotification());
                    }
                
                }
            
            }
        
        }
    
        private void proceedTime()
        {
            for(Map.Entry<String, Integer> e : profileCurrentTimeMap.entrySet())
            {
                Integer value = e.getValue();
                if(value <= 0)
                {
                    e.setValue(profileInitialTimeMap.get(e.getKey()));
                }
                else
                {
                    e.setValue(value - 1);
                }
            }
        }
    
        private void smartAddToMapList(Map<ApproximateBoxRegion, ArrayList<Item>> maplist, ApproximateBoxRegion region, Item item)
        {
        
            ArrayList<Item> list = maplist.get(region);
            if(null == list)
            {
                list = new ArrayList<>();
                maplist.put(region, list);
            }
            list.add(item);
        
        }
    
        private ArrayList<Item> smartGetFromMapList(Map<ApproximateBoxRegion, ArrayList<Item>> maplist, ApproximateBoxRegion region)
        {
        
            ArrayList<Item> list = maplist.get(region);
            if(null == list)
            {
                list = new ArrayList<>();
                maplist.put(region, list);
            }
        
            return list;
        }
        
    }
    
}
