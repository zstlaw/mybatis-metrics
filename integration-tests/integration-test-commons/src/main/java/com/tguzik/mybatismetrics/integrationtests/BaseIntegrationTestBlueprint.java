package com.tguzik.mybatismetrics.integrationtests;

import org.junit.Test;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public abstract class BaseIntegrationTestBlueprint {
    @Test
    public abstract void testMyBatisConfiguration_containsInstanceOfPropertyBootstrappedInstrumentingInterceptor();

    @Test
    public abstract void testMapperOperation_select_updatesMetricRegistry();

    @Test
    public abstract void testMapperOperation_update_updatesMetricRegistry();

    @Test
    public abstract void testMapperOperation_insert_updatesMetricRegistry();

    @Test
    public abstract void testMapperOperation_delete_updatesMetricRegistry();
}
