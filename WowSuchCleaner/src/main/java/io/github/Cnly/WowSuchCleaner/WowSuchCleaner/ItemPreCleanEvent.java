package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.util.List;

import org.bukkit.entity.Item;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;

/**
 * The event called before the active cleaner cleans.
 */
public class ItemPreCleanEvent extends Event implements Cancellable
{
    
    private final List<Item> itemsToClean;
    private final List<ItemStack> itemsToAuction;
    private boolean cancelled = false;

    public ItemPreCleanEvent(List<Item> itemsToClean, List<ItemStack> itemsToAuction)
    {
        this.itemsToClean = itemsToClean;
        this.itemsToAuction = itemsToAuction;
    }

    /**
     * Gets the list of item entities to clean. This list will never be null.
     * 
     * @return The list of item entities to clean.
     */
    public List<Item> getItemsToClean()
    {
        return itemsToClean;
    }
    
    /**
     * Gets the list of items to auction. The list can be null if
     * cleaning.active.auction in config.yml is set to false.
     * 
     * @return The list of items to auction
     */
    public List<ItemStack> getItemsToAuction()
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
