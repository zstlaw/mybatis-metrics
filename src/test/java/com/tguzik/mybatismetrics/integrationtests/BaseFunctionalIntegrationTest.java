package com.tguzik.mybatismetrics.integrationtests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Map;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.tguzik.mybatismetrics.PropertyBootstrappedInstrumentingInterceptor;
import com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap.FakeMapper;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.junit.Before;
import org.junit.Test;

/**
 * The reason why we're bothering with inheritance in these integration tests is that we want to have exactly same
 * tests for each of MyBatis initialization methods.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public abstract class BaseFunctionalIntegrationTest {
    private MetricRegistry metricRegistry;



    protected void validateSuccessfulOperation( MetricRegistry registry, String baseMetricName ) {
        // Preconditions
        assertThat( registry ).isNotNull();
        assertThat( baseMetricName ).isNotEmpty();

        // Convenience
        Map<String, Counter> counters = registry.getCounters();
        Map<String, Meter> meters = registry.getMeters();
        Map<String, Timer> timers = registry.getTimers();

        // Actual verifications
        assertThat( counters ).containsKeys( baseMetricName + ".totalInvocations", baseMetricName + ".totalFailures" );
        assertThat( counters.get( baseMetricName + ".totalInvocations" ).getCount() ).isEqualTo( 1 );
        assertThat( counters.get( baseMetricName + ".totalFailures" ).getCount() ).isZero();

        assertThat( meters ).containsKeys( baseMetricName + ".invocationsPerSecond",
                                           baseMetricName + ".failuresPerSecond" );
        assertThat( meters.get( baseMetricName + ".invocationsPerSecond" ).getCount() ).isEqualTo( 1 );
        assertThat( meters.get( baseMetricName + ".failuresPerSecond" ).getCount() ).isZero();

        assertThat( timers ).containsKey( baseMetricName + ".elapsed" );
        assertThat( timers.get( baseMetricName + ".elapsed" ).getCount() ).isEqualTo( 1 );
    }

    protected void validateFailedOperation( MetricRegistry registry, String baseMetricName ) {
        // Preconditions
        assertThat( registry ).isNotNull();
        assertThat( baseMetricName ).isNotEmpty();

        // Convenience
        Map<String, Counter> counters = registry.getCounters();
        Map<String, Meter> meters = registry.getMeters();
        Map<String, Timer> timers = registry.getTimers();

        // Actual verifications
        assertThat( counters ).containsKeys( baseMetricName + ".totalInvocations", baseMetricName + ".totalFailures" );
        assertThat( counters.get( baseMetricName + ".totalInvocations" ).getCount() ).isZero();
        assertThat( counters.get( baseMetricName + ".totalFailures" ).getCount() ).isEqualTo( 1 );

        assertThat( meters ).containsKeys( baseMetricName + ".invocationsPerSecond",
                                           baseMetricName + ".failuresPerSecond" );
        assertThat( meters.get( baseMetricName + ".invocationsPerSecond" ).getCount() ).isZero();
        assertThat( meters.get( baseMetricName + ".failuresPerSecond" ).getCount() ).isEqualTo( 1 );

        assertThat( timers ).containsKey( baseMetricName + ".elapsed" );
        assertThat( timers.get( baseMetricName + ".elapsed" ).getCount() ).isEqualTo( 1 );
    }
}
