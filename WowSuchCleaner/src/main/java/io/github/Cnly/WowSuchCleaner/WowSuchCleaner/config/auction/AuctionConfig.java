package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;

import java.util.ArrayList;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class AuctionConfig
{
    
    private double chargePercentPerBid;
    private double minimumChargePerBid;
    
    private int bidIntervalInSeconds;
    
    private boolean useAsBlacklist;
    private ArrayList<AuctionableItem> auctionableItems = new ArrayList<>();
    
    private boolean activeCleaningAuction;
    private boolean passiveCleaningAuction;
    
    private int vaultCapacity;
    private boolean vaultCapacityPermissionControl;
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    
    public AuctionConfig()
    {
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

    private void load()
    {
        
        this.chargePercentPerBid = this.config.getDouble("auction.charge.chargePercentPerBid");
        this.minimumChargePerBid = this.config.getDouble("auction.charge.minimumChargePerBid");
        
        this.bidIntervalInSeconds = this.config.getInt("auction.bid.intervalInSeconds");
        
        this.useAsBlacklist = this.config.getBoolean("auction.useAsBlacklist");
        for(Map<?, ?> map : this.config.getMapList("auction.auctionableItems"))
        {
            this.auctionableItems.add(AuctionableItem.fromMap(map));
        }
        
        this.activeCleaningAuction = this.config.getBoolean("cleaning.active.auction");
        this.passiveCleaningAuction = this.config.getBoolean("cleaning.passive.auction");
        
        if(this.config.isSet("vault.capacity.defaultCapacity"))
        {
            this.vaultCapacity = this.config.getInt("vault.capacity.defaultCapacity");
        }
        else
        {
            this.vaultCapacity = this.config.getInt("vault.capacity");
        }
        
        this.vaultCapacityPermissionControl = this.config.getBoolean("vault.capacity.permissionControl");
        
    }
    
    public double getChargePercentPerBid()
    {
        return chargePercentPerBid;
    }

    public double getMinimumChargePerBid()
    {
        return minimumChargePerBid;
    }

    public int getBidIntervalInSeconds()
    {
        return bidIntervalInSeconds;
    }

    public boolean isActiveCleaningAuction()
    {
        return activeCleaningAuction;
    }

    public boolean isUseAsBlacklist()
    {
        return useAsBlacklist;
    }

    public boolean isPassiveCleaningAuction()
    {
        return passiveCleaningAuction;
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
