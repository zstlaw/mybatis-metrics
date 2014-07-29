package com.tguzik.metrics.mybatis;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Properties;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.tguzik.annotations.RefactorThis;
import com.tguzik.metrics.BasicInstrumentation;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;

/**
 * Interceptor for mapper invocations that saves basic instrumentation data to
 * provided {@link MetricRegistry}. At this time there's only one pattern for
 * mapper method metric names - each metric is constructed using package, class
 * name and specific metric name.
 * <p/>
 * As for the class usage, this interceptor has to be registered with MyBatis
 * (please refer to main README for this project for examples) *and* has to have
 * access to {@link MetricRegistry}. The registry can be injected using Guice or
 * any other IoC/DI container.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class InstrumentingInterceptor implements Interceptor {
    private final LoadingCache<Invocation, BasicInstrumentation> metricSets;

    @Inject
    public InstrumentingInterceptor( @Nonnull MetricRegistry metricRegistry ) {
        this.metricSets = createInternalCache( Preconditions.checkNotNull( metricRegistry ) );
    }

    @Override
    public Object intercept( Invocation invocation ) throws Throwable {
        BasicInstrumentation instrumentation = metricSets.get( invocation );

        /* Note that the Timer.Context will double-count the time taken if it is closed twice,
         * or stopped and then closed.
         */
        try ( Timer.Context ctx = instrumentation.openTimerContext() ) {
            instrumentation.markInvoked();
            return invocation.proceed();
        }
        catch ( Throwable e ) {
            instrumentation.markFailed();
            throw e;
        }
    }

    @Override
    @RefactorThis( "This method is not documented very well. Find out what it's supposed to do and implement that" )
    public Object plugin( Object target ) {
        return null;
    }

    @Override
    @RefactorThis( "This method is not documented very well. Find out what it's supposed to do and implement that" )
    public void setProperties( Properties properties ) {

    }

    protected LoadingCache<Invocation, BasicInstrumentation> createInternalCache(
            @Nonnull MetricRegistry metricRegistry ) {
        return CacheBuilder.newBuilder()
                           .maximumSize( 64 )
                           .build( new InstrumentationMetricSetLoader( metricRegistry ) );
    }
}
