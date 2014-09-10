package com.tguzik.metrics.mybatis.integrationtests;

import static com.codahale.metrics.MetricRegistry.name;
import static org.assertj.core.api.Assertions.assertThat;

import com.codahale.metrics.MetricRegistry;

/**
 * The reason why we're bothering with inheritance in these integration tests is that we want to have exactly same
 * tests for each of MyBatis initialization methods.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class IntegrationTestVerificationUtil {
    public static void validateSuccessfulOperation( MetricRegistry registry, String baseMetricName ) {
        validate( registry, baseMetricName, 1, 0 );
    }

    public static void validateFailingOperation( MetricRegistry registry, String baseMetricName ) {
        validate( registry, baseMetricName, 1, 1 );
    }

    private static void validate( MetricRegistry registry,
                                  String base,
                                  long expectedInvocations,
                                  long expectedFailures ) {
        // Counters
        assertThat( registry.getCounters() ).containsKeys( name( base, "totalInvocations" ),
                                                           name( base, "totalFailures" ) );
        assertThat( counterValue( registry, base, "totalInvocations" ) ).isEqualTo( expectedInvocations );
        assertThat( counterValue( registry, base, "totalFailures" ) ).isEqualTo( expectedFailures );

        // Meters
        assertThat( registry.getMeters() ).containsKeys( name( base, "invocationsPerSecond" ),
                                                         name( base, "failuresPerSecond" ) );
        assertThat( meterValue( registry, base, "invocationsPerSecond" ) ).isEqualTo( expectedInvocations );
        assertThat( meterValue( registry, base, "failuresPerSecond" ) ).isEqualTo( expectedFailures );

        // Timers
        assertThat( registry.getTimers() ).containsKey( name( base, "elapsed" ) );
        assertThat( timerCount( registry, base, "elapsed" ) ).isEqualTo( expectedInvocations );
    }

    private static long counterValue( MetricRegistry registry, String baseName, String item ) {
        return registry.counter( name( baseName, item ) ).getCount();
    }

    private static long meterValue( MetricRegistry registry, String baseName, String item ) {
        return registry.meter( name( baseName, item ) ).getCount();
    }

    private static long timerCount( MetricRegistry registry, String baseName, String item ) {
        return registry.timer( name( baseName, item ) ).getCount();
    }
}
