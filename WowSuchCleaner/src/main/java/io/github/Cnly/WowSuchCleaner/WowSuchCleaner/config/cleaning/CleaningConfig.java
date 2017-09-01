package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning;

import java.util.*;

import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.Crafter.Crafter.utils.ItemUtils;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class CleaningConfig
{
    
    private String profileName;
    private boolean activeCleaningEnabled;
    private boolean activeCleaningAuction;
    private boolean autoMerge;
    private int intervalInSeconds;
    private int generousDelayInTicks; // Tick conversion for the config field generousDelayInSeconds
    private List<ItemStack> preservedItems;
    private boolean inRegionNotification;
    private HashMap<Integer, String> notificationMap;
    private boolean clickableCleaningNotification;
    
    private boolean passiveCleaningAuction;
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    
    public CleaningConfig(String profileName)
    {
        this.profileName = profileName;
        this.load();
    }

    @SuppressWarnings("unchecked")
    private void load()
    {
        
        this.preservedItems = new ArrayList<>();
        this.notificationMap = new HashMap<>();
    
        ConfigurationSection baseSection = config.getConfigurationSection("cleaning.profiles." + profileName);
        
        this.activeCleaningEnabled = baseSection.getBoolean("active.enabled");
        this.activeCleaningAuction = baseSection.getBoolean("active.auction");
        this.autoMerge = baseSection.getBoolean("active.autoMerge");
        this.intervalInSeconds = baseSection.getInt("active.intervalInSeconds");
        this.generousDelayInTicks = baseSection.getInt("active.generousDelayInSeconds") * 20;
        this.clickableCleaningNotification = baseSection.getBoolean("active.clickableCleaningNotification");
        
        List<String> tempPreservedItems = baseSection.getStringList("active.preservedItems");
        for(String s : tempPreservedItems)
        {
            this.preservedItems.add(ItemUtils.getItemByIdString(s));
        }
    
        this.inRegionNotification = baseSection.getBoolean("active.inRegionNotification");
    
        List<Map<?, ?>> notificationMapList = baseSection.getMapList ("active.notify");
        for(Map<?, ?> map : notificationMapList)
        {
            Map<Object, Object> convertedMap = (Map<Object, Object>)map;
            Map.Entry<Object, Object> e = convertedMap.entrySet().iterator().next();
            Integer time = (Integer)e.getKey();
            String msg = (String)e.getValue();
            if(msg.equals(""))
            {
                if(0 == time)
                {
                    msg = localeManager.getLocalizedString("cleaning.cleanNotify");
                }
                else
                {
                    msg = localeManager.getLocalizedString("cleaning.preNotify");
                }
            }
            msg = msg.replace("{time}", time.toString());
            this.notificationMap.put(time, msg);
        }
        
        if(this.notificationMap.get(0) == null)
        {// Ensure key 0 is in the map to make it more convenient for ActiveCleaner
            this.notificationMap.put(0, localeManager.getLocalizedString("cleaning.cleanNotify").replace("{time}", String.valueOf(0)));
        }
    
        this.passiveCleaningAuction = baseSection.getBoolean("passive.auction");
        
    }
    
    public String getNotification(int time)
    {
        return this.notificationMap.get(time);
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
    
    public String getProfileName()
    {
        return profileName;
    }
    
    public boolean isActiveCleaningEnabled()
    {
        return activeCleaningEnabled;
    }
    
    public boolean isActiveCleaningAuction()
    {
        return activeCleaningAuction;
    }
    
    public boolean isAutoMerge()
    {
        return autoMerge;
    }

    public int getIntervalInSeconds()
    {
        return intervalInSeconds;
    }
    
    public int getGenerousDelayInTicks()
    {
        return generousDelayInTicks;
    }
    
    public List<ItemStack> getPreservedItems()
    {
        return Collections.unmodifiableList(preservedItems);
    }
    
    public boolean isInRegionNotification()
    {
        return inRegionNotification;
    }
    
    public boolean isClickableCleaningNotification()
    {
        return clickableCleaningNotification;
    }
    
    public boolean isPassiveCleaningAuction()
    {
        return passiveCleaningAuction;
    }
    
}
