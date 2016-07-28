package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import io.github.Cnly.Crafter.Crafter.framework.configs.CrafterYamlConfigManager;
import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.Crafter.Crafter.utils.CompatUtils;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionableItem;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.LotOperationMenu;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.gui.LotShowcase;

public class AuctionDataManager
{
    
    private Main main = Main.getInstance();
    private AuctionConfig auctionConfig = main.getAuctionConfig();
    private ILocaleManager localeManager = main.getLocaleManager();
    
    private CrafterYamlConfigManager data = new CrafterYamlConfigManager(new File(main.getDataFolder(), "auctionData.yml"), false, main)
    {
        @Override
        public CrafterYamlConfigManager save()
        {
            return super.save();
        }
        @Override
        protected Runnable getAsynchronousSaveRunnable()
        {
            AuctionDataManager.this.save();
            return super.getAsynchronousSaveRunnable();
        }
    };
    
    private ArrayList<Lot> lots = new ArrayList<>();

    public AuctionDataManager()
    {
        this.load();
        data.setAutoSaveInterval(60);
        data.setAsynchronousAutoSave(true);
        new LotMaintainTask().runTaskTimer(main, 20L, 20L);
    }
    
    public void shutdownGracefully()
    {
        data.setAutoSaveInterval(0);
        data.shutdownAsynchronousSavingConsumer();
        this.save();
        data.save();
    }
    
    public Map<UUID, ItemStack> getVaultContents(Player p)
    {
        
        String vaultPath = new StringBuilder(43).append("vaults.").append(p.getUniqueId()).toString();
        ConfigurationSection singlePlayerVaultSection = data.getConfigurationSection(vaultPath);
        
        HashMap<UUID, ItemStack> result = new HashMap<>();
        
        for(String lotUuidString : singlePlayerVaultSection.getKeys(false))
        {
            
            if(lotUuidString.length() != 36) continue; // There is an itemCount field
            
            result.put(UUID.fromString(lotUuidString), singlePlayerVaultSection.getItemStack(lotUuidString));
            
        }
        
        return result;
    }
    
    public boolean removeVaultItem(Player p, UUID lotUuid)
    {
        
        String vaultPath = new StringBuilder(43).append("vaults.").append(p.getUniqueId()).toString();
        ConfigurationSection singlePlayerVaultSection = data.getConfigurationSection(vaultPath);
        String uuidString = lotUuid.toString();
        
        if(singlePlayerVaultSection.isSet(uuidString))
        {
            singlePlayerVaultSection.set(uuidString, null);
            unoccupyVault(p.getUniqueId());
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    public boolean hasLot(Lot lot)
    {
        return lots.contains(lot);
    }
    
    public boolean addLot(ItemStack item)
    {
        
        AuctionableItem ai = auctionConfig.getAuctionableItemConfig(item);
        
        if(null == ai) return false;
        
        double startingPrice = (((int)(ai.getStartingPrice() * 100)) * item.getAmount()) / 100D;
        double minimumIncrement = (((int)(ai.getMinimumIncrement() * 100)) * item.getAmount()) / 100D;
        
        Lot lot = new Lot(item, false, startingPrice, null, null, -1, minimumIncrement, System.currentTimeMillis() + ai.getPreserveTimeInSeconds() * 1000, ai.getAuctionDurationInSeconds() * 1000);
        lots.add(lot);
        
        LotShowcase.updateAll();
        
        return true;
    }
    
    public void addLots(List<ItemStack> items)
    {
        
        for(ItemStack item : items)
        {
            
            AuctionableItem ai = auctionConfig.getAuctionableItemConfig(item);
            
            if(null == ai) continue;
            
            double startingPrice = (((int)(ai.getStartingPrice() * 100)) * item.getAmount()) / 100D;
            double minimumIncrement = (((int)(ai.getMinimumIncrement() * 100)) * item.getAmount()) / 100D;
            
            Lot lot = new Lot(item, false, startingPrice, null, null, -1, minimumIncrement, System.currentTimeMillis() + ai.getPreserveTimeInSeconds() * 1000, ai.getAuctionDurationInSeconds() * 1000);
            lots.add(lot);
            
        }
        
        LotShowcase.updateAll();
        
    }
    
    public boolean removeLot(Lot lot)
    {
        boolean success = lots.remove(lot);
        removeFromBackend(lot);
        LotShowcase.updateAll();
        LotOperationMenu.updateAll();
        return success;
    }
    
    public List<Lot> getLots()
    {
        return Collections.unmodifiableList(lots);
    }
    
    private void save()
    {
        
        for(Lot lot : lots)
        {
            this.saveToBackend(lot);
        }
        
        // Vaults section cleanup
        ConfigurationSection vaultsSection = data.getYamlConfig().getConfigurationSection("vaults");
        if(vaultsSection != null)
        {
            for(Entry<String, Object> e : vaultsSection.getValues(false).entrySet())
            {
                
                Object v = e.getValue();
                if(!(v instanceof ConfigurationSection)) continue;
                
                ConfigurationSection singleVaultSection = (ConfigurationSection)v;
                int size = singleVaultSection.getKeys(false).size();
                
                if(size == 0 || (size == 1 && singleVaultSection.getInt("itemCount", -1) == 0))
                {
                    vaultsSection.set(e.getKey(), null);
                }
                
            }
        }
        
    }
    
    public boolean hasBidBefore(Player p, Lot lot)
    {
        String depositPath = new StringBuilder(86).append("lots.").append(lot.getUuid()).append(".deposit.").append(p.getUniqueId()).toString();
        return data.isSet(depositPath);
    }
    
    private void saveToBackend(Lot lot)
    {
        
        UUID uuid = lot.getUuid();
        String uuidString = uuid.toString();
        ItemStack item = lot.getItem();
        boolean started = lot.isStarted();
        double price = lot.getPrice();
        String lastBidPlayerName = lot.getLastBidPlayerName();
        UUID lastBidPlayerUuid = lot.getLastBidPlayerUuid();
        double lastBidPrice = lot.getLastBidPrice();
        double minimumIncrement = lot.getMinimumIncrement();
        long preserveTimeExpire = lot.getPreserveTimeExpire();
        long auctionDurationExpire = lot.getAuctionDurationExpire();
        
        String sectionPath = "lots." + uuidString;
        ConfigurationSection singleLotSection = data.getYamlConfig().getConfigurationSection(sectionPath);
        if(null == singleLotSection)
        {
            singleLotSection = data.getYamlConfig().createSection(sectionPath);
        }
        
        singleLotSection.set("item", item);
        singleLotSection.set("started", started);
        singleLotSection.set("price", price);
        singleLotSection.set("lastBidPlayerName", lastBidPlayerName);
        singleLotSection.set("lastBidPlayerUuid", null == lastBidPlayerUuid ? null : lastBidPlayerUuid.toString());
        singleLotSection.set("lastBidPrice", lastBidPrice);
        singleLotSection.set("minimumIncrement", minimumIncrement);
        singleLotSection.set("preserveTimeExpire", preserveTimeExpire);
        singleLotSection.set("auctionDurationExpire", auctionDurationExpire);
        
    }
    
    private void removeFromBackend(Lot lot)
    {
        data.set("lots." + lot.getUuid().toString(), null);
    }
    
    public void bid(Player p, boolean anonymous, Lot lot, double priceIncrement)
    {
        
        if(hasBidBefore(p, lot))
        {
            addDeposit(lot, p, priceIncrement);
        }
        else
        {
            occupyVault(p);
            addDeposit(lot, p, lot.getPrice() + priceIncrement);
        }
        
        if(!lot.isStarted())
        {
            lot.setAuctionDurationExpire(lot.getAuctionDurationExpire() + System.currentTimeMillis());
            lot.setStarted(true);
        }
        
        lot.setLastBidPlayerName(anonymous ? localeManager.getLocalizedString("ui.anonymous") : p.getName());
        lot.setLastBidPlayerUuid(p.getUniqueId());
        lot.setLastBidPrice(priceIncrement);
        lot.setPrice(lot.getPrice() + priceIncrement);
        
        LotShowcase.updateAll();
        LotOperationMenu.updateAll();
        
    }
    
    public long getLastBid(Player p, Lot lot)
    {
        String lastBidPath = new StringBuilder(90).append("lots.").append(lot.getUuid()).append(".bid.lastBid.").append(p.getUniqueId()).toString();
        return data.getYamlConfig().getLong(lastBidPath);
    }
    
    public void setLastBid(Player p, Lot lot, long lastBid)
    {
        String lastBidPath = new StringBuilder(90).append("lots.").append(lot.getUuid()).append(".bid.lastBid.").append(p.getUniqueId()).toString();
        data.set(lastBidPath, lastBid);
    }
    
    public boolean isVaultAvailable(Player p)
    {
        
        String vaultPath = new StringBuilder(43).append("vaults.").append(p.getUniqueId()).toString();
        ConfigurationSection singlePlayerVaultSection = data.getConfigurationSection(vaultPath);
        
        return singlePlayerVaultSection.getInt("itemCount", 0) < auctionConfig.getVaultCapacity(p);
    }
    
    public boolean occupyVault(Player p)
    {
        
        String vaultPath = new StringBuilder(43).append("vaults.").append(p.getUniqueId()).toString();
        ConfigurationSection singlePlayerVaultSection = data.getConfigurationSection(vaultPath);
        int itemCount = singlePlayerVaultSection.getInt("itemCount", 0);
        
        if(itemCount < auctionConfig.getVaultCapacity(p))
        {
            singlePlayerVaultSection.set("itemCount", ++itemCount);
            return true;
        }
        else
        {
            return false;
        }
        
    }
    
    public void unoccupyVault(UUID uuid)
    {
        
        String vaultPath = new StringBuilder(43).append("vaults.").append(uuid).toString();
        ConfigurationSection singlePlayerVaultSection = data.getConfigurationSection(vaultPath);
        int itemCount = singlePlayerVaultSection.getInt("itemCount", 0);
        
        if(itemCount <= 0) return;
        
        singlePlayerVaultSection.set("itemCount", --itemCount);
        
    }
    
    public void addDepositToVault(UUID uuid, double deposit)
    {
        String path = new StringBuilder(51).append("vaults.").append(uuid.toString()).append('.').append("deposit").toString();
        double original = data.getYamlConfig().getDouble(path, 0);
        data.set(path, original + deposit);
    }
    
    public void removeDepositFromVault(UUID uuid, double deposit)
    {
        String path = new StringBuilder(51).append("vaults.").append(uuid.toString()).append('.').append("deposit").toString();
        double original = data.getYamlConfig().getDouble(path, 0);
        double newDeposit = original - deposit;
        if(newDeposit <= 0)
        {
            data.set(path, null);
        }
        else
        {
            data.set(path, newDeposit);
        }
    }
    
    public void removeDepositFromVault(UUID uuid)
    {
        String path = new StringBuilder(51).append("vaults.").append(uuid.toString()).append('.').append("deposit").toString();
        data.set(path, null);
    }
    
    public double getDepositInVault(UUID uuid)
    {
        String path = new StringBuilder(51).append("vaults.").append(uuid.toString()).append('.').append("deposit").toString();
        return data.getYamlConfig().getDouble(path, 0);
    }
    
    public void returnDepositInVault(UUID uuid)
    {
        Player p = CompatUtils.getPlayer(uuid);
        double deposit = getDepositInVault(uuid);
        if(deposit <= 0) return;
        Main.economy.depositPlayer(p, deposit);
        removeDepositFromVault(uuid);
    }
    
    public void addDeposit(Lot lot, Player p, double deposit)
    {
        String depositPath = new StringBuilder(86).append("lots.").append(lot.getUuid()).append(".deposit.").append(p.getUniqueId()).toString();
        data.set(depositPath, data.getYamlConfig().getDouble(depositPath, 0) + deposit);
    }
    
    public Map<UUID, Double> getDeposit(Lot lot)
    {
        
        HashMap<UUID, Double> result = new HashMap<>();
        
        String path = new StringBuilder(49).append("lots.").append(lot.getUuid()).append(".deposit").toString();
        ConfigurationSection singleLotDepositSection = data.getConfigurationSection(path);
        
        for(String uuidString : singleLotDepositSection.getKeys(false))
        {
            
            UUID uuid = UUID.fromString(uuidString);
            result.put(uuid, singleLotDepositSection.getDouble(uuidString));
            
        }
        
        return result;
    }
    
    public void returnDeposit(Lot lot)
    {
        for(Entry<UUID, Double> e : getDeposit(lot).entrySet())
        {
            Player p = CompatUtils.getPlayer(e.getKey());
            if(p != null)
            {
                Main.economy.depositPlayer(p, e.getValue());
            }
            else
            {
                addDepositToVault(e.getKey(), e.getValue());
            }
        }
    }
    
    public void hammer(Lot lot)
    {
        
        UUID buyerUuid = lot.getLastBidPlayerUuid();
        Player buyer = CompatUtils.getPlayer(buyerUuid);
        if(null == buyer || buyer.getInventory().firstEmpty() == -1)
        {
            String path = new StringBuilder(80).append("vaults.").append(buyerUuid.toString()).append('.').append(lot.getUuid()).toString();
            data.set(path, lot.getItem());
        }
        else
        {
            unoccupyVault(buyerUuid);
            buyer.getInventory().addItem(lot.getItem());
        }
        
        if(buyer != null) buyer.sendMessage(localeManager.getLocalizedString("ui.hammerBuyer"));
        
        Map<UUID, Double> depositMap = getDeposit(lot);
        double buyerDeposit = depositMap.get(buyerUuid);
        addToTransferAccount(buyerDeposit);
        depositMap.remove(buyerUuid);
        
        for(Entry<UUID, Double> e : depositMap.entrySet())
        {
            
            Player p = CompatUtils.getPlayer(e.getKey());
            
            if(null != p)
            {
                p.sendMessage(localeManager.getLocalizedString("ui.hammerOthers"));
                Main.economy.depositPlayer(p, e.getValue());
            }
            else
            {
                addDepositToVault(e.getKey(), e.getValue());
            }
            
            unoccupyVault(e.getKey());
            
        }
        
        removeLot(lot);
        
    }
    
    private void load()
    {
        
        ConfigurationSection lotsSection = data.getConfigurationSection("lots");
        
        for(String uuidString : lotsSection.getKeys(false))
        {
            
            ConfigurationSection singleLotSection = lotsSection.getConfigurationSection(uuidString);
            
            ItemStack item = singleLotSection.getItemStack("item");
            boolean started = singleLotSection.getBoolean("started");
            double price = singleLotSection.getDouble("price");
            String lastBidPlayerName = singleLotSection.getString("lastBidPlayerName");
            UUID lastBidPlayerUuid = singleLotSection.isSet("lastBidPlayerUuid") ? UUID.fromString(singleLotSection.getString("lastBidPlayerUuid")) : null;
            double lastBidPrice = singleLotSection.getDouble("lastBidPrice");
            double minimumIncrement = singleLotSection.getDouble("minimumIncrement");
            long preserveTimeExpire = singleLotSection.getLong("preserveTimeExpire");
            long auctionDurationExpire = singleLotSection.getLong("auctionDurationExpire");
            
            Lot lot = new Lot(UUID.fromString(uuidString), item, started, price, lastBidPlayerName, lastBidPlayerUuid, lastBidPrice, minimumIncrement, preserveTimeExpire, auctionDurationExpire);
            lots.add(lot);
            
        }
        
    }
    
    private class LotMaintainTask extends BukkitRunnable
    {

        @Override
        public void run()
        {
            
            long currentTime = System.currentTimeMillis();
            List<Lot> lotsToHammer = null;
            List<Lot> lotsToRemove = null;
            
            for(Lot lot : lots)
            {
                if(lot.isStarted())
                {
                    if(currentTime > lot.getAuctionDurationExpire())
                    {
                        if(null == lotsToHammer)
                        {
                            lotsToHammer = new ArrayList<Lot>();
                        }
                        lotsToHammer.add(lot);
                    }
                }
                else
                {
                    if(currentTime > lot.getPreserveTimeExpire())
                    {
                        if(null == lotsToRemove)
                        {
                            lotsToRemove = new ArrayList<Lot>();
                        }
                        lotsToRemove.add(lot);
                    }
                }
            }
            
            if(lotsToHammer != null)
            {
                for(Lot lot : lotsToHammer)
                {
                    hammer(lot);
                }
            }
            
            if(lotsToRemove != null)
            {
                for(Lot lot : lotsToRemove)
                {
                    removeLot(lot);
                }
            }
            
        }
        
    }
    
    public void addToTransferAccount(Double amount)
    {
        if(auctionConfig.getTransferAccount().length() != 0)
        {
            Main.economy.depositPlayer(auctionConfig.getTransferAccount(), amount);
        }
    }
    
}
