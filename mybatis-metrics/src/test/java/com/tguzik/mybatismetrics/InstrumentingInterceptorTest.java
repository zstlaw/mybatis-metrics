package com.tguzik.mybatismetrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.codahale.metrics.MetricRegistry;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.junit.Before;
import org.junit.Test;

public class InstrumentingInterceptorTest {
    private InstrumentingInterceptor interceptor;
    private MetricRegistry metricRegistry;

    private MappedStatement fakeStatement;
    private Invocation invocation;
    private Executor fakeExecutor;

    @Before
    public void setUp() throws Exception {
        this.metricRegistry = new MetricRegistry();
        this.interceptor = new InstrumentingInterceptor( metricRegistry );
        this.fakeExecutor = mock( Executor.class );
        this.fakeStatement = new MappedStatement.Builder( mock( Configuration.class ),
                                                          "statement id",
                                                          mock( SqlSource.class ),
                                                          SqlCommandType.SELECT ).lang( mock( LanguageDriver.class ) )
                                                                                 .build();
        this.invocation = new Invocation( fakeExecutor,
                                          Executor.class.getDeclaredMethod( "query",
                                                                            MappedStatement.class,
                                                                            Object.class,
                                                                            RowBounds.class,
                                                                            ResultHandler.class,
                                                                            CacheKey.class,
                                                                            BoundSql.class ),
                                          new Object[] { fakeStatement, null, null, null, null, null } );
    }

    @Test
    public void testIntercept_marksMetrics() throws Throwable {
        // Perform the test
        interceptor.intercept( invocation );

        // Verify that following metrics were created
        assertThat( metricRegistry.getCounters() ).containsKeys( "statement id.totalInvocations",
                                                                 "statement id.totalFailures" );
        assertThat( metricRegistry.getMeters() ).containsKeys( "statement id.invocationsPerSecond",
                                                               "statement id.failuresPerSecond" );
        assertThat( metricRegistry.getTimers() ).containsKey( "statement id.elapsed" );

        // Validate that metrics were incremented
        assertThat( metricRegistry.counter( "statement id.totalInvocations" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.counter( "statement id.totalFailures" ).getCount() ).isZero();
        assertThat( metricRegistry.meter( "statement id.invocationsPerSecond" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.meter( "statement id.failuresPerSecond" ).getCount() ).isZero();
        assertThat( metricRegistry.timer( "statement id.elapsed" ).getCount() ).isEqualTo( 1L );
    }

    @Test
    public void testIntercept_marksMetrics_exceptionFromMapper() throws Throwable {
        doThrow( new RuntimeException( "custom exception" ) ).when( fakeExecutor )
                                                             .query( any( MappedStatement.class ),
                                                                     any(),
                                                                     any( RowBounds.class ),
                                                                     any( ResultHandler.class ),
                                                                     any( CacheKey.class ),
                                                                     any( BoundSql.class ) );

        // Perform the test
        try {
            interceptor.intercept( invocation );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // Discard the exception. These are verified in a separate test
        }

        // Verify that following metrics were created
        assertThat( metricRegistry.getCounters() ).containsKeys( "statement id.totalInvocations",
                                                                 "statement id.totalFailures" );
        assertThat( metricRegistry.getMeters() ).containsKeys( "statement id.invocationsPerSecond",
                                                               "statement id.failuresPerSecond" );
        assertThat( metricRegistry.getTimers() ).containsKey( "statement id.elapsed" );

        // Validate that metrics were incremented
        assertThat( metricRegistry.counter( "statement id.totalInvocations" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.counter( "statement id.totalFailures" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.meter( "statement id.invocationsPerSecond" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.meter( "statement id.failuresPerSecond" ).getCount() ).isEqualTo( 1L );
        assertThat( metricRegistry.timer( "statement id.elapsed" ).getCount() ).isEqualTo( 1L );
    }

    @Test
    public void testIntercept_returnsValueFromTheMapperUnmodified() throws Throwable {
        Object expectedValue = mock( List.class );
        doReturn( expectedValue ).when( fakeExecutor )
                                 .query( any( MappedStatement.class ),
                                         any(),
                                         any( RowBounds.class ),
                                         any( ResultHandler.class ),
                                         any( CacheKey.class ),
                                         any( BoundSql.class ) );

        Object actualValue = interceptor.intercept( invocation );

        assertThat( actualValue ).isSameAs( expectedValue );
    }

    @Test
    public void testIntercept_rethrowsExceptionUnmodified() throws Throwable {
        Throwable expcetedException = new RuntimeException( "whatever it throws" );
        doThrow( expcetedException ).when( fakeExecutor )
                                    .query( any( MappedStatement.class ),
                                            any(),
                                            any( RowBounds.class ),
                                            any( ResultHandler.class ),
                                            any( CacheKey.class ),
                                            any( BoundSql.class ) );

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
    public void testDeriveMetricName_returnsMappedStatementId() throws Exception {
        assertThat( interceptor.deriveMetricName( invocation ) ).isEqualTo( "statement id" );
    }

    @Test
    public void testDeriveMetricName_returnsMappedStatementId_evenIfTargetIsNotAnExecutor() throws Exception {
        this.invocation = new Invocation( mock( Object.class ),
                                          Object.class.getDeclaredMethod( "equals", Object.class ),
                                          new Object[] { fakeStatement } );

        assertThat( interceptor.deriveMetricName( invocation ) ).isEqualTo( "statement id" );
    }

    @Test
    public void testDeriveMetricName_returnsInvalidInvocation_firstArgumentIsNotMappedStatement() throws Exception {
        this.invocation = new Invocation( fakeExecutor,
                                          Executor.class.getDeclaredMethod( "query",
                                                                            MappedStatement.class,
                                                                            Object.class,
                                                                            RowBounds.class,
                                                                            ResultHandler.class,
                                                                            CacheKey.class,
                                                                            BoundSql.class ),
                                          new Object[] { "not a mapped statement", null, null, null, null, null } );

        assertThat( interceptor.deriveMetricName( invocation ) ).isEqualTo( "mybatis-metrics.invocations.invalid" );
    }

    @Test
    public void testFirstArgumentIsMappedStatement() {
        assertThat( interceptor.firstArgumentIsMappedStatement( invocation ) ).isTrue();
    }

    @Test
    public void testFirstArgumentIsMappedStatement_targetIsNotAnExecutor() throws NoSuchMethodException {
        this.invocation = new Invocation( mock( Object.class ),
                                          Object.class.getDeclaredMethod( "equals", Object.class ),
                                          new Object[] { fakeStatement } );

        assertThat( interceptor.firstArgumentIsMappedStatement( invocation ) ).isTrue();
    }

    @Test
    public void testFirstArgumentIsMappedStatement_firstArgumentNotMappedStatement() throws NoSuchMethodException {
        this.invocation = new Invocation( mock( Object.class ),
                                          Object.class.getDeclaredMethod( "equals", Object.class ),
                                          new Object[] { "not a mapped statement" } );

        assertThat( interceptor.firstArgumentIsMappedStatement( invocation ) ).isFalse();
    }
}
