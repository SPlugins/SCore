package com.ssomar.score.api.custompiglinstrades.config;

import com.ssomar.score.sobject.SObjectWithActivators;

/**
 * A CustomPiglinsTrades trade configuration.
 */
public interface TradeInterface extends SObjectWithActivators {

    /**
     * Get the id of this trade.
     *
     * @return the id
     */
    String getId();
}
