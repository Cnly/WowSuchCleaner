package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config;

import io.github.Cnly.Crafter.Crafter.framework.configs.CrafterYamlConfigManager;
import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionableItem;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning.CleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.RegionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WSCApproxBoxRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WholeServerRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WorldRegion;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public class RegionalConfigManager
{
    
    private Main main = Main.getInstance();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private CrafterYamlConfigManager config = new CrafterYamlConfigManager(new File(main.getDataFolder(), "regions.yml"), true, main);
    private HashMap<ApproximateBoxRegion, RegionConfig> regionConfigMap = new HashMap<>();
    private LinkedList<RegionConfig> regionConfigList = new LinkedList<>(); // Used to bring priority to regions
    
    public RegionalConfigManager()
    {
        load();
    }
    
    private void load()
    {
        
        boolean modified = false;
        ConfigurationSection regionSection = config.getConfigurationSection("regions");
        for(String regionName : regionSection.getKeys(false))
        {
            ConfigurationSection singleRegionSection = regionSection.getConfigurationSection(regionName);
            if(regionName.length() == 0)
            {
                throw new AssertionError("A region's name cannot be null!");
            }
            String regionString = singleRegionSection.getString("region");
            WSCApproxBoxRegion region;
            if(regionString.contains(","))
            {
                region = stringToRegion(regionString);
            }
            else
            {// New world region set by user. Convert it.
                region = new WorldRegion(regionString);
                singleRegionSection.set("region", region.toString());
                modified = true;
            }
            AuctionConfig auctionConfig = sharedConfigManager.getAuctionConfig(singleRegionSection.getString("auctionConfig"));
            CleaningConfig cleaningConfig = sharedConfigManager.getCleaningConfig(singleRegionSection.getString("cleaningConfig"));
            RegionConfig regionConfig = new RegionConfig(regionName, region, auctionConfig, cleaningConfig);
            regionConfigMap.put(region, regionConfig);
            regionConfigList.add(regionConfig);
        }
    
        if(modified)
        {
            config.save();
        }
    
        // Add default whole-server region
        WholeServerRegion wholeServerRegion = new WholeServerRegion();
        RegionConfig wholeServerRegionConfig = new RegionConfig("", wholeServerRegion, sharedConfigManager.getAuctionConfig("default"),
                                                                sharedConfigManager.getCleaningConfig("default"));
        regionConfigMap.put(wholeServerRegion, wholeServerRegionConfig);
        regionConfigList.add(wholeServerRegionConfig);
        
    }
    
    private static WSCApproxBoxRegion stringToRegion(String s)
    {
        
        String[] split = s.split(",");
        
        String worldName = split[0];
        int maxX = Integer.parseInt(split[1]);
        int minX = Integer.parseInt(split[2]);
        int maxY = Integer.parseInt(split[3]);
        int minY = Integer.parseInt(split[4]);
        int maxZ = Integer.parseInt(split[5]);
        int minZ = Integer.parseInt(split[6]);
        
        if(maxX == Integer.MAX_VALUE && minX == Integer.MIN_VALUE
                   && maxY == Integer.MAX_VALUE && minY == Integer.MIN_VALUE
                   && maxZ == Integer.MAX_VALUE && minZ == Integer.MIN_VALUE)
        {
            return new WorldRegion(worldName);
        }
        else
        {
            return new WSCApproxBoxRegion(worldName, maxX, minX, maxY, minY, maxZ, minZ);
        }
        
    }
    
    public Map<ApproximateBoxRegion, RegionConfig> getRegionConfigMap()
    {
        return Collections.unmodifiableMap(regionConfigMap);
    }
    
    public RegionConfig getRegionConfig(ApproximateBoxRegion region)
    {
        for(RegionConfig i : regionConfigList)
        {
            if(i.getRegion().isTheSameArea(region))
            {
                return i;
            }
        }
        return null;
    }
    
    public RegionConfig getRegionConfig(Location location)
    {
        for(RegionConfig regionConfig : regionConfigList)
        {
            if(regionConfig.getRegion().isInRegion(location))
            {
                return regionConfig;
            }
        }
        return null;
    }
    
    public RegionConfig getRegionConfig(String regionName)
    {
        for(RegionConfig regionConfig : regionConfigList)
        {
            if(regionConfig.getRegionName().equals(regionName))
            {
                return regionConfig;
            }
        }
        return null;
    }
    
    public AuctionableItem getAuctionableItemConfig(Item item)
    {
        return getAuctionableItemConfig(item.getItemStack(), item.getLocation());
    }
    
    public AuctionableItem getAuctionableItemConfig(ItemStack item, Location location)
    {
        RegionConfig regionConfig = getRegionConfig(location);
        if(null == regionConfig)
        {
            return null;
        }
        return regionConfig.getAuctionConfig().getAuctionableItemConfig(item);
    }
    
    public boolean createRegion(ApproximateBoxRegion region, RegionConfig regionConfig)
    {
    
        for(RegionConfig i : regionConfigList)
        {
            if(i.getRegion().isTheSameArea(region))
            {
                return false;
            }
        }
        
        regionConfigMap.put(region, regionConfig);
        regionConfigList.add(0, regionConfig);
    
        // Maintain insertion order in regions.yml
        ConfigurationSection originalRegionSection = config.getConfigurationSection("regions");
        ConfigurationSection newRegionSection = config.getYamlConfig().createSection("regions");
        
        ConfigurationSection newSection = newRegionSection.createSection(regionConfig.getRegionName());
        newSection.set("region", regionConfig.getRegion().toString());
        newSection.set("auctionConfig", regionConfig.getAuctionConfig().getProfileName());
        newSection.set("cleaningConfig", regionConfig.getCleaningConfig().getProfileName());
    
        for(String key : originalRegionSection.getKeys(true))
        {
            newRegionSection.set(key, originalRegionSection.get(key));
        }
        
        config.save();
        
        return true;
    }
    
    /**
     * Tries to remove the region with the given name.
     * @param regionName region name
     * @return the config removed; or null if not found
     */
    public RegionConfig removeRegion(String regionName)
    {
        RegionConfig regionConfig;
        for(Iterator<RegionConfig> i = regionConfigList.iterator(); i.hasNext();)
        {
            regionConfig = i.next();
            if(regionConfig.getRegionName().equals(regionName))
            {
                
                i.remove();
                regionConfigMap.remove(regionConfig.getRegion());
    
                config.set("regions." + regionName, null);
                config.save();
                
                return regionConfig;
            }
        }
        
        return null;
    }
    
}
