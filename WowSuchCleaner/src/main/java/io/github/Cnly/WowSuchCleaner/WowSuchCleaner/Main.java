package io.github.Cnly.WowSuchCleaner.WowSuchCleaner;

import java.io.File;

import io.github.Cnly.BusyInv.BusyInv.BusyInv;
import io.github.Cnly.Crafter.Crafter.framework.commands.CrafterMainCommand;
import io.github.Cnly.Crafter.Crafter.framework.locales.CrafterLocaleManager;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.Crafter.Crafter.utils.CommandUtils;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.AuctionCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.ReloadCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.ShowcaseCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.VaultCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region.RegionCreateCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region.RegionCreateWorldCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region.RegionRemoveCommand;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
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
    
    private SharedConfigManager sharedConfigManager;
    private RegionalConfigManager regionalConfigManager;
    private CrafterLocaleManager localeManager;
    
    private AuctionDataManager auctionDataManager;
    
    private BidHandler bidHandler;
    
    private ActiveCleaner activeCleaner;
    
    private static Main instance;
    public static Economy economy = null;
    
    @Override
    public void onEnable()
    {
        
        instance = this;
        
        if(!setupEconomy())
        {
            getLogger().severe("Unable to setup economy support!");
            this.setEnabled(false);
            return;
        }
        
        BusyInv.registerFor(this);
        
        // >>>>> Config >>>>>
        
        saveDefaultConfig();
        
        localeManager = new WSCLocaleManager(getConfig().getString("locale"), new File(getDataFolder(), "locales"), true, this);
        sharedConfigManager = new SharedConfigManager();
        regionalConfigManager = new RegionalConfigManager();
        
        // <<<<< Config <<<<<
        
        // >>>>> Data >>>>>
        
        auctionDataManager = new AuctionDataManager();
        
        // <<<<< Data <<<<<
        
        // >>>>> Listeners >>>>>
        
        getServer().getPluginManager().registerEvents(new PassiveCleaningItemListener(), this);
        getServer().getPluginManager().registerEvents(bidHandler = new BidHandler(), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        
        // <<<<< Listeners <<<<<
        
        // >>>>> Tasks >>>>>
        
        activeCleaner = new ActiveCleaner();
        
        // <<<<< Tasks <<<<<
    
        // >>>>> Commands >>>>>
    
        CrafterMainCommand regionMainCommand = new CrafterMainCommand();
        regionMainCommand
                .setHelp(localeManager.getLocalizedString("commands.region.help").replace("{usage}", "/wsc region"))
                .setAction("region")
                .setArgumentOffset(1);
        regionMainCommand.addSubcommand(new RegionCreateCommand());
        regionMainCommand.addSubcommand(new RegionCreateWorldCommand());
        regionMainCommand.addSubcommand(new RegionRemoveCommand());
    
        CrafterMainCommand mainCommand = new CrafterMainCommand(this);
        mainCommand.addSubcommand(new AuctionCommand());
        mainCommand.addSubcommand(new ShowcaseCommand());
        mainCommand.addSubcommand(new VaultCommand());
        mainCommand.addSubcommand(regionMainCommand);
        mainCommand.addSubcommand(new ReloadCommand());
    
        CommandUtils.register(this, "wowsuchcleaner", mainCommand, "wsc");
    
        // <<<<< Commands <<<<<
        
    }
    
    @Override
    public void onDisable()
    {
        
        LotShowcase.closeAll();
        Vault.closeAll();
        LotOperationMenu.closeAll();
        
        auctionDataManager.shutdownGracefully();

        CommandUtils.unregister("wowsuchcleaner");
        CommandUtils.unregister("wsc");
        CommandUtils.unregister(this.getName() + ":wowsuchcleaner");
        CommandUtils.unregister(this.getName() + ":wsc");
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTasks(this);
        
    }
    
    public static Main getInstance()
    {
        return instance;
    }
    
    public SharedConfigManager getSharedConfigManager()
    {
        return sharedConfigManager;
    }
    
    public RegionalConfigManager getRegionalConfigManager()
    {
        return regionalConfigManager;
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
    
    public ActiveCleaner getActiveCleaner()
    {
        return activeCleaner;
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
