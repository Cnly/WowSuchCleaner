package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners;

import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerListener implements Listener
{
    
    private Main main = Main.getInstance();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e)
    {
        auctionDataManager.returnDepositInVault(e.getPlayer().getUniqueId());
    }
    
}
