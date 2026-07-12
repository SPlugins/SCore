package com.ssomar.score.api.custompiglinstrades.config;

import java.util.List;
import java.util.Optional;

/**
 * Manager of the CustomPiglinsTrades trades.
 */
public interface CustomPiglinsTradesManagerInterface {

    /**
     * Check whether the trades have finished loading.
     * Before that, the lookup methods return empty results.
     * Listen to {@link com.ssomar.score.api.custompiglinstrades.load.CustomPiglinsTradesPostLoadEvent}
     * to be notified when loading completes.
     *
     * @return true once all trades are loaded
     **/
    boolean isLoaded();

    /**
     * Verify if id is a valid trade ID
     *
     * @param id The ID to verify
     * @return true if it is a valid ID, false otherwise
     **/
    boolean isValidID(String id);

    /**
     * Get a trade from its ID
     *
     * @param id The ID of the trade
     * @return The trade or an empty optional
     **/
    Optional<? extends TradeInterface> getTrade(String id);

    /**
     * Get all trade Ids
     *
     * @return All trade ids
     **/
    List<String> getTradeIdsList();

    /**
     * Get all trades
     *
     * @return All trades
     **/
    List<? extends TradeInterface> getAllTrades();
}
