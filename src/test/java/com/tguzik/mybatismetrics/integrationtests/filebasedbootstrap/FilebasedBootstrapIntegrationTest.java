package com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap;

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
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Yes, there are multiple @Before methods. Yes, it's for clarity. No, they are not executed in any specific order.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class FilebasedBootstrapIntegrationTest {
    private SqlSessionFactory sqlSessionFactory;
    private MetricRegistry metricRegistry;

    /**
     * This field is should not be used - it is here so we don't immediately close the connection to the in-memory
     * database and not lose our prepared tables.
     */
    private Connection databaseConnection;

    @Before
    public void createEmptyMetricRegistry() {
        this.metricRegistry = new MetricRegistry();
        MetricRegistryProvider.set( this.metricRegistry );
    }

    @Before
    public void bootstrapInMemoryDatabase() throws SQLException {
        this.databaseConnection = DriverManager.getConnection( "jdbc:hsqldb:mem:file-based-integration-test",
                                                               "sa",
                                                               "" );
        this.databaseConnection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
    }

    @After
    public void tearDownInMemoryDatabase() throws SQLException {
        if ( databaseConnection == null ) {
            return;
        }

        this.databaseConnection.prepareStatement( "drop table test;" ).execute();
        this.databaseConnection.close();
    }

    @Before
    public void bootstrapMyBatis() throws IOException {
        System.out.println( "cwd: " + Paths.get( "." ).toAbsolutePath().toString() );
        Path configurationPath = createMainConfigurationPath( "mybatis-configuration.xml" );
        this.sqlSessionFactory = bootstrapSqlSessionFactory( configurationPath );

    }

    @Test
    public void testMyBatisConfiguration_containsInstanceOfInstrumentingInterceptor() {
        //    assertThat( this.sqlSessionFactory.getConfiguration().getInterceptors()
    }

    private SqlSessionFactory bootstrapSqlSessionFactory( Path configurationPath ) throws IOException {
        try ( InputStream configurationStream = Resources.getResourceAsStream( configurationPath.toString() ) ) {
            return new SqlSessionFactoryBuilder().build( configurationStream );
        }
        catch ( IOException e ) {
            // There's no point in continuing
            throw e;
        }
    }

    private Path createMainConfigurationPath( String mainConfigurationFileName ) {
        return Paths.get( getClass().getPackage().getName().replaceAll( "\\.", "/" ),
                          mainConfigurationFileName );
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
