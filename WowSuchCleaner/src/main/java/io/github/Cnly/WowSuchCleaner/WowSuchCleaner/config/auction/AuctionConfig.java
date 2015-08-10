package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;

import java.util.HashSet;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class AuctionConfig
{
    
    private double chargePercentPerBid;
    private double minimumChargePerBid;
    
    private HashSet<AuctionableItem> auctionableItems = new HashSet<>();
    
    private boolean activeCleaningAuction;
    private boolean passiveCleaningAuction;
    
    private int vaultCapacity;
    
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
        
        for(AuctionableItem ai : auctionableItems)
        {
            if(ai.isTheSameItem(item)) return ai;
        }
        
        return null;
    }

    private void load()
    {
        
        this.chargePercentPerBid = this.config.getDouble("auction.charge.chargePercentPerBid");
        this.minimumChargePerBid = this.config.getDouble("auction.charge.minimumChargePerBid");
        
        for(Map<?, ?> map : this.config.getMapList("auction.auctionableItems"))
        {
            this.auctionableItems.add(AuctionableItem.fromMap(map));
        }
        
        this.activeCleaningAuction = this.config.getBoolean("cleaning.active.auction");
        this.passiveCleaningAuction = this.config.getBoolean("cleaning.passive.auction");
        
        this.vaultCapacity = this.config.getInt("vault.capacity");
        
    }
    
    public double getChargePercentPerBid()
    {
        return chargePercentPerBid;
    }

    public double getMinimumChargePerBid()
    {
        return minimumChargePerBid;
    }

    public boolean isActiveCleaningAuction()
    {
        return activeCleaningAuction;
    }

    public boolean isPassiveCleaningAuction()
    {
        return passiveCleaningAuction;
    }

    public int getVaultCapacity()
    {
        return vaultCapacity;
    }
    
}
