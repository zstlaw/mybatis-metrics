package com.tguzik.metrics.mybatis;

import javax.annotation.Nonnull;

import com.codahale.metrics.MetricRegistry;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tguzik.metrics.BasicInstrumentation;
import org.apache.ibatis.plugin.Invocation;

/**
 * Loader/creator of MetricSet instances to be used in {@link LoadingCache}
 * inside {@link InstrumentingInterceptor}.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class InstrumentationMetricSetLoader extends CacheLoader<Invocation, BasicInstrumentation> {
    private final MetricRegistry registry;

    public InstrumentationMetricSetLoader( MetricRegistry metricRegistry ) {
        this.registry = metricRegistry;
    }

    @Nonnull
    @Override
    public BasicInstrumentation load( @Nonnull Invocation key ) throws Exception {
        return new BasicInstrumentation( registry, deriveBaseName( key ) );
    }

    @Nonnull
    String deriveBaseName( @Nonnull Invocation invocation ) {
        String containingClass = invocation.getMethod().getDeclaringClass().getCanonicalName();
        String methodName = invocation.getMethod().getName();

        return String.format( "%s#%s", containingClass, methodName );
    }
}
