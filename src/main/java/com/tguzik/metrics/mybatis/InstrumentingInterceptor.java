package com.tguzik.metrics.mybatis;

import java.util.Properties;

import javax.inject.Inject;

import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Invocation;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.tguzik.metrics.BasicInstrumentation;

/**
 * Interceptor for mapper invocations that saves basic instrumentation data to
 * provided {@link MetricRegistry}. At this time there's only one pattern for
 * mapper method metric names - each metric is constructed using package, class
 * name and specific metric name.
 *
 * As for the class usage, this interceptor has to be registered with MyBatis
 * (please refer to main README for this project for examples) *and* has to have
 * access to {@link MetricRegistry}. The registry can be injected using Guice or
 * any other IoC/DI container.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class InstrumentingInterceptor implements Interceptor
{
    private final LoadingCache<Invocation, BasicInstrumentation> metricSets;

    @Inject
    public InstrumentingInterceptor( MetricRegistry metricRegistry ) {
        this.metricSets = CacheBuilder.newBuilder()
                .maximumSize( 64 )
                .build( new InstrumentationMetricSetLoader( metricRegistry ) );
    }

    @Override
    public Object intercept( Invocation invocation ) throws Throwable {
        BasicInstrumentation instrumentation = metricSets.get( invocation );
        Timer.Context ctx = instrumentation.openTimerContext();
        instrumentation.markInvoked();

        try {
            return invocation.proceed();
        }
        catch ( Throwable e ) {
            instrumentation.markFailed();
            throw e;
        }
        finally {
            ctx.close();
        }
    }

    @Override
    public Object plugin( Object target ) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setProperties( Properties properties ) {
        // TODO Auto-generated method stub

    }

}
