package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.BusyInv.BusyInv.events.ItemClickEvent;
import io.github.Cnly.BusyInv.BusyInv.items.BusyItem;
import io.github.Cnly.BusyInv.BusyInv.menus.BusyMenu;
import io.github.Cnly.BusyInv.BusyInv.menus.ChestMenu;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.Lot;

public class LotOperationMenu extends ChestMenu
{
    
    private static HashMap<Player, LotOperationMenu> opened = new HashMap<>();
    
    private Main main = Main.getInstance();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    private ILocaleManager localeManager;
    
    private LotShowcase parent;
    private Player player;
    private Lot lot;
    private int previousPage;
    
    public LotOperationMenu(ILocaleManager localeManager, Lot lot, LotShowcase parent, Player player, int previousPage)
    {
        super(localeManager.getLocalizedString("ui.lotOperationMenu"), parent, ChestSize.ONE_LINE);
        this.localeManager = localeManager;
        this.parent = parent;
        this.player = player;
        this.lot = lot;
        this.previousPage = previousPage;
    }
    
    @Override
    public ChestMenu openFor(Player p)
    {
        ChestMenu result = super.openFor(p);
        opened.put(p, this);
        return result;
    }
    
    @Override
    public BusyMenu applyOn(Player p, Inventory inv)
    {
        
        if(!auctionDataManager.hasLot(lot))
        {
            player.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
            openParentShowcase();
            return this;
        }
        
        this.setItem(0, new LotItem());
        
        int index = 2;
        
        if(p.hasPermission("WowSuchCleaner.lotOperationMenu.forcePurchase"))
        {
            this.setItem(index++, new ForcePurchaseButton());
        }
        
        if(p.hasPermission("WowSuchCleaner.lotOperationMenu.removeLot"))
        {
            this.setItem(index, new RemoveLotButton());
        }
        
        this.setItem(8, new BackItem());
        
        return super.applyOn(p, inv);
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e)
    {
        opened.remove(e.getPlayer());
        super.onMenuClose(e);
    }
    
    public static void updateAll()
    {
        for(Entry<Player, LotOperationMenu> e : opened.entrySet())
        {
            e.getValue().updateFor(e.getKey());
        }
    }
    
    public static Collection<Player> closeAll()
    {
        
        Collection<Player> result = new HashSet<Player>(opened.keySet());
        
        for(Player p : result)
        {
            p.closeInventory();
        }
        
        opened.clear();
        
        return result;
    }

    private void openParentShowcase()
    {
        Bukkit.getScheduler().runTask(main, new Runnable(){
            @Override
            public void run()
            {
                parent.openFor(player, previousPage);
            }
        });
    }
    
    private class BackItem extends BusyItem
    {
        
        public BackItem()
        {
            super(localeManager.getLocalizedString("ui.back"), new ItemStack(Material.LADDER));
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            openParentShowcase();
        }
        
    }
    
    private class RemoveLotButton extends BusyItem
    {

        public RemoveLotButton()
        {
            // Red wool
            super(localeManager.getLocalizedString("ui.removeLot"),
                    new ItemStack(Material.WOOL, 1, (short)14),
                    localeManager.getLocalizedString("ui.removeLotLore"),
                    localeManager.getLocalizedString("ui.removeLotLore2"));
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            
            if(e.getClickType() != ClickType.SHIFT_LEFT)
            { // Move the item to inventory
                
                if(player.getInventory().firstEmpty() == -1)
                {
                    player.sendMessage(localeManager.getLocalizedString("ui.fullInventory"));
                    return;
                }
                
                player.getInventory().addItem(lot.getItem());
                
            }
            
            auctionDataManager.returnDeposit(lot);
            auctionDataManager.removeLot(lot);
            openParentShowcase();
            
        }
        
    }
    
    private class ForcePurchaseButton extends BusyItem
    {

        public ForcePurchaseButton()
        {
            // Orange wool
            super(localeManager.getLocalizedString("ui.forcePurchase"), new ItemStack(Material.WOOL, 1, (short)1), localeManager.getLocalizedString("ui.forcePurchaseLore"));
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            
            if(player.getInventory().firstEmpty() == -1 && !auctionDataManager.isVaultAvailable(player))
            {
                player.sendMessage(localeManager.getLocalizedString("ui.fullInventory"));
                player.sendMessage(localeManager.getLocalizedString("ui.fullVault"));
                return;
            }
            
            if(!auctionDataManager.hasBidBefore(player, lot))
            {
                EconomyResponse er = Main.economy.withdrawPlayer(Bukkit.getOfflinePlayer(player.getUniqueId()), lot.getPrice());
                
                if(!er.transactionSuccess())
                {
                    player.sendMessage(localeManager.getLocalizedString("ui.balanceNotEnough")
                            .replace("{balance}", String.valueOf(er.balance))
                            .replace("{currency}", Main.economy.currencyNamePlural()));
                    return;
                }
            }
            
            auctionDataManager.bid(player, true, lot, 0);
            auctionDataManager.hammer(lot);
            openParentShowcase();
            
        }
        
    }
    
    private class LotItem extends BusyItem
    {
        
        public LotItem()
        {
            super(lot.getItem());
            
            List<String> lore = new ArrayList<String>(this.getLores());
            
            if(lot.isStarted())
            {
                
                lore.add(localeManager.getLocalizedString("ui.currentPrice")
                        .replace("{price}", String.format("%.2f", lot.getPrice()))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                
                lore.add(localeManager.getLocalizedString("ui.timeRemaining")
                        .replace("{time}", String.valueOf((lot.getAuctionDurationExpire() - System.currentTimeMillis()) / 1000)));
                
                lore.add(localeManager.getLocalizedString("ui.lastBid")
                        .replace("{player}", lot.getLastBidPlayerName())
                        .replace("{price}", String.format("%.2f", lot.getLastBidPrice()))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                
            }
            else
            {
                
                lore.add(localeManager.getLocalizedString("ui.bidUnstarted"));
                
                lore.add(localeManager.getLocalizedString("ui.currentPrice")
                        .replace("{price}", String.format("%.2f", lot.getPrice()))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                
                lore.add(localeManager.getLocalizedString("ui.timeRemaining")
                        .replace("{time}", String.valueOf((lot.getPreserveTimeExpire() - System.currentTimeMillis()) / 1000)));
                
            }
            
            lore.add(localeManager.getLocalizedString("ui.minimumIncrement")
                    .replace("{price}", String.valueOf(lot.getMinimumIncrement()))
                    .replace("{currency}", Main.economy.currencyNamePlural()));
            
            this.setLores(lore);
            
        }
        
    }
    
}
