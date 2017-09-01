package io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.region;

import io.github.Cnly.Crafter.Crafter.utils.regions.boxregions.ApproximateBoxRegion;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.cleaning.CleaningConfig;
import io.github.Cnly.WowSuchCleaner.WowSuchCleaner.config.auction.AuctionConfig;

public class RegionConfig
{
    
    private String regionName;
    private WSCApproxBoxRegion region;
    private AuctionConfig auctionConfig;
    private CleaningConfig cleaningConfig;
    
    public RegionConfig(String regionName,
                        WSCApproxBoxRegion region,
                        AuctionConfig auctionConfig,
                        CleaningConfig cleaningConfig)
    {
        this.regionName = regionName;
        this.region = region;
        this.auctionConfig = auctionConfig;
        this.cleaningConfig = cleaningConfig;
    }
    
    public String getRegionName()
    {
        return regionName;
    }
    
    public WSCApproxBoxRegion getRegion()
    {
        return region;
    }
    
    public AuctionConfig getAuctionConfig()
    {
        return auctionConfig;
    }
    
    public CleaningConfig getCleaningConfig()
    {
        return cleaningConfig;
    }
}
