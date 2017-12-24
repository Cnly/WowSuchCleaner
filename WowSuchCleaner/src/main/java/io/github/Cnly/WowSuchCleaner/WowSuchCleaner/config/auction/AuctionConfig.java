package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Map;

public class AuctionConfig
{
    
    private String profileName;
    private boolean useAsBlacklist;
    private ArrayList<AuctionableItem> auctionableItems = new ArrayList<>();
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    
    public AuctionConfig(String profileName)
    {
        this.profileName = profileName;
        this.load();
    }
    
    public boolean isAuctionableItem(ItemStack item)
    {
        return getAuctionableItemConfig(item) != null;
    }
    
    public AuctionableItem getAuctionableItemConfig(ItemStack item)
    {
    
        if(!useAsBlacklist)
        {
            for(AuctionableItem ai : auctionableItems)
            {
                if(ai.isTheSameItem(item)) return ai;
            }
        }
        else
        {
            for(AuctionableItem ai : auctionableItems)
            {
                if(!(ai instanceof DefaultItem))
                {
                    if(ai.isTheSameItem(item)) return null;
                }
                else
                {
                    return ai;
                }
            }
        }
        
        return null;
    }
    
    private void load()
    {
    
        ConfigurationSection baseSection = config.getConfigurationSection("auction.profiles." + profileName);
        
        this.useAsBlacklist = baseSection.getBoolean("useAsBlacklist");
    
        for(Map<?, ?> map : baseSection.getMapList("auctionableItems"))
        {
            this.auctionableItems.add(AuctionableItem.fromMap(map));
        }
        
    }
    
    public String getProfileName()
    {
        return profileName;
    }
    
    public boolean isUseAsBlacklist()
    {
        return useAsBlacklist;
    }

}
