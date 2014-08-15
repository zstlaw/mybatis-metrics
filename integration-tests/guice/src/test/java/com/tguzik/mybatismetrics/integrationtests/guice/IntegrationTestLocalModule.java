package com.tguzik.mybatismetrics.integrationtests.guice;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.AbstractModule;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class IntegrationTestLocalModule extends AbstractModule {
    private final MetricRegistry metricRegistry;

    public IntegrationTestLocalModule( MetricRegistry metricRegistry ) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    protected void configure() {
        bind( MetricRegistry.class ).toInstance( metricRegistry );
    }
}
