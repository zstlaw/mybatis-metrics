package com.tguzik.mybatismetrics.integrationtests.xmlfiles;

import static com.tguzik.mybatismetrics.integrationtests.IntegrationTestVerificationUtil.validateFailingOperation;
import static com.tguzik.mybatismetrics.integrationtests.IntegrationTestVerificationUtil.validateSuccessfulOperation;
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
import com.tguzik.mybatismetrics.integrationtests.IntegrationTestBlueprint;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test where we bootstrap MyBatis using XML files and perform operations on mappers.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class XmlFilesIntegrationTest implements IntegrationTestBlueprint {
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

    /** Create in-memory database table so that mapper methods might operate on something */
    private Connection bootstrapInMemoryDatabase() throws SQLException {
        Connection connection = DriverManager.getConnection( "jdbc:hsqldb:mem:xmlfiles-integration-test", "sa", "" );
        connection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
        return connection;
    }

    @After
    public void tearDownInMemoryDatabase() throws SQLException {
        this.databaseConnection.prepareStatement( "drop table test;" ).execute();
        this.databaseConnection.close();
    }

    private SqlSessionFactory bootstrapMyBatis() throws IOException {
        Path configurationPath = createMainConfigurationPath( "mybatis-configuration.xml" );

        try ( InputStream configurationStream = Resources.getResourceAsStream( configurationPath.toString() ) ) {
            return new SqlSessionFactoryBuilder().build( configurationStream );
        }
    }

    @Test
    @Override
    public void testMyBatisConfiguration_containsInstanceOfInterceptor() {
        for ( Interceptor interceptor : this.sqlSessionFactory.getConfiguration().getInterceptors() ) {
            if ( interceptor instanceof PropertyBootstrappedInstrumentingInterceptor ) {
                return;
            }
        }

        fail( "Expected the list of all interceptors to contain an instance of " +
              PropertyBootstrappedInstrumentingInterceptor.class.getSimpleName() );
    }

    @Test
    @Override
    public void testMapperOperation_select_success() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doSelect( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doSelect";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_select_failure() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doFailingSelect( "any", 123, "arguments" );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // we don't care about this particular exception. discard it.
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doFailingSelect";
        validateFailingOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_update_success() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doUpdate( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doUpdate";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_update_failure() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doFailingUpdate( "any", 123, "arguments" );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // we don't care about this particular exception. discard it.
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doFailingUpdate";
        validateFailingOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_insert_success() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doInsert( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doInsert";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_insert_failure() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doFailingInsert( "any", 123, "arguments" );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // we don't care about this particular exception. discard it.
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doFailingInsert";
        validateFailingOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_delete_success() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doDelete( "any", 123, "arguments" );
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doDelete";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_delete_failure() {
        // Perform action
        try ( SqlSession session = this.sqlSessionFactory.openSession() ) {
            session.getMapper( FakeMapper.class ).doFailingDelete( "any", 123, "arguments" );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // we don't care about this particular exception. discard it.
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doFailingDelete";
        validateFailingOperation( this.metricRegistry, baseMetricName );
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
