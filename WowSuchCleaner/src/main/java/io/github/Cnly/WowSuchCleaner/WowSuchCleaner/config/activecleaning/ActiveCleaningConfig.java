package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.activecleaning;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.Cnly.Crafter.Crafter.utils.ItemUtils;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class ActiveCleaningConfig
{
    
    private boolean enabled;
    private boolean auction;
    private boolean autoMerge;
    private int intervalInSeconds;
    private List<ItemStack> preservedItems;
    private List<Integer> notifyTimes;
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    
    public ActiveCleaningConfig()
    {
        this.load();
    }

    private void load()
    {
        
        this.preservedItems = new ArrayList<>();
        this.notifyTimes = new ArrayList<>();
        
        this.enabled = config.getBoolean("cleaning.active.enabled");
        this.auction = config.getBoolean("cleaning.active.auction");
        this.autoMerge = config.getBoolean("cleaning.active.autoMerge");
        this.intervalInSeconds = config.getInt("cleaning.active.intervalInSeconds");
        
        List<String> tempPreservedItems = config.getStringList("cleaning.active.preservedItems");
        for(String s : tempPreservedItems)
        {
            this.preservedItems.add(ItemUtils.getItemByIdString(s));
        }
        
        List<String> tempNotifyTimes = config.getStringList("cleaning.active.notify");
        for(String s : tempNotifyTimes)
        {
            this.notifyTimes.add(Integer.parseInt(s));
        }
        
    }
    
    public boolean isPreservedItem(ItemStack item)
    {
        for(ItemStack i : preservedItems)
        {
            if(item.getType() != i.getType()) continue;
            if(i.getType().getMaxDurability() == 0)
            {
                if(item.getDurability() != i.getDurability()) continue;
            }
            return true;
        }
        return false;
    }

    public boolean isEnabled()
    {
        return enabled;
    }

    public boolean isAuction()
    {
        return auction;
    }
    
    public boolean isAutoMerge()
    {
        return autoMerge;
    }

    public int getIntervalInSeconds()
    {
        return intervalInSeconds;
    }

    public List<ItemStack> getPreservedItems()
    {
        return Collections.unmodifiableList(preservedItems);
    }

    public List<Integer> getNotifyTimes()
    {
        return Collections.unmodifiableList(notifyTimes);
    }
    
}
