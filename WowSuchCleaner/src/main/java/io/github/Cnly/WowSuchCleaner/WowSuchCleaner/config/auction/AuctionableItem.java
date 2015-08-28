package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction;

import io.github.Cnly.Crafter.Crafter.utils.ItemUtils;

import java.util.Map;

import org.bukkit.inventory.ItemStack;

public class AuctionableItem
{
    
    protected final ItemStack item;
    protected final double startingPrice;
    protected final double minimumIncrement;
    protected final int preserveTimeInSeconds;
    protected final int auctionDurationInSeconds;
    
    @SuppressWarnings("unchecked")
    public static AuctionableItem fromMap(Map<?, ?> map)
    {
        
        Map<String, Object> convertedMap = (Map<String, Object>)map;
        
        String itemRepresent = (String)convertedMap.get("item");
        double startingPrice = (double)convertedMap.get("startingPrice");
        double minimumIncrement = (double)convertedMap.get("minimumIncrement");
        int preserveTimeInSeconds = (int)convertedMap.get("preserveTimeInSeconds");
        int auctionDurationInSeconds = (int)convertedMap.get("auctionDurationInSeconds");
        
        ItemStack item = null;
        if(itemRepresent.equalsIgnoreCase("default"))
        {
            return new DefaultItem(startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
        }
        else
        {
            item = ItemUtils.getItemByIdString(itemRepresent);
        }
        
        return new AuctionableItem(item, startingPrice, minimumIncrement, preserveTimeInSeconds, auctionDurationInSeconds);
    }

    public AuctionableItem(ItemStack item, double startingPrice, double minimumIncrement, int preserveTimeInSeconds, int auctionDurationInSeconds)
    {
        super();
        this.item = item;
        this.startingPrice = startingPrice;
        this.minimumIncrement = minimumIncrement;
        this.preserveTimeInSeconds = preserveTimeInSeconds;
        this.auctionDurationInSeconds = auctionDurationInSeconds;
    }
    
    public boolean isTheSameItem(ItemStack item)
    {
        if(item.getType() != this.item.getType()) return false;
        if(this.item.getType().getMaxDurability() == 0)
        {
            if(item.getDurability() != this.item.getDurability()) return false;
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
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + auctionDurationInSeconds;
        result = prime * result + ((item == null) ? 0 : item.hashCode());
        long temp;
        temp = Double.doubleToLongBits(minimumIncrement);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        result = prime * result + preserveTimeInSeconds;
        temp = Double.doubleToLongBits(startingPrice);
        result = prime * result + (int)(temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        AuctionableItem other = (AuctionableItem)obj;
        if(auctionDurationInSeconds != other.auctionDurationInSeconds)
            return false;
        if(item == null)
        {
            if(other.item != null)
                return false;
        }
        else if(!item.equals(other.item))
            return false;
        if(Double.doubleToLongBits(minimumIncrement) != Double.doubleToLongBits(other.minimumIncrement))
            return false;
        if(preserveTimeInSeconds != other.preserveTimeInSeconds)
            return false;
        if(Double.doubleToLongBits(startingPrice) != Double.doubleToLongBits(other.startingPrice))
            return false;
        return true;
    }
    
}
