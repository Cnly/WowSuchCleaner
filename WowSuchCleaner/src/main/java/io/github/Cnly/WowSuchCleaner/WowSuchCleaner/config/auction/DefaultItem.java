package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DefaultItem extends AuctionableItem
{
    
    public static final ItemStack DEFAULT_ITEMSTACKS = new ItemStack(Material.AIR);
    
    public DefaultItem(double startingPrice, double minimumIncrement, int preserveTimeInSeconds, int auctionDurationInSeconds)
    {
        super(DEFAULT_ITEMSTACKS, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
    }
    
    @Override
    public boolean isTheSameItem(ItemStack item)
    {
        return true;
    }
    
}
