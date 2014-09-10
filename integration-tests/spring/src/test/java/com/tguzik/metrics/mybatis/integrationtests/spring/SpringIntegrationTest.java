package com.tguzik.metrics.mybatis.integrationtests.spring;

import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateFailingOperation;
import static com.tguzik.metrics.mybatis.integrationtests.IntegrationTestVerificationUtil.validateSuccessfulOperation;
import static org.junit.Assert.fail;

import javax.inject.Inject;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.SQLException;

import com.codahale.metrics.MetricRegistry;
import com.tguzik.metrics.mybatis.InstrumentingInterceptor;
import com.tguzik.metrics.mybatis.integrationtests.IntegrationTestBlueprint;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
@RunWith( SpringJUnit4ClassRunner.class )
@ContextConfiguration( locations = "/spring-config.xml" )
public class SpringIntegrationTest implements IntegrationTestBlueprint {
    @Inject
    private MetricRegistry metricRegistry;

    @Inject
    private SqlSessionFactory sqlSessionFactory;

    @Inject
    private FakeMapper fakeMapper;

    @Before
    public void setUp() throws SQLException, IOException {
        /**
         * no-op
         * Spring runner injects the instances for us.
         */
    }

    @After
    public void tearDown() throws SQLException {
        /**
         * no-op
         * Spring runner handles this for us.
         */
    }

    @Test
    @Override
    public void testMyBatisConfiguration_containsInstanceOfInterceptor() {
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
