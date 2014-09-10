package com.tguzik.metrics.mybatis.integrationtests.guice;

import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateFailingOperation;
import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateSuccessfulOperation;
import static org.junit.Assert.fail;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.codahale.metrics.MetricRegistry;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.tguzik.metrics.mybatis.InstrumentingInterceptor;
import com.tguzik.metrics.mybatis.integrationtests.IntegrationTestBlueprint;
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
    private DataSource dataSource;
    private Injector injector;
    private FakeMapper fakeMapper;

    @Before
    public void setUp() throws SQLException, IOException {
        this.metricRegistry = new MetricRegistry();
        this.dataSource = bootstrapInMemoryDatabase();
        this.injector = bootstrapGuice( metricRegistry, dataSource );
        this.fakeMapper = injector.getInstance( FakeMapper.class );
    }

    @After
    public void tearDown() throws SQLException {
        try ( Connection connection = dataSource.getConnection() ) {
            connection.prepareStatement( "drop table test" ).executeUpdate();
        }
    }

    private Injector bootstrapGuice( MetricRegistry metricRegistry, DataSource dataSource ) {
        return Guice.createInjector( new IntegrationTestLocalModule( metricRegistry ),
                                     new MyBatisIntegrationModule( dataSource ) );
    }

    /** Create in-memory database table so that mapper methods might operate on something */
    private JDBCDataSource bootstrapInMemoryDatabase() throws SQLException {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl( "jdbc:hsqldb:mem:guice-integration-test" );
        dataSource.setUser( "sa" );
        dataSource.setPassword( "" );

        try ( Connection connection = dataSource.getConnection() ) {
            connection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
        }

        return dataSource;
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
        fakeMapper.doSelect( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doSelect";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_select_failure() {
        // Perform action
        try {
            fakeMapper.doFailingSelect( "any", 123, "arguments" );
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
        fakeMapper.doUpdate( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doUpdate";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_update_failure() {
        // Perform action
        try {
            fakeMapper.doFailingUpdate( "any", 123, "arguments" );
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
        fakeMapper.doInsert( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doInsert";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_insert_failure() {
        // Perform action
        try {
            fakeMapper.doFailingInsert( "any", 123, "arguments" );
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
        fakeMapper.doDelete( "any", 123, "arguments" );

        // Validate
        String baseMetricName = FakeMapper.class.getCanonicalName() + ".doDelete";
        validateSuccessfulOperation( this.metricRegistry, baseMetricName );
    }

    @Test
    @Override
    public void testMapperOperation_delete_failure() {
        // Perform action
        try {
            fakeMapper.doFailingDelete( "any", 123, "arguments" );
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
