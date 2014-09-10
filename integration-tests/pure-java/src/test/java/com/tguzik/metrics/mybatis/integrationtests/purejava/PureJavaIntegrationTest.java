package com.tguzik.metrics.mybatis.integrationtests.purejava;

import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateFailingOperation;
import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateSuccessfulOperation;
import static org.assertj.core.api.Assertions.fail;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import com.codahale.metrics.MetricRegistry;
import com.tguzik.metrics.mybatis.InstrumentingInterceptor;
import com.tguzik.metrics.mybatis.integrationtests.IntegrationTestBlueprint;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test where we bootstrap MyBatis without using XML files.
 *
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class PureJavaIntegrationTest implements IntegrationTestBlueprint {
    private SqlSessionFactory sqlSessionFactory;
    private MetricRegistry metricRegistry;
    private JDBCDataSource dataSource;

    @Before
    public void setUp() throws SQLException, IOException {
        this.metricRegistry = new MetricRegistry();
        this.dataSource = bootstrapInMemoryDatabase();
        this.sqlSessionFactory = bootstrapMyBatis( dataSource, new InstrumentingInterceptor( metricRegistry ) );
    }

    /** Create in-memory database table so that mapper methods might operate on something */
    private JDBCDataSource bootstrapInMemoryDatabase() throws SQLException {
        JDBCDataSource dataSource = new JDBCDataSource();
        dataSource.setUrl( "jdbc:hsqldb:mem:purejava-integration-test" );
        dataSource.setUser( "sa" );
        dataSource.setPassword( "" );

        try ( Connection connection = dataSource.getConnection() ) {
            connection.prepareStatement( "create table test (key varchar(20), value varchar(20))" ).execute();
        }

        return dataSource;
    }

    private SqlSessionFactory bootstrapMyBatis( DataSource dataSource, InstrumentingInterceptor interceptor ) {
        Environment environment = new Environment( "integration test", new JdbcTransactionFactory(), dataSource );
        Configuration myBatisConfig = new Configuration( environment );
        myBatisConfig.addMapper( FakeMapper.class );
        myBatisConfig.setEnvironment( environment );
        myBatisConfig.addInterceptor( interceptor );

        return new SqlSessionFactoryBuilder().build( myBatisConfig );
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
        for ( Interceptor interceptor : this.sqlSessionFactory.getConfiguration().getInterceptors() ) {
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
}
