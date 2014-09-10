package com.tguzik.metrics.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.junit.Before;
import org.junit.Test;

public class BasicInstrumentationTest {
    private static final String NAME = "custom metric name";
    private BasicInstrumentation basicInstrumentation;
    private MetricRegistry registry;

    @Before
    public void setUp() throws Exception {
        this.registry = new MetricRegistry();
        this.basicInstrumentation = new BasicInstrumentation( registry, NAME );
    }

    @Test
    public void testBasicInstrumentation_createsTwoCounters() {
        assertThat( registry.getCounters() ).hasSize( 2 );
    }

    @Test
    public void testBasicInstrumentation_createsTotalInvocationsCounter() {
        assertThat( registry.getCounters() ).containsKey( NAME + ".totalInvocations" );
        assertThat( registry.getCounters().get( NAME + ".totalInvocations" ).getCount() ).isZero();
    }

    @Test
    public void testBasicInstrumentation_createsTotalFailuresCounter() {
        assertThat( registry.getCounters() ).containsKey( NAME + ".totalFailures" );
        assertThat( registry.getCounters().get( NAME + ".totalFailures" ).getCount() ).isZero();
    }

    @Test
    public void testBasicInstrumentation_createsTwoMeters() {
        assertThat( registry.getMeters() ).hasSize( 2 );
    }

    @Test
    public void testBasicInstrumentation_createsInvocationsPerSecondMeter() {
        assertThat( registry.getMeters() ).containsKey( NAME + ".invocationsPerSecond" );
        assertThat( registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() ).isZero();
    }

    @Test
    public void testBasicInstrumentation_createsFailuresPerSecondMeter() {
        assertThat( registry.getMeters() ).containsKey( NAME + ".failuresPerSecond" );
        assertThat( registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() ).isZero();
    }

    @Test
    public void testBasicInstrumentation_createsElapsedTimer() {
        assertThat( registry.getTimers() ).containsKey( NAME + ".elapsed" );
        assertThat( registry.getTimers().get( NAME + ".elapsed" ).getCount() ).isZero();
    }

    @Test
    public void testMarkInvoked() {
        basicInstrumentation.markInvoked();

        // Verify update
        assertThat( registry.getCounters().get( NAME + ".totalInvocations" ).getCount() ).isEqualTo( 1 );
        assertThat( registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() ).isEqualTo( 1 );

        // Verify these mybatis are not updated
        assertThat( registry.getCounters().get( NAME + ".totalFailures" ).getCount() ).isZero();
        assertThat( registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() ).isZero();
        assertThat( registry.getTimers().get( NAME + ".elapsed" ).getCount() ).isZero();
    }

    @Test
    public void testMarkFailed() {
        basicInstrumentation.markFailed();

        // Verify update
        assertThat( registry.getCounters().get( NAME + ".totalFailures" ).getCount() ).isEqualTo( 1 );
        assertThat( registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() ).isEqualTo( 1 );

        // Verify these mybatis are not updated
        assertThat( registry.getCounters().get( NAME + ".totalInvocations" ).getCount() ).isZero();
        assertThat( registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() ).isZero();
        assertThat( registry.getTimers().get( NAME + ".elapsed" ).getCount() ).isZero();
    }

    @Test
    public void testOpenTimerContext() {
        Timer.Context context = basicInstrumentation.openTimerContext();
        assertNotNull( context );
        context.close();

        // Verify update
        assertThat( registry.getTimers().get( NAME + ".elapsed" ).getCount() ).isEqualTo( 1 );

        // Verify these mybatis are not updated
        assertThat( registry.getCounters().get( NAME + ".totalFailures" ).getCount() ).isZero();
        assertThat( registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() ).isZero();
        assertThat( registry.getCounters().get( NAME + ".totalInvocations" ).getCount() ).isZero();
        assertThat( registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() ).isZero();
    }
}
