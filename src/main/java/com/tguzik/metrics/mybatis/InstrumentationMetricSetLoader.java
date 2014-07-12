package com.tguzik.metrics.mybatis;

import org.apache.ibatis.plugin.Invocation;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tguzik.metrics.BasicInstrumentation;

/**
 * Loader/creator of MetricSet instances to be used in {@link LoadingCache}
 * inside {@link InstrumentingInterceptor}.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class InstrumentationMetricSetLoader extends CacheLoader<Invocation, BasicInstrumentation>
{
    private final MetricRegistry registry;

    public InstrumentationMetricSetLoader( MetricRegistry metricRegistry ) {
        this.registry = metricRegistry;
    }

    @Override
    public BasicInstrumentation load( Invocation key ) throws Exception {
        return new BasicInstrumentation( registry, deriveBaseName( key ) );
    }

    private String deriveBaseName( Invocation invocation ) {
        String containingClass = invocation.getMethod().getDeclaringClass().getCanonicalName();
        String methodName = invocation.getMethod().getName();

        return String.format( "%s#%s", containingClass, methodName );
    }
}
