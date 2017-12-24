package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import io.github.Cnly.Crafter.Crafter.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;

public class AuctionableItem
{
    
    protected final ItemStack item;
    protected final float minDurabilityPercent;
    protected final float maxDurabilityPercent;
    private final short minDurability;
    private final short maxDurability;
    protected final double startingPrice;
    protected final double minimumIncrement;
    protected final int preserveTimeInSeconds;
    protected final int auctionDurationInSeconds;

    @SuppressWarnings("unchecked")
    public static AuctionableItem fromMap(Map<?, ?> map)
    {
        
        Map<String, Object> convertedMap = (Map<String, Object>)map;
        
        String itemRepresent = (String)convertedMap.get("item");
        float minDurabilityPercent = ((Integer)convertedMap.get("minDurabilityPercent") / 100.0F);
        float maxDurabilityPercent = ((Integer)convertedMap.get("maxDurabilityPercent") / 100.0F);
        double startingPrice = (double)convertedMap.get("startingPrice");
        double minimumIncrement = (double)convertedMap.get("minimumIncrement");
        int preserveTimeInSeconds = (int)convertedMap.get("preserveTimeInSeconds");
        int auctionDurationInSeconds = (int)convertedMap.get("auctionDurationInSeconds");
        
        ItemStack item = null;
        if(itemRepresent.equalsIgnoreCase("default"))
        {
            return new DefaultItem(minDurabilityPercent, maxDurabilityPercent, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
        }
        else
        {
            item = ItemUtils.getItemByIdString(itemRepresent);
        }
        
        return new AuctionableItem(item, minDurabilityPercent, maxDurabilityPercent, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
    }

    public AuctionableItem(ItemStack item, float minDurabilityPercent, float maxDurabilityPercent, double startingPrice, double minimumIncrement, int preserveTimeInSeconds, int auctionDurationInSeconds) {
        this.item = item;
        this.minDurabilityPercent = minDurabilityPercent;
        this.maxDurabilityPercent = maxDurabilityPercent;
        this.minDurability = (short)(item.getType().getMaxDurability() * minDurabilityPercent);
        this.maxDurability = (short)(item.getType().getMaxDurability() * maxDurabilityPercent);
        this.startingPrice = startingPrice;
        this.minimumIncrement = minimumIncrement;
        this.preserveTimeInSeconds = preserveTimeInSeconds;
        this.auctionDurationInSeconds = auctionDurationInSeconds;
    }

    public boolean isTheSameItem(ItemStack item)
    {
        Material type = this.item.getType();
        if(item.getType() != type) return false;
        short durability = (short)(maxDurability - item.getDurability());
        if(type.getMaxDurability() == 0)  // duration acting as extra data to indicate material variant
        {
            if(durability != this.item.getDurability()) return false;
        }
        else
        {
            if(!(durability >= this.minDurability && durability <= this.maxDurability)) return false;
        }
        return true;
    }
    
    public ItemStack getItem()
    {
        return item;
    }

    public double getStartingPrice()
    {
        return startingPrice;
    }

    public double getMinimumIncrement()
    {
        return minimumIncrement;
    }

    public int getPreserveTimeInSeconds()
    {
        return preserveTimeInSeconds;
    }

    public int getAuctionDurationInSeconds()
    {
        return auctionDurationInSeconds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuctionableItem that = (AuctionableItem) o;
        return minDurability == that.minDurability &&
                maxDurability == that.maxDurability &&
                Double.compare(that.startingPrice, startingPrice) == 0 &&
                Double.compare(that.minimumIncrement, minimumIncrement) == 0 &&
                preserveTimeInSeconds == that.preserveTimeInSeconds &&
                auctionDurationInSeconds == that.auctionDurationInSeconds &&
                Objects.equals(item, that.item);
    }

    @Override
    public int hashCode() {

        return Objects.hash(item, minDurability, maxDurability, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
    }
}
