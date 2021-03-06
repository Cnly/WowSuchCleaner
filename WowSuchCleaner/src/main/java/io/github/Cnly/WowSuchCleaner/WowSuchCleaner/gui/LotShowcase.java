package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import io.github.Cnly.BusyInv.BusyInv.apis.IOpenable;
import io.github.Cnly.BusyInv.BusyInv.events.ItemClickEvent;
import io.github.Cnly.BusyInv.BusyInv.items.BusyItem;
import io.github.Cnly.BusyInv.BusyInv.menus.ChestMenu;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.Lot;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners.BidHandler;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners.BidHandler.BidCallback;

public class LotShowcase extends ChestMenu
{
    
    private static HashMap<Player, LotShowcase> opened = new HashMap<>();
    
    private static final ItemStack PAGE_BUTTON_ICON = new ItemStack(Material.LADDER);
    private static final ItemStack PAGE_INDICATOR_ICON = new ItemStack(Material.PAPER);
    private static final ItemStack REFRESH_BUTTON_ICON = new ItemStack(Material.TNT);
    
    private Main main = Main.getInstance();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();
    private BidHandler bidHandler = main.getBidHandler();
    private ILocaleManager localeManager;
    
    private Player player;
    private int currentNaturalPage;
    
    public LotShowcase(ILocaleManager localeManager)
    {
        super(localeManager.getLocalizedString("ui.title"), null, ChestSize.SIX_LINES);
        this.localeManager = localeManager;
    }
    
    @Deprecated
    @Override
    public ChestMenu openFor(Player p)
    {
        openFor(p, 1);
        return this;
    }
    
    public void openFor(Player p, int naturalPage)
    {
        this.currentNaturalPage = naturalPage;
        this.setPageContent(naturalPage);
        super.openFor(p);
        this.player = p;
        opened.put(p, this);
    }

    @Override
    public boolean updateFor(Player p)
    {
        this.setPageContent(currentNaturalPage);
        return super.updateFor(p);
    }

    @Override
    public void onMenuClose(InventoryCloseEvent e)
    {
        super.onMenuClose(e);
        this.player = null;
        opened.remove(e.getPlayer());
    }
    
    @Override
    public void onMenuClick(InventoryClickEvent e)
    {
        
        Player p = (Player)e.getWhoClicked();
        
        int rawSlot = e.getRawSlot();
        if(rawSlot < 0 || rawSlot >= this.size)
            return;
        
        // MODIFICATION START
        if(e.getClick() == ClickType.DROP)
        {
            reloadFor(p);
            return;
        }
        // MODIFICATION END
        
        BusyItem bi = this.items[rawSlot];
        if(null == bi)
            return;
        ItemClickEvent ice = new ItemClickEvent(p, this, e.getClick(),
                e.getHotbarButton());
        bi.onClick(ice);
        
        if(ice.willCloseDirectly())
        {
            // The following is a magic
            if(rawSlot <= 44)
                p.closeInventory();
            else
                this.closeInventorySafely(p);
            return;
        }
        
        if(ice.willOpenParent())
        {
            IOpenable parentMenu = this.openParentFor(p);
            if(null == parentMenu && ice.willCloseOnNoParent())
                // The following is a magic
                if(rawSlot <= 44)
                    p.closeInventory();
                else
                    this.closeInventorySafely(p);
            return;
        }
        
        if(ice.willReloadMenu())
        {
            reloadFor(p);
            return;
        }
        
    }
    
    public static void updateAll()
    {
        for(Entry<Player, LotShowcase> e : opened.entrySet())
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
    
    private void setPageContent(int targetNaturalPage)
    {
        
        this.setItems(new BusyItem[this.size]);
        
        int startIndex = (targetNaturalPage - 1) * 48;
        
        List<Lot> lots = auctionDataManager.getLots();
        if(lots.size() > startIndex)
        {
            
            ArrayList<Lot> pageLots = new ArrayList<Lot>();
            int count = 0;
            for(Iterator<Lot> i = lots.listIterator(startIndex); i.hasNext();)
            {
                
                Lot lot = i.next();
                pageLots.add(lot);
                
                count++;
                if(count == 48) break;
                
            }
            
            Iterator<Lot> it = pageLots.iterator();
            go:
                for(int row = 1; row < 7; row++)
                {
                    for(int column = 1; column < 9; column++) // Last column preserved for control buttons
                    {
                        if(!it.hasNext()) break go;
                        LotItem lotItem = new LotItem(it.next());
                        this.naturalSet(row, column, lotItem);
                    }
                }
            
        }
        
        if(targetNaturalPage > 1)
        {
            this.naturalSet(1, 9, new PageButton(localeManager.getLocalizedString("ui.prevPageButton")
                    .replace("{page}", String.valueOf(targetNaturalPage - 1)), targetNaturalPage - 1));
        }
        
        if(lots.size() > startIndex + 48)
        {
            this.naturalSet(6, 9, new PageButton(localeManager.getLocalizedString("ui.nextPageButton")
                    .replace("{page}", String.valueOf(targetNaturalPage + 1)), targetNaturalPage + 1));
        }
        
        this.naturalSet(3, 9, new PageIndicator(targetNaturalPage));
        this.naturalSet(4, 9, new RefreshButton());
        
    }
    
    @Override
    protected void closeInventorySafely(Player p)
    {
        closeInventorySafely(main, p);
    }

    private class PageButton extends BusyItem
    {
        
        private int targetNaturalPage;
        
        public PageButton(String displayName, int targetNaturalPage)
        {
            super(displayName, PAGE_BUTTON_ICON);
            this.targetNaturalPage = targetNaturalPage;
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            currentNaturalPage = targetNaturalPage;
            updateFor(player);
        }
        
    }
    
    private class PageIndicator extends BusyItem
    {
        
        public PageIndicator(int naturalPage)
        {
            super(PAGE_INDICATOR_ICON);
            this.setPage(naturalPage);
        }
        
        public void setPage(int naturalPage)
        {
            this.look.setAmount(naturalPage);
            this.setDisplayName(localeManager.getLocalizedString("ui.currentPage")
                    .replace("{page}", String.valueOf(naturalPage)));
        }
        
    }
    
    private class RefreshButton extends BusyItem
    {

        public RefreshButton()
        {
            super(localeManager.getLocalizedString("ui.clickToRefresh"), REFRESH_BUTTON_ICON);
        }

        @Override
        public void onClick(ItemClickEvent e)
        {
            e.setReloadMenu(true);
        }
        
    }
    
    private class LotItem extends BusyItem implements BidCallback
    {
        
        private Lot lot;
        
        public LotItem(Lot lot)
        {
            super(lot.getItem());
            
            this.lot = lot;
            
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

        @Override
        public void onClick(ItemClickEvent e)
        {
            
            if(!auctionDataManager.hasLot(lot))
            {
                player.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
                updateFor(player);
                return;
            }
            
            switch(e.getClickType())
            {
            
            case LEFT:
                // fall down
            case SHIFT_LEFT:
                e.setCloseDirectly(true);
                Bukkit.getScheduler().runTask(main, new Runnable()
                { // Task is used for BidCallback::onCancel in BidHandler::requestBid
                    Player tmpPlayer = player;
                    @Override
                    public void run()
                    {
                        bidHandler.requestBid(tmpPlayer, lot, LotItem.this);
                    }
                });
                
                break;
            case RIGHT:
                // fall down
            case SHIFT_RIGHT:
                if(!player.hasPermission("WowSuchCleaner.lotOperationMenu.open")) return;
                new LotOperationMenu(localeManager, lot, LotShowcase.this, player, currentNaturalPage).openFor(player);
                break;
            default:
                break;
            }
            
        }

        @Override
        public void onBidSuccess(Player player, double bid, boolean anonymous)
        {
            openFor(player, currentNaturalPage);
        }

        @Override
        public void onCancel(Player player)
        {
            openFor(player, currentNaturalPage);
        }
        
    }
    
}
