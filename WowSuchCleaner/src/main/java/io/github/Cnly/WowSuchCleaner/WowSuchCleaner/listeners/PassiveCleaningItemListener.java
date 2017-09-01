package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemDespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PassiveCleaningItemListener implements Listener
{
    
    private Main main = Main.getInstance();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void handlePassiveCleaning(ItemDespawnEvent e)
    {
        
        auctionDataManager.addLot(e.getEntity());
        
    }
    
}
