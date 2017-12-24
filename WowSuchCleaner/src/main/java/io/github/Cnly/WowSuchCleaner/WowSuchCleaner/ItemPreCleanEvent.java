package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.Map;

/**
 * The event called before the active cleaner cleans.
 */
public class ItemPreCleanEvent extends Event implements Cancellable
{
    
    private final Map<ApproximateBoxRegion, ArrayList<Item>> itemsToClean;
    private final Map<ApproximateBoxRegion, ArrayList<Item>> itemsToAuction;
    private boolean cancelled = false;

    public ItemPreCleanEvent(Map<ApproximateBoxRegion, ArrayList<Item>> itemsToClean, Map<ApproximateBoxRegion, ArrayList<Item>> itemsToAuction)
    {
        this.itemsToClean = itemsToClean;
        this.itemsToAuction = itemsToAuction;
    }

    /**
     * Gets the map list of item entities to clean. The result will never be null.<br>
     * {@code ApproximateBoxRegion} is a class from {@code Crafter} the library by me. In this plugin,
     * region objects are unique.
     * 
     * @return The map list of item entities to clean.
     */
    public Map<ApproximateBoxRegion, ArrayList<Item>> getItemsToClean()
    {
        return itemsToClean;
    }
    
    /**
     * Gets the map list of item entities to auction. The result can be null if
     * cleaning.&lt;profileName&gt;.active.auction in config.yml is set to false.<br>
     * {@code ApproximateBoxRegion} is a class from {@code Crafter} the library by me. In this plugin,
     * region objects are unique.
     * 
     * @return The map list of items to auction
     */
    public Map<ApproximateBoxRegion, ArrayList<Item>> getItemsToAuction()
    {
        return itemsToAuction;
    }

    @Override
    public boolean isCancelled()
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled)
    {
        this.cancelled = cancelled;
    }

    private static final HandlerList handlerList = new HandlerList();
    
    @Override
    public HandlerList getHandlers()
    {
        
        return handlerList;
    }
    
    public static HandlerList getHandlerList()
    {
        
        return handlerList;
    }
    
}
