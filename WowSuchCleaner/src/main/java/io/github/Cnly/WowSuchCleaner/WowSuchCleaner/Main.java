package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.io.File;

import io.github.Cnly.BusyInv.BusyInv.BusyInv;
import io.github.Cnly.Crafter.Crafter.framework.commands.CrafterMainCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.AuctionCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.ReloadCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.ShowcaseCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.VaultCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.activecleaning.ActiveCleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.LotOperationMenu;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.LotShowcase;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.Vault;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners.BidHandler;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners.PassiveCleaningItemListener;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners.PlayerListener;
import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin
{
    
    private AuctionConfig auctionConfig;
    private ActiveCleaningConfig activeCleaningConfig;
    private CrafterLocaleManager localeManager;
    
    private AuctionDataManager auctionDataManager;
    
    private BidHandler bidHandler;
    
    private static Main instance;
    public static Economy economy = null;
    
    @Override
    public void onEnable()
    {
        
        instance = this;
        
        if(!setupEconomy())
        {
            getLogger().severe("Unable to setup economy support!");
            return;
        }
        
        BusyInv.registerFor(this);
        
        // >>>>> Config >>>>>
        
        saveDefaultConfig();
        
        auctionConfig = new AuctionConfig();
        activeCleaningConfig = new ActiveCleaningConfig();
        localeManager = new CrafterLocaleManager(getConfig().getString("locale"), new File(getDataFolder(), "locales"), true, this);
        
        // <<<<< Config <<<<<
        
        // >>>>> Data >>>>>
        
        auctionDataManager = new AuctionDataManager();
        
        // <<<<< Data <<<<<
        
        // >>>>> Commands >>>>>
        
        CrafterMainCommand mainCommand = new CrafterMainCommand(this);
        mainCommand.addSubcommand(new AuctionCommand());
        mainCommand.addSubcommand(new ShowcaseCommand());
        mainCommand.addSubcommand(new VaultCommand());
        mainCommand.addSubcommand(new ReloadCommand());
        
        getCommand("wowsuchcleaner").setExecutor(mainCommand);
        
        // <<<<< Commands <<<<<
        
        // >>>>> Listeners >>>>>
        
        if(auctionConfig.isPassiveCleaningAuction())
        {
            getServer().getPluginManager().registerEvents(new PassiveCleaningItemListener(), this);
        }
        
        getServer().getPluginManager().registerEvents(bidHandler = new BidHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        // <<<<< Listeners <<<<<
        
        // >>>>> Tasks >>>>>
        
        if(activeCleaningConfig.isEnabled())
        {
            new ActiveCleaner().runTaskTimer(this, 20L, 20L);
        }
        
        // <<<<< Tasks <<<<<
        
    }
    
    @Override
    public void onDisable()
    {
        
        LotShowcase.closeAll();
        Vault.closeAll();
        LotOperationMenu.closeAll();
        
        auctionDataManager.shutdownGracefully();
        
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        
    }
    
    public static Main getInstance()
    {
        return instance;
    }

    public AuctionConfig getAuctionConfig()
    {
        return auctionConfig;
    }

    public ActiveCleaningConfig getActiveCleaningConfig()
    {
        return activeCleaningConfig;
    }

    public ILocaleManager getLocaleManager()
    {
        return localeManager;
    }

    public AuctionDataManager getAuctionDataManager()
    {
        return auctionDataManager;
    }
    
    public BidHandler getBidHandler()
    {
        return bidHandler;
    }

    private boolean setupEconomy()
    {
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }

        return (economy != null);
    }
    
}
