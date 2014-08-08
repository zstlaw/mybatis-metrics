package com.tguzik.metrics.mybatis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.apache.ibatis.plugin.Invocation;
import org.junit.Before;
import org.junit.Test;

public class InstrumentingInterceptorTest {
    private InstrumentingInterceptor interceptor;
    private MetricRegistry metricRegistry;
    private Invocation invocation;
    private FakeMapper fakeMapper;

    @Before
    public void setUp() throws Exception {
        this.metricRegistry = new MetricRegistry();
        this.interceptor = new InstrumentingInterceptor( metricRegistry );
        this.fakeMapper = mock( FakeMapper.class );
        this.invocation = new Invocation( fakeMapper,
                                          FakeMapper.class.getDeclaredMethod( "doSomething",
                                                                              String.class,
                                                                              int.class,
                                                                              int.class ),
                                          new Object[] { "arg1", 2, 3 } );
    }

    @Test
    public void testIntercept_marksMetrics() throws Throwable {
        // Prepare test by fetching appropriate metrics from the registry
        String baseMetricName = "com.tguzik.metrics.mybatis.InstrumentingInterceptorTest$FakeMapper#doSomething";
        Meter invocationsPerSecond = metricRegistry.meter( baseMetricName + ".invocationsPerSecond" );
        Meter failuresPerSecond = metricRegistry.meter( baseMetricName + ".failuresPerSecond" );
        Counter invocations = metricRegistry.counter( baseMetricName + ".totalInvocations" );
        Counter failures = metricRegistry.counter( baseMetricName + ".totalFailures" );
        Timer timer = metricRegistry.timer( baseMetricName + ".elapsed" );

        // Validate conditions before the test (just to be safe)
        assertThat( invocationsPerSecond.getCount() ).isEqualTo( 0L );
        assertThat( failuresPerSecond.getCount() ).isEqualTo( 0L );
        assertThat( invocations.getCount() ).isEqualTo( 0L );
        assertThat( failures.getCount() ).isEqualTo( 0L );
        assertThat( timer.getCount() ).isEqualTo( 0L );

        // Perform the test
        interceptor.intercept( invocation );

        // Validate that almost all metrics were incremented
        assertThat( invocationsPerSecond.getCount() ).isGreaterThan( 0L );
        assertThat( invocations.getCount() ).isGreaterThan( 0L );
        assertThat( timer.getCount() ).isGreaterThan( 0L );
        assertThat( timer.getSnapshot().getMean() ).isGreaterThan( 0.0 );

        // Validate that metrics for failure mode were NOT incremented
        assertThat( failuresPerSecond.getCount() ).isEqualTo( 0L );
        assertThat( failures.getCount() ).isEqualTo( 0L );
    }

    @Test
    public void testIntercept_marksMetrics_exceptionFromMapper() throws Throwable {
        // Prepare test by fetching appropriate metrics from the registry
        String baseMetricName = "com.tguzik.metrics.mybatis.InstrumentingInterceptorTest$FakeMapper#doSomething";
        Meter invocationsPerSecond = metricRegistry.meter( baseMetricName + ".invocationsPerSecond" );
        Meter failuresPerSecond = metricRegistry.meter( baseMetricName + ".failuresPerSecond" );
        Counter invocations = metricRegistry.counter( baseMetricName + ".totalInvocations" );
        Counter failures = metricRegistry.counter( baseMetricName + ".totalFailures" );
        Timer timer = metricRegistry.timer( baseMetricName + ".elapsed" );

        // Validate conditions before the test (just to be safe)
        assertThat( invocationsPerSecond.getCount() ).isEqualTo( 0L );
        assertThat( failuresPerSecond.getCount() ).isEqualTo( 0L );
        assertThat( invocations.getCount() ).isEqualTo( 0L );
        assertThat( failures.getCount() ).isEqualTo( 0L );
        assertThat( timer.getCount() ).isEqualTo( 0L );

        // Modify invocation to throw an exception
        doThrow( new RuntimeException( "custom exception" ) ).when( fakeMapper )
                                                             .doSomething( anyString(), anyInt(), anyInt() );

        // Perform the test
        try {
            interceptor.intercept( invocation );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // Discard the exception. It is verified in a separate test
        }

        // Validate that almost all metrics were incremented
        assertThat( invocationsPerSecond.getCount() ).isGreaterThan( 0L );
        assertThat( invocations.getCount() ).isGreaterThan( 0L );
        assertThat( timer.getCount() ).isGreaterThan( 0L );
        assertThat( timer.getSnapshot().getMean() ).isGreaterThan( 0.0 );

        // Validate that metrics for failure mode were also incremented
        assertThat( failuresPerSecond.getCount() ).isGreaterThan( 0L );
        assertThat( failures.getCount() ).isGreaterThan( 0L );
    }

    @Test
    public void testIntercept_returnsValueFromTheMapperUnmodified() throws Throwable {
        Object expectedValue = "whatever mapper returns";
        doReturn( expectedValue ).when( fakeMapper ).doSomething( anyString(), anyInt(), anyInt() );

        Object actualValue = interceptor.intercept( invocation );

        assertThat( actualValue ).isSameAs( expectedValue );
    }

    @Test
    public void testIntercept_rethrowsExceptionUnmodified() throws Throwable {
        Throwable expcetedException = new RuntimeException( "whatever mapper throws" );
        doThrow( expcetedException ).when( fakeMapper ).doSomething( anyString(), anyInt(), anyInt() );

        try {
            interceptor.intercept( invocation );
            fail( "Expected exception" );
        }
        catch ( Throwable actualException ) {
            assertThat( actualException ).isInstanceOf( InvocationTargetException.class );
            assertThat( actualException.getCause() ).isSameAs( expcetedException );
        }
    }

    @Test
    public void testPlugin_returnsParameterUnchanged() throws Exception {
        Object arg = "any object. really.";

        assertThat( interceptor.plugin( arg ) ).isSameAs( arg );
    }

    @Test
    public void testGetInstrumentation_returnsNonNullInstance() throws Exception {
        BasicInstrumentation instrumentation = interceptor.getInstrumentation( invocation );

        assertThat( instrumentation ).isNotNull();
    }

    @Test
    public void testGetRegistry_returnsRegistryFromConstructor() throws Exception {
        assertThat( interceptor.getRegistry() ).isSameAs( metricRegistry );
    }

    @Test
    public void testDeriveMetricName() throws Exception {
        String actualName = interceptor.deriveMetricName( invocation );

        assertEquals( "com.tguzik.metrics.mybatis.InstrumentingInterceptorTest$FakeMapper#doSomething", actualName );
    }

    public static class FakeMapper {
        public Object doSomething( String arg1, int arg2, int arg3 ) {
            return 0;
        }
    }
}
