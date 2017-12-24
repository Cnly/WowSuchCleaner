package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class DefaultItem extends AuctionableItem
{
    
    public static final ItemStack DEFAULT_ITEMSTACK = new ItemStack(Material.AIR);

    public DefaultItem(float minDurabilityPercent, float maxDurabilityPercent, double startingPrice, double minimumIncrement, int preserveTimeInSeconds, int auctionDurationInSeconds) {
        super(DEFAULT_ITEMSTACK, minDurabilityPercent, maxDurabilityPercent, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
    }
    
    @Override
    public boolean isTheSameItem(ItemStack item)
    {
        if(item.getType() == Material.AIR)
        {
            return false;
        }
        else
        {
            short maxDurability = item.getType().getMaxDurability();
            if(maxDurability != 0)  // duration acting as extra data to indicate material variant
            {
                short durability = (short)(maxDurability - item.getDurability());
                if(!(durability >= (short)(maxDurability * this.minDurabilityPercent) && durability <= (short)(maxDurability * this.maxDurabilityPercent))) return false;
            }
        }
        return true;
    }
    
}
