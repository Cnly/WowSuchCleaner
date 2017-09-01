package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning.CleaningConfig;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.HashMap;

public class SharedConfigManager
{
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    
    private double chargePercentPerBid;
    private double minimumChargePerBid;
    private String transferAccount;
    
    private int bidIntervalInSeconds;
    
    private int vaultCapacity;
    private boolean vaultCapacityPermissionControl;
    
    private HashMap<String, AuctionConfig> auctionConfigPool = new HashMap<>();
    private HashMap<String, CleaningConfig> cleaningConfigPool = new HashMap<>();
    
    public SharedConfigManager()
    {
        load();
    }
    
    private void load()
    {
    
        this.chargePercentPerBid = this.config.getDouble("auction.charge.chargePercentPerBid");
        this.minimumChargePerBid = this.config.getDouble("auction.charge.minimumChargePerBid");
        this.transferAccount = this.config.getString("auction.transferAccount");
    
        this.bidIntervalInSeconds = this.config.getInt("auction.bid.intervalInSeconds");
    
        if(this.config.isSet("vault.capacity.defaultCapacity"))
        {
            this.vaultCapacity = this.config.getInt("vault.capacity.defaultCapacity");
        }
        else
        {
            this.vaultCapacity = this.config.getInt("vault.capacity");
        }
    
        this.vaultCapacityPermissionControl = this.config.getBoolean("vault.capacity.permissionControl");
    
        ConfigurationSection auctionProfileSection = this.config.getConfigurationSection("auction.profiles");
        for(String profileName : auctionProfileSection.getKeys(false))
        {
            this.auctionConfigPool.put(profileName, new AuctionConfig(profileName));
        }
    
        ConfigurationSection cleaningProfileSection = this.config.getConfigurationSection("cleaning.profiles");
        for(String profileName : cleaningProfileSection.getKeys(false))
        {
            this.cleaningConfigPool.put(profileName, new CleaningConfig(profileName));
        }
        
    }
    
    public AuctionConfig getAuctionConfig(String profileName)
    {
        return this.auctionConfigPool.get(profileName);
    }
    
    public CleaningConfig getCleaningConfig(String profileName)
    {
        return this.cleaningConfigPool.get(profileName);
    }
    
    public int getVaultCapacity(Player p)
    {
        
        int capacity = getVaultCapacity();
        if(isVaultCapacityPermissionControl())
        {
            
            for(PermissionAttachmentInfo pai : p.getEffectivePermissions())
            {
                String per = pai.getPermission();
                
                if(!per.toLowerCase().startsWith("wowsuchcleaner.vault.capacity.")) continue;
                
                String capacityString = per.split("\\.")[3];
                capacity = Integer.parseInt(capacityString);
                
                continue;
            }
            
        }
        
        return capacity;
    }
    
    public double getChargePercentPerBid()
    {
        return chargePercentPerBid;
    }
    
    public double getMinimumChargePerBid()
    {
        return minimumChargePerBid;
    }
    
    public String getTransferAccount()
    {
        return transferAccount;
    }
    
    public int getBidIntervalInSeconds()
    {
        return bidIntervalInSeconds;
    }
    
    public int getVaultCapacity()
    {
        return vaultCapacity;
    }
    
    public boolean isVaultCapacityPermissionControl()
    {
        return vaultCapacityPermissionControl;
    }
    
}
