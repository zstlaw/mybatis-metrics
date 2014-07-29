package com.tguzik.metrics;

import static org.junit.Assert.*;

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
        assertEquals( 2, registry.getCounters().size() );
    }

    @Test
    public void testBasicInstrumentation_createsTotalInvocationsCounter() {
        assertNotNull( registry.getCounters().get( NAME + ".totalInvocations" ) );
        assertEquals( 0, registry.getCounters().get( NAME + ".totalInvocations" ).getCount() );
    }

    @Test
    public void testBasicInstrumentation_createsTotalFailuresCounter() {
        assertNotNull( registry.getCounters().get( NAME + ".totalFailures" ) );
        assertEquals( 0, registry.getCounters().get( NAME + ".totalFailures" ).getCount() );
    }

    @Test
    public void testBasicInstrumentation_createsTwoMeters() {
        assertEquals( 2, registry.getMeters().size() );
    }

    @Test
    public void testBasicInstrumentation_createsInvocationsPerSecondMeter() {
        assertNotNull( registry.getMeters().get( NAME + ".invocationsPerSecond" ) );
        assertEquals( 0, registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() );
    }

    @Test
    public void testBasicInstrumentation_createsFailuresPerSecondMeter() {
        assertNotNull( registry.getMeters().get( NAME + ".failuresPerSecond" ) );
        assertEquals( 0, registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() );
    }

    @Test
    public void testBasicInstrumentation_createsElapsedTimer() {
        assertNotNull( registry.getTimers().get( NAME + ".elapsed" ) );
        assertEquals( 0, registry.getTimers().get( NAME + ".elapsed" ).getCount() );
    }

    @Test
    public void testMarkInvoked() {
        basicInstrumentation.markInvoked();

        // Verify update
        assertEquals( 1, registry.getCounters().get( NAME + ".totalInvocations" ).getCount() );
        assertEquals( 1, registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() );

        // Verify these metrics are not updated
        assertEquals( 0, registry.getCounters().get( NAME + ".totalFailures" ).getCount() );
        assertEquals( 0, registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() );
        assertEquals( 0, registry.getTimers().get( NAME + ".elapsed" ).getCount() );
    }

    @Test
    public void testMarkFailed() {
        basicInstrumentation.markFailed();

        // Verify update
        assertEquals( 1, registry.getCounters().get( NAME + ".totalFailures" ).getCount() );
        assertEquals( 1, registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() );

        // Verify these metrics are not updated
        assertEquals( 0, registry.getCounters().get( NAME + ".totalInvocations" ).getCount() );
        assertEquals( 0, registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() );
        assertEquals( 0, registry.getTimers().get( NAME + ".elapsed" ).getCount() );
    }

    @Test
    public void testOpenTimerContext() {
        Timer.Context context = basicInstrumentation.openTimerContext();
        assertNotNull( context );
        context.close();

        // Verify that metric was updated
        assertEquals( 1, registry.getTimers().get( NAME + ".elapsed" ).getCount() );

        // Verify that these metrics are not updated
        assertEquals( 0, registry.getCounters().get( NAME + ".totalFailures" ).getCount() );
        assertEquals( 0, registry.getMeters().get( NAME + ".failuresPerSecond" ).getCount() );
        assertEquals( 0, registry.getCounters().get( NAME + ".totalInvocations" ).getCount() );
        assertEquals( 0, registry.getMeters().get( NAME + ".invocationsPerSecond" ).getCount() );
    }
}
