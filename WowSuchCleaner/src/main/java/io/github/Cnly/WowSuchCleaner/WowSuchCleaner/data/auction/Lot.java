package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.data.auction;

import java.util.UUID;

import org.bukkit.inventory.ItemStack;

public class Lot
{
    
    private final UUID uuid;
    private final ItemStack item;
    private boolean started;
    private double price;
    private String lastBidPlayerName;
    private UUID lastBidPlayerUuid;
    private double lastBidPrice;
    private final double minimumIncrement;
    private final long preserveTimeExpire;
    private long auctionDurationExpire;
    
    public Lot(ItemStack item, boolean started, double price, String lastBidPlayerName, UUID lastBidPlayerUuid, double lastBidPrice, double minimumIncrement, long preserveTimeExpire,
            long auctionDurationExpire)
    {
        this(UUID.randomUUID(), item, started, price, lastBidPlayerName, lastBidPlayerUuid, lastBidPrice, minimumIncrement, preserveTimeExpire, auctionDurationExpire);
    }

    public Lot(UUID uuid, ItemStack item, boolean started, double price, String lastBidPlayerName, UUID lastBidPlayerUuid, double lastBidPrice, double minimumIncrement, long preserveTimeExpire,
            long auctionDurationExpire)
    {
        super();
        this.uuid = uuid;
        this.item = item;
        this.started = started;
        this.price = price;
        this.lastBidPlayerName = lastBidPlayerName;
        this.lastBidPlayerUuid = lastBidPlayerUuid;
        this.lastBidPrice = lastBidPrice;
        this.minimumIncrement = minimumIncrement;
        this.preserveTimeExpire = preserveTimeExpire;
        this.auctionDurationExpire = auctionDurationExpire;
    }

    public boolean isStarted()
    {
        return started;
    }

    public void setStarted(boolean started)
    {
        this.started = started;
    }

    public double getPrice()
    {
        return price;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }

    public String getLastBidPlayerName()
    {
        return lastBidPlayerName;
    }

    public void setLastBidPlayerName(String lastBidPlayerName)
    {
        this.lastBidPlayerName = lastBidPlayerName;
    }

    public UUID getLastBidPlayerUuid()
    {
        return lastBidPlayerUuid;
    }

    public void setLastBidPlayerUuid(UUID lastBidPlayerUuid)
    {
        this.lastBidPlayerUuid = lastBidPlayerUuid;
    }

    public double getLastBidPrice()
    {
        return lastBidPrice;
    }

    public void setLastBidPrice(double lastBidPrice)
    {
        this.lastBidPrice = lastBidPrice;
    }

    public long getAuctionDurationExpire()
    {
        return auctionDurationExpire;
    }

    public void setAuctionDurationExpire(long auctionDurationExpire)
    {
        this.auctionDurationExpire = auctionDurationExpire;
    }

    public UUID getUuid()
    {
        return uuid;
    }

    public ItemStack getItem()
    {
        return item;
    }

    public double getMinimumIncrement()
    {
        return minimumIncrement;
    }

    public long getPreserveTimeExpire()
    {
        return preserveTimeExpire;
    }
    
    
}
