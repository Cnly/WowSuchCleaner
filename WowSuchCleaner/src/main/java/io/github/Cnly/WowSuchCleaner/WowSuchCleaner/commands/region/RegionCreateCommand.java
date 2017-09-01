package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.commands.region;

import io.github.Cnly.Crafter.Crafter.framework.commands.AbstractCrafterCommand;
import io.github.Cnly.Crafter.Crafter.framework.commands.ArgumentLengthValidator;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.BoxRegionSetter;
import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.IBoxRegionSettingListener;
import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.IRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.ActiveCleaner;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.RegionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region.WSCApproxBoxRegion;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class RegionCreateCommand extends AbstractCrafterCommand
{
    
    private Main main = Main.getInstance();
    private FileConfiguration config = main.getConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private RegionalConfigManager regionalConfigManager = main.getRegionalConfigManager();
    private ActiveCleaner activeCleaner = main.getActiveCleaner();
    
    private BoxRegionSetter regionSetter = new BoxRegionSetter(main, WSCApproxBoxRegion.class, new RegionSetterListener());
    
    private HashMap<UUID, RegionConfig> playerRegionConfigMap = new HashMap<>();
    
    public RegionCreateCommand()
    {
        this.setAction("create");
        this.setArgumentOffset(1);
        this.setArgumentValidator(new ArgumentLengthValidator().setExactLength(3));
        this.setPlayerNeeded(true);
        this.setPlayerNeededNotice(localeManager.getLocalizedString("commands.playerNeeded"));
        this.setHelp(localeManager.getLocalizedString("commands.region.create.help").replace("{usage}", "/wsc region create <region name> <auction profile> <cleaning profile>"));
        this.setPermission("WowSuchCleaner.commands.region.create");
        this.setPermissionNeededNotice(localeManager.getLocalizedString("commands.noPermission"));
    }
    
    @Override
    protected void executeCommand(CommandSender sender, String[] args)
    {
        
        Player p = (Player)sender;
        String regionName = args[0];
        String auctionProfileName = args[1];
        String cleaningProfileName = args[2];
    
        if(regionName.length() == 0)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.emptyRegionName"));
            return;
        }
    
        if(regionalConfigManager.getRegionConfig(regionName) != null)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.regionNameAlreadyExists"));
            return;
        }
    
        if(auctionProfileName.length() == 0)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.emptyAuctionProfileName"));
            return;
        }
    
        if(!config.contains("auction.profiles." + auctionProfileName))
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.noSuchAuctionProfile"));
            return;
        }
    
        if(cleaningProfileName.length() == 0)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.emptyCleaningProfileName"));
            return;
        }
    
        if(!config.contains("cleaning.profiles." + cleaningProfileName))
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.noSuchCleaningProfile"));
            return;
        }
    
        RegionConfig regionConfig = new RegionConfig(regionName, null,
                                                     sharedConfigManager.getAuctionConfig(auctionProfileName),
                                                     sharedConfigManager.getCleaningConfig(cleaningProfileName));
        playerRegionConfigMap.put(p.getUniqueId(), regionConfig);
        
        regionSetter.enterSettingMode(p);
    
    }
    
    private class RegionSetterListener implements IBoxRegionSettingListener
    {
    
        @Override
        public void onEnterSettingMode(Player p)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.create.settingModeEntered"));
        }
    
        @Override
        public void onExitSettingMode(Player p)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.create.settingModeExited"));
            playerRegionConfigMap.remove(p.getUniqueId());
        }
    
        @Override
        public void onLocationSet(Player p, int i, Location location)
        {
            p.sendMessage(localeManager.getLocalizedString("commands.region.create.pointSet").replace("{pointIndex}", String.valueOf(i)));
        }
    
        @Override
        public void onRegionBuilt(Player p, IRegion region)
        {
            
            WSCApproxBoxRegion wscRegion = (WSCApproxBoxRegion)region;
            if(regionalConfigManager.getRegionConfig(wscRegion) != null)
            {
                p.sendMessage(localeManager.getLocalizedString("commands.region.create.regionAreaAlreadyExists"));
                return;
            }
    
            p.sendMessage(localeManager.getLocalizedString("commands.region.create.regionBuilt"));
    
            RegionConfig originalRegionConfig = playerRegionConfigMap.remove(p.getUniqueId());
            RegionConfig regionConfig = new RegionConfig(originalRegionConfig.getRegionName(),
                                                         wscRegion,
                                                         originalRegionConfig.getAuctionConfig(),
                                                         originalRegionConfig.getCleaningConfig());
            
            regionalConfigManager.createRegion(wscRegion, regionConfig);
            activeCleaner.regionCreated(regionConfig);
            
        }
    }
    
}
