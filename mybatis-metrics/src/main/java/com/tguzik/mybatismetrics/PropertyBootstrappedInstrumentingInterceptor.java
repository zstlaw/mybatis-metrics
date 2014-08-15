package com.tguzik.mybatismetrics;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import com.codahale.metrics.MetricRegistry;
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
 * Acts as a wrapper/decorator on {@link com.tguzik.mybatismetrics.InstrumentingInterceptor} in order to bootstrap
 * the interceptor with an instance of {@link com.codahale.metrics.MetricRegistry} from a {@link javax.inject.Provider}
 * that is taken from either:
 * <ul>
 * <li>System property with key <code>metrics.registry.provider</code></li>
 * <li>MyBatis plugin property with key <code>metrics.registry.provider</code></li>
 * </ul>
 * <p/>
 * If both lookups fail, this class will pass on the invocations to MyBatis mappers while loudly complaining through
 * SLF4j logger's WARN messages.
 * <p/>
 * <p/>
 * <p/>
 * TODO: Rewrite this incoherent rant below.
 * <p/>
 * So here's the thing. Superclass works great when it is instanitated either manually or when instanitated by a
 * dependency injection framework, such as guice. The problem starts when we have to do bootstrap 'the traditional
 * mybatis way', with bunch of XML files and nothing else. In that situation we don't really know how to get the
 * {@link com.codahale.metrics.MetricRegistry} to count for all these fancy numbers.
 * <p/>
 * But fear not! MyBatis has this mechanism to pass some properties to the plugins, so we can define a provider there.
 * Now, the problem is that these properties are passed *AFTER* the object is created, which means that if we depend on
 * basic functionality from properties, *we are not threadsafe*!
 * <p/>
 * To combat that we have to do some real sketchy stuff: we need to  override the {@link
 * InstrumentingInterceptor#getRegistry()} method and make it return the data from an instance of {@link
 * java.util.concurrent.atomic.AtomicReference}.
 * <p/>
 * Since this sucks majorly, we'll also try to look up a system property (with same name as the property in mybatis
 * configuration) and try to get the registry from there - if we succeed then we don't have to deal with the
 * {@linkplain java.util.concurrent.atomic.AtomicReference} BS.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */

@RefactorThis( "Rename this class to something less hideous" )

@Intercepts( { //
        /* as far as I know there's no way to set it up to 'intercept everything' in one line :( */
        @Signature(
                type = Executor.class,
                method = "update",
                args = { MappedStatement.class, Object.class } ), //
        @Signature( type = Executor.class,
                    method = "query",
                    args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class,
                             BoundSql.class } ), //
        @Signature( type = Executor.class,
                    method = "query",
                    args = { MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class } ) //
             } )
public class PropertyBootstrappedInstrumentingInterceptor implements Interceptor {
    private static final Logger LOGGER = LoggerFactory.getLogger( PropertyBootstrappedInstrumentingInterceptor.class );
    private static final String PROPERTY_NAME = "metrics.registry.provider";

    private final AtomicReference<Interceptor> interceptorReference;

    public PropertyBootstrappedInstrumentingInterceptor() {
        this.interceptorReference = new AtomicReference<>();
        tryToInstanitateProvider( "System property", System.getProperty( PROPERTY_NAME ) );
    }

    @Override
    @ExpectedPerformanceProfile( path = ExpectedPerformanceProfile.Path.HOT )
    public Object intercept( Invocation invocation ) throws Throwable {
        // Get the reference
        Interceptor interceptor = interceptorReference.get();

        if ( interceptor == null ) {
            // Complain loudly, but in the end do whatever the invocation intended to do.
            LOGGER.warn( "InstrumentingInterceptor is not initialized - no metrics are collected! Check whether you " +
                         "have correctly set the '" + PROPERTY_NAME + "' system or MyBatis plugin property." );
            return invocation.proceed();
        }

        // All is well. Let it do whatever it does.
        return interceptor.intercept( invocation );
    }

    @Override
    @Nullable
    public Object plugin( @Nullable Object target ) {
        LOGGER.trace( "{}#plugin(): ", getClass().getSimpleName(), target.getClass().getName() );
        return Plugin.wrap( target, this );
    }

    @Override
    @RefactorThis( "This method has a subtle race condition. While it's a low priority item, " +
                   "it should be removed eventually." )
    public void setProperties( @Nullable Properties properties ) {
        LOGGER.trace( "setProperties(): {}", properties );

        if ( properties == null ) {
            return;
        }

        tryToInstanitateProvider( "MyBatis plugin property", properties.getProperty( PROPERTY_NAME ) );
        Interceptor interceptor = interceptorReference.get();

        if ( interceptor == null ) {
            LOGGER.warn( "The interceptor was not initialized! No metrics will be collected! Make sure you've " +
                         "set up either system property or MyBatis plugin property with MetricRegistry provider!" );
            return;
        }

        // Pass the properties to constructed instance - it may read something from these
        interceptor.setProperties( properties );
    }

    private void tryToInstanitateProvider( @Nonnull String propertySource, @Nullable String providerName ) {
        if ( providerName == null || providerName.trim().isEmpty() ) {
            LOGGER.debug( "{} '{}' was null or empty.", propertySource, PROPERTY_NAME );
            return;
        }

        LOGGER.debug( "Attempting to instanitate javax.inject.Provider<MetricRegistry> from class '{}'...",
                      providerName );

        try {
            Class<Provider<MetricRegistry>> clazz = (Class<Provider<MetricRegistry>>) Class.forName( providerName );
            initializeInterceptor( clazz.newInstance() );
        }
        catch ( ClassNotFoundException | InstantiationException | IllegalAccessException e ) {
            LOGGER.error( "Unable to instanitate MetricRegistry provider: {}", e.getMessage(), e );
        }
    }

    private void initializeInterceptor( @Nonnull Provider<MetricRegistry> provider ) {
        // Do we need to do anything?
        if ( interceptorReference.get() != null ) {
            // No, we don't.
            return;
        }

        /* The instances of InstrumentingInterceptor are pretty lightweight and since constructing another instance
         * does not bring any side effects, we can just discard the old instance, if there was any.
         */
        interceptorReference.set( new InstrumentingInterceptor( provider.get() ) );
        LOGGER.info( "Interceptor initialized." );
    }
}
