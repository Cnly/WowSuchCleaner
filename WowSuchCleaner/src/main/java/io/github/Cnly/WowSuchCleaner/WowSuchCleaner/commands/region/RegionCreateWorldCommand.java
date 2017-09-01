package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.commands.ArgumentLengthValidator;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.ActiveCleaner;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning.CleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.RegionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WorldRegion;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public class RegionCreateWorldCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private RegionalConfigManager regionalConfigManager = main.getRegionalConfigManager();
    private ActiveCleaner activeCleaner = main.getActiveCleaner();
    
    public RegionCreateWorldCommand()
    {
        this.setAction("createworld");
        this.setArgumentOffset(1);
        this.setArgumentValidator(new ArgumentLengthValidator().setExactLength(4));
        this.setPlayerNeeded(false);
        this.setHelp(localeManager.getLocalizedString("commands.region.createWorld.help").replace("{usage}", "/wsc region createworld <region name> <world name> <auction profile> <cleaning profile>"));
        this.setPermission("WowSuchCleaner.commands.region.createWorld");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }
    
    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
    
        String regionName = args[0];
        String worldName = args[1];
        String auctionProfileName = args[2];
        String cleaningProfileName = args[3];
    
        if(regionName.length() == 0)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.emptyRegionName"));
            return;
        }
    
        if(regionalConfigManager.getRegionConfig(regionName) != null)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.regionNameAlreadyExists"));
            return;
        }
    
        if(auctionProfileName.length() == 0)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.emptyAuctionProfileName"));
            return;
        }
    
        AuctionConfig auctionConfig = sharedConfigManager.getAuctionConfig(auctionProfileName);
    
        if(null == auctionConfig)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.noSuchAuctionProfile"));
            return;
        }
    
        if(cleaningProfileName.length() == 0)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.emptyCleaningProfileName"));
            return;
        }
    
        CleaningConfig cleaningConfig = sharedConfigManager.getCleaningConfig(cleaningProfileName);
    
        if(null == cleaningConfig)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.noSuchCleaningProfile"));
            return;
        }
    
        WorldRegion region = new WorldRegion(worldName);
        RegionConfig regionConfig = new RegionConfig(regionName, region, auctionConfig, cleaningConfig);
    
        if(regionalConfigManager.getRegionConfig(region) != null)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.createWorld.worldRegionAlreadyExists"));
            return;
        }
    
        regionalConfigManager.createRegion(region, regionConfig);
        activeCleaner.regionCreated(regionConfig);
        
        sender.sendMessage(localeManager.getLocalizedString("commands.region.createWorld.success"));
    
        if(Bukkit.getWorld(worldName) == null)
        {
            sender.sendMessage(localeManager.getLocalizedString("commands.region.createWorld.noSuchWorldWarning"));
        }
        
    }
    
}
