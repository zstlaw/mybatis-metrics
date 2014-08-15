package com.tguzik.mybatismetrics.integrationtests.guice;

import static com.tguzik.mybatismetrics.integrationtests.IntegrationTestVerificationUtil.validateFailingOperation;
import static com.tguzik.mybatismetrics.integrationtests.IntegrationTestVerificationUtil.validateSuccessfulOperation;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tguzik.mybatismetrics.InstrumentingInterceptor;
import com.tguzik.mybatismetrics.integrationtests.IntegrationTestBlueprint;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test where we bootstrap MyBatis using mybatis-guice.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class GuiceIntegrationTest implements IntegrationTestBlueprint {
    private MetricRegistry metricRegistry;
    private JDBCDataSource dataSource;
    private Injector injector;

    @Before
    public void setUp() throws SQLException, IOException {
        this.metricRegistry = new MetricRegistry();
        this.dataSource = bootstrapInMemoryDatabase();
        this.injector = bootstrapGuice( metricRegistry, dataSource );
    }

    private Injector bootstrapGuice( MetricRegistry metricRegistry, JDBCDataSource dataSource ) {
        return Guice.createInjector( new IntegrationTestLocalModule( metricRegistry ),
                                     new MyBatisIntegrationModule( dataSource ) );
    }

    /** Create in-memory database table so that mapper methods might operate on something */
    private JDBCDataSource bootstrapInMemoryDatabase() throws SQLException {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl( "jdbc:hsqldb:mem:fileless-integration-test" );
        dataSource.setUser( "sa" );
        dataSource.setPassword( "" );

        try ( Connection connection = dataSource.getConnection() ) {
            connection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
        }

        return dataSource;
    }

    @After
    public void tearDown() throws SQLException {
        try ( Connection connection = dataSource.getConnection() ) {
            connection.prepareStatement( "drop table test" ).executeUpdate();
        }
    }

    @Test
    @Override
    public void testMyBatisConfiguration_containsInstanceOfInterceptor() {
        SqlSessionFactory sqlSessionFactory = injector.getInstance( SqlSessionFactory.class );

        for ( Interceptor interceptor : sqlSessionFactory.getConfiguration().getInterceptors() ) {
            if ( interceptor instanceof InstrumentingInterceptor ) {
                return;
            }
        }

        fail( "Expected the list of all interceptors to contain an instance of " +
              InstrumentingInterceptor.class.getSimpleName() );
    }

    @Test
    @Override
    public void testMapperOperation_select_success() {
        // Perform action
        injector.getInstance( FakeMapper.class ).doSelect( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doSelect";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_select_failure() {
        // Perform action
        try {
            injector.getInstance( FakeMapper.class ).doFailingSelect( "any", 123, "arguments" );
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
        injector.getInstance( FakeMapper.class ).doUpdate( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doUpdate";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_update_failure() {
        // Perform action
        try {
            injector.getInstance( FakeMapper.class ).doFailingUpdate( "any", 123, "arguments" );
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
        injector.getInstance( FakeMapper.class ).doInsert( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doInsert";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_insert_failure() {
        // Perform action
        try {
            injector.getInstance( FakeMapper.class ).doFailingInsert( "any", 123, "arguments" );
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
        injector.getInstance( FakeMapper.class ).doDelete( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doDelete";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_delete_failure() {
        // Perform action
        try {
            injector.getInstance( FakeMapper.class ).doFailingDelete( "any", 123, "arguments" );
            fail( "Expected exception" );
        }
        catch ( Exception e ) {
            // we don't care about this particular exception. discard it.
        }

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doFailingDelete";
        validateFailingOperation( this.metricRegistry, baseMetricName );
    }
}
