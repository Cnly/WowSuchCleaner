package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.listeners;

import io.github.Cnly.Crafter.Crafter.framework.locales.ILocaleManager;
import io.github.Cnly.Crafter.Crafter.utils.CompatUtils;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.Main;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.RegionalConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.SharedConfigManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.AuctionDataManager;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction.Lot;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

public class BidHandler implements Listener {

    private HashMap<UUID, BidArgument> biddingPlayers = new HashMap<>();
    private Main main = Main.getInstance();
    private ILocaleManager localeManager = main.getLocaleManager();
    private SharedConfigManager sharedConfigManager = main.getSharedConfigManager();
    private RegionalConfigManager regionalConfigManager = main.getRegionalConfigManager();
    private AuctionDataManager auctionDataManager = main.getAuctionDataManager();

    public BidHandler() {
        auctionDataManager.setBidHandler(this);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void handleBid(AsyncPlayerChatEvent e) {

        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        BidArgument arg = biddingPlayers.get(uuid);
        if (arg == null) return;

        BidCallback callback = arg.getCallback();
        Lot lot = arg.getLot();

        e.setCancelled(true);

        Bukkit.getScheduler().runTask(main, () -> {
            if (!auctionDataManager.hasLot(lot)) {
                if (lot.isStarted()) {
                    p.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
                    cancelBidRequest(p);
                    return;
                } else {
                    p.sendMessage(localeManager.getLocalizedString("ui.itemNoLongerAvailable"));
                    cancelBidRequest(p);
                    return;
                }
            }

            boolean anonymous = false;
            String msg = ChatColor.stripColor(e.getMessage());

            if (msg.length() == 0) return;
            if (msg.equalsIgnoreCase("cancel")) {
                cancelBidRequest(p);
                return;
            }
            if (msg.startsWith("a")) {
                anonymous = true;
                msg = msg.substring(1);
            }

            double bid = 0;
            try {
                bid = Double.parseDouble(msg);
            } catch (Exception ex) {
                p.sendMessage(localeManager.getLocalizedString("ui.wrongBidFormat"));
                p.sendMessage(localeManager.getLocalizedString("ui.bidPrompt"));
                return;
            }

            if (bid < lot.getMinimumIncrement()) {
                p.sendMessage(localeManager.getLocalizedString("ui.bidTooLow")
                        .replace("{minimumIncrement}", String.valueOf(lot.getMinimumIncrement()))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                return;
            }

            double charge = bid * (sharedConfigManager.getChargePercentPerBid() / 100);
            if (charge < sharedConfigManager.getMinimumChargePerBid())
                charge = sharedConfigManager.getMinimumChargePerBid();

            EconomyResponse er = null;
            if (auctionDataManager.hasBidBefore(p, lot)) {
                er = Main.economy.withdrawPlayer(p, bid + charge);
            } else {
                er = Main.economy.withdrawPlayer(p, lot.getPrice() + bid + charge);
            }

            if (!er.transactionSuccess()) {
                p.sendMessage(localeManager.getLocalizedString("ui.balanceNotEnough")
                        .replace("{balance}", String.valueOf(er.balance))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                p.sendMessage(localeManager.getLocalizedString("ui.chargePerBid")
                        .replace("{chargePercent}", String.valueOf(sharedConfigManager.getChargePercentPerBid()))
                        .replace("{minimumCharge}", String.valueOf(sharedConfigManager.getMinimumChargePerBid()))
                        .replace("{currency}", Main.economy.currencyNamePlural()));
                return;
            }

            auctionDataManager.addToTransferAccount(charge);
            auctionDataManager.bid(p, anonymous, lot, bid);
            auctionDataManager.setLastBid(p, lot, System.currentTimeMillis());

            callback.onBidSuccess(p, bid, anonymous);
            biddingPlayers.remove(uuid);

            p.sendMessage(localeManager.getLocalizedString("ui.sucessfulBid")
                    .replace("{price}", String.valueOf(bid))
                    .replace("{charge}", String.valueOf(charge))
                    .replace("{currency}", Main.economy.currencyNamePlural()));

        });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        biddingPlayers.remove(e.getPlayer().getUniqueId());
    }

    public void requestBid(Player p, Lot lot, BidCallback callback) {

        if (!auctionDataManager.hasLot(lot)) {
            p.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
            p.sendMessage(localeManager.getLocalizedString("ui.bidCancelled"));
            callback.onCancel(p);
            return;
        }

        if (!auctionDataManager.hasBidBefore(p, lot) && !auctionDataManager.isVaultAvailable(p)) {
            p.sendMessage(localeManager.getLocalizedString("ui.fullVault"));
            callback.onCancel(p);
            return;
        }

        long deltaSeconds = (System.currentTimeMillis() - auctionDataManager.getLastBid(p, lot)) / 1000;
        long bidInterval = sharedConfigManager.getBidIntervalInSeconds();

        if (deltaSeconds < bidInterval) {
            p.sendMessage(localeManager.getLocalizedString("ui.bidTooFast")
                    .replace("{time}", String.valueOf(bidInterval - deltaSeconds)));
            callback.onCancel(p);
            return;
        }

        BidArgument arg = new BidArgument(lot, callback);
        biddingPlayers.put(p.getUniqueId(), arg);

        p.sendMessage(localeManager.getLocalizedString("ui.bidPrompt"));
        p.sendMessage(localeManager.getLocalizedString("ui.chargePerBid")
                .replace("{chargePercent}", String.valueOf(sharedConfigManager.getChargePercentPerBid()))
                .replace("{minimumCharge}", String.valueOf(sharedConfigManager.getMinimumChargePerBid()))
                .replace("{currency}", Main.economy.currencyNamePlural()));

    }

    public void cancelInvalidBidState(Lot lot) {
        for (Map.Entry<UUID, BidArgument> e : biddingPlayers.entrySet()) {
            if (!e.getValue().getLot().equals(lot)) {
                continue;
            }
            cancelBidRequest(e.getKey());
        }
    }

    public void cancelInvalidBidStates(List<Lot> lots) {
        HashSet<UUID> lotUuidSet = new HashSet<>();
        for (Lot l : lots) {
            lotUuidSet.add(l.getUuid());
        }
        for (Map.Entry<UUID, BidArgument> e : biddingPlayers.entrySet()) {
            UUID lotUuid = e.getValue().getLot().getUuid();
            if (!lotUuidSet.contains(lotUuid)) {
                cancelBidRequest(e.getKey());
            }
        }
    }

    public void cancelBidRequest(Player p) {
        UUID uuid = p.getUniqueId();
        BidArgument arg = biddingPlayers.get(uuid);
        if (arg == null) return;
        BidCallback callback = arg.getCallback();
        if (arg.getLot().isStarted()) {
            p.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
        } else {
            p.sendMessage(localeManager.getLocalizedString("ui.itemNoLongerAvailable"));
        }
        p.sendMessage(localeManager.getLocalizedString("ui.bidCancelled"));
        callback.onCancel(p);
        biddingPlayers.remove(uuid);
    }

    public void cancelBidRequest(UUID uuid) {
        BidArgument arg = biddingPlayers.get(uuid);
        if (arg == null) return;
        Player p = CompatUtils.getPlayer(uuid);
        if (null != p) {
            BidCallback callback = arg.getCallback();
            if (arg.getLot().isStarted()) {
                p.sendMessage(localeManager.getLocalizedString("ui.itemAlreadySold"));
            } else {
                p.sendMessage(localeManager.getLocalizedString("ui.itemNoLongerAvailable"));
            }
            p.sendMessage(localeManager.getLocalizedString("ui.bidCancelled"));
            callback.onCancel(p);
        }
        biddingPlayers.remove(uuid);
    }

    public static interface BidCallback {

        public void onBidSuccess(Player player, double bid, boolean anonymous);

        public void onCancel(Player player);

    }

    private static class BidArgument {

        private final Lot lot;
        private final BidCallback callback;

        public BidArgument(Lot lot, BidCallback callback) {
            super();
            this.lot = lot;
            this.callback = callback;
        }

        public Lot getLot() {
            return lot;
        }

        public BidCallback getCallback() {
            return callback;
        }

    }

}
