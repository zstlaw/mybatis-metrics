package com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap;

import static org.assertj.core.api.Assertions.fail;

import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import com.codahale.metrics.MetricRegistry;
import com.tguzik.mybatismetrics.PropertyBootstrappedInstrumentingInterceptor;
import com.tguzik.mybatismetrics.integrationtests.BaseFunctionalIntegrationTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.log4j.BasicConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Yes, there are multiple @Before methods. Yes, it's for clarity. No, they are not executed in any specific order.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class FilebasedBootstrapIntegrationTest extends BaseFunctionalIntegrationTest {
    private SqlSessionFactory sqlSessionFactory;
    private MetricRegistry metricRegistry;

    /**
     * This field is should not be used - it is here so we don't immediately close the connection to the in-memory
     * database and not lose our prepared tables.
     */
    private Connection databaseConnection;

    @Before
    public void setUp() throws SQLException, IOException {
        this.metricRegistry = setUpFreshMetricRegistry();
        this.databaseConnection = bootstrapInMemoryDatabase();
        this.sqlSessionFactory = bootstrapMyBatis();
    }

    private MetricRegistry setUpFreshMetricRegistry() {
        MetricRegistry registry = new MetricRegistry();
        MetricRegistryProvider.set( registry );
        return registry;
    }

    private Connection bootstrapInMemoryDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection( "jdbc:hsqldb:mem:file-based-integration-test", "sa", "" );
        connection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
        return connection;
    }

    @After
    public void tearDownInMemoryDatabase() throws SQLException {
        if ( databaseConnection == null ) {
            return;
        }

        this.databaseConnection.prepareStatement( "drop table test;" ).execute();
        this.databaseConnection.close();
    }

    private SqlSessionFactory bootstrapMyBatis() throws IOException {
        System.out.println( "cwd: " + Paths.get( "." ).toAbsolutePath().toString() );
        Path configurationPath = createMainConfigurationPath( "mybatis-configuration.xml" );

        try ( InputStream configurationStream = Resources.getResourceAsStream( configurationPath.toString() ) ) {
            return new SqlSessionFactoryBuilder().build( configurationStream );
        }
    }

    @Test
    public void testMyBatisConfiguration_containsInstanceOfPropertyBootstrappedInstrumentingInterceptor() {
        boolean containsExpectedInstance = false;

        for ( Interceptor interceptor : this.sqlSessionFactory.getConfiguration().getInterceptors() ) {
            if ( interceptor instanceof PropertyBootstrappedInstrumentingInterceptor ) {
                containsExpectedInstance = true;
            }
        }

        if ( !containsExpectedInstance ) {
            fail( "Expected the list of all interceptors to contain an instance of " +
                  PropertyBootstrappedInstrumentingInterceptor.class.getSimpleName() );
        }
    }

    @Test
    public void testMapperOperation_select_updatesMetricRegistry() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            FakeMapper mapper = session.getMapper( FakeMapper.class );
            mapper.doSelect( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = "com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap.FakeMapper.doSelect";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    public void testMapperOperation_update_updatesMetricRegistry() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            FakeMapper mapper = session.getMapper( FakeMapper.class );
            mapper.doUpdate( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = "com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap.FakeMapper.doUpdate";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    public void testMapperOperation_insert_updatesMetricRegistry() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            FakeMapper mapper = session.getMapper( FakeMapper.class );
            mapper.doInsert( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = "com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap.FakeMapper.doInsert";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    public void testMapperOperation_delete_updatesMetricRegistry() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            FakeMapper mapper = session.getMapper( FakeMapper.class );
            mapper.doDelete( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = "com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap.FakeMapper.doDelete";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    private Path createMainConfigurationPath( String mainConfigurationFileName ) {
        return Paths.get( getClass().getPackage().getName().replaceAll( "\\.", "/" ), mainConfigurationFileName );
    }

    public static class MetricRegistryProvider implements Provider<MetricRegistry> {
        private static final AtomicReference<MetricRegistry> REGISTRY = new AtomicReference<>();

        @Override
        public MetricRegistry get() {
            return REGISTRY.get();
        }

        public static void set( MetricRegistry registry ) {
            REGISTRY.set( registry );
        }
    }
}
