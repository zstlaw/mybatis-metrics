package com.tguzik.mybatismetrics;

import static com.tguzik.annotations.ExpectedPerformanceProfile.Path;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Properties;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.tguzik.annotations.ExpectedPerformanceProfile;
import com.tguzik.annotations.RefactorThis;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

/* as far as I know there's no way to set it up to 'intercept everything' in one line :( */
@Intercepts( { //
               @Signature(
                       type = Executor.class,
                       method = "update",
                       args = { MappedStatement.class, Object.class } ), //
               @Signature( type = Executor.class,
                           method = "query",
                           args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class,
                                    CacheKey.class, BoundSql.class } ), //
               @Signature( type = Executor.class,
                           method = "query",
                           args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class } ) //
             } )
@RefactorThis( "Class turned out to be semi-ugly. Refactor it to be prettier." )
public class InstrumentingInterceptor implements Interceptor {
    protected static final String INVALID_INVOCATION_METRIC_NAME = "mybatis-metrics.invocations.invalid";

    /** Metric registry to be used to store all metrics. */
    private final MetricRegistry metricRegistry;

    /** Logger matching current class (pointing to child class if this class is extended). */
    private final Logger logger;

    @Inject
    public InstrumentingInterceptor( @Nonnull MetricRegistry metricRegistry ) {
        this.logger = LoggerFactory.getLogger( getClass() );
        this.metricRegistry = metricRegistry;
    }

    @Override
    @ExpectedPerformanceProfile( path = Path.HOT )
    public Object intercept( @Nonnull Invocation invocation ) throws Throwable {
        if ( logger.isDebugEnabled() ) {
            logger.debug( "Intercepting invocation for {}#{}...",
                          invocation.getTarget().getClass().getCanonicalName(),
                          invocation.getMethod().getName() );
        }

        BasicInstrumentation instrumentation = getInstrumentation( invocation );

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

    /**
     * This method appears to be used to pass several different types of objects, depending on the context. I think
     * that the original intent was to allow plugins to enhance (or 'plug into') these objects. It also appears that
     * there is an expectation to return the enhanced object.
     * <p/>
     * This class doesn't need to do that, so it just returns what it was given.
     */
    @Override
    @ExpectedPerformanceProfile( path = Path.COLD )
    public Object plugin( Object target ) {
        logger.trace( "{}#plugin(): ", getClass().getSimpleName(), target.getClass().getName() );
        return Plugin.wrap( target, this );
    }

    /**
     * Used to pass any configuration from the {@link org.apache.ibatis.builder.xml.XMLConfigBuilder#pluginElement
     * (org.apache.ibatis.parsing.XNode)}
     * to the instance. Appears to be called only once, right after instanitation (this may change in future
     * versions, of course).
     * <p/>
     * Since this plugin does not need any configuration (yet), invoking this method has no effect.
     */
    @Override
    @ExpectedPerformanceProfile( path = Path.COLD )
    public void setProperties( Properties properties ) {
        logger.trace( "{}#setProperties( {} )", getClass().getSimpleName(), properties );
    }

    /**
     * Retrieves or creates an instance of {@link BasicInstrumentation} to be used. Code on hot
     * path.
     */
    @Nonnull
    @ExpectedPerformanceProfile( path = Path.HOT )
    protected BasicInstrumentation getInstrumentation( @Nonnull Invocation invocation ) {
        /* So here's the deal, class for Invocation does not override .hashCode() method, which means
         * that if we wanted to create a cache for instances of BasicInstrumentation (one instance
         * of BasicInstrumentation per one mapper method), we would have to calculate its identity
         * on each invocation of this method. While doing so on just hash codes of the method (part
         * of Invocation instance) and its containing class, it would be also beneficial to go through
         * method arguments... which all in all is quite a bit of work just to reuse instances of
         * BasicInstrumentation that frankly I'd like to avoid (it's messy and it's not our ultimate
         * objective). While this might change once this code is profiled, for now let's just
         * create new instance of BasicInstrumentation and use it as if it came from cache.
         *
         * Basically using cache vs. creating instances would come down to what is faster. However,
         * considering where this library is right now, let's just do what is simpler.
         */

        return new BasicInstrumentation( getRegistry(), deriveMetricName( invocation ) );
    }

    /** Returns instance of MetricRegistry for child classes. */
    protected MetricRegistry getRegistry() {
        return metricRegistry;
    }

    /**
     * Used to determine metric names. Override this method if you want to implement a custom name template
     * <p/>
     * When we get instance of Invocation, the value in {@link Invocation#target} is an implementation of
     * {@link org.apache.ibatis.executor.Executor}, it's {@link Invocation#method} is one of {@link Executor#query}
     * methods and finally, the {@link Invocation#args} array contains parameters <i>not</i> for the original MyBatis
     * Mapper that issued the call, <i>but instead</i> contains the parameters of the
     * {@link org.apache.ibatis.executor.Executor#query} method. While this is logical from framework
     * designer's perspective, it may not be your first guess. Lack of type safety only exacerbates this fact.
     * <p/>
     * This can, of course, change when this class (or a decorator to this class) changes the contents of the class
     * annotation {@link org.apache.ibatis.plugin.Intercepts}.
     * <p/>
     * Remember that we're basically using a private API that can change at any time for any reason!.
     */
    protected String deriveMetricName( Invocation invocation ) {
        /* ...and here we have some black magic that we love MyBatis for... */
        if ( firstArgumentIsMappedStatement( invocation ) ) {
            MappedStatement statement = (MappedStatement) invocation.getArgs()[ 0 ];
            return statement.getId();
        }

        /* Well, we're fsck'd. We can't make any assumptions on the argument names, so we don't know what is
         * the specific name for this query We could throw an exception, but that would also screw over the end
         * user. For now let's just complain loudly and use a BS metric name. We might want to revisit this part
         * in the future
         */
        logger.warn( "Received invocation for unknown or invalid target. Did MyBatis implementation change? I'll " +
                     "use metric name '" + INVALID_INVOCATION_METRIC_NAME + "' instead." );
        return INVALID_INVOCATION_METRIC_NAME;
    }

    protected boolean firstArgumentIsMappedStatement( Invocation invocation ) {
        return invocation != null && //
               invocation.getArgs() != null && //
               invocation.getArgs()[ 0 ] instanceof MappedStatement;
    }
}
