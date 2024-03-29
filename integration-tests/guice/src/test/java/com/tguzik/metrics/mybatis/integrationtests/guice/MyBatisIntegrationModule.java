package com.tguzik.metrics.mybatis.integrationtests.guice;

import javax.sql.DataSource;

import com.google.inject.util.Providers;
import com.tguzik.metrics.mybatis.InstrumentingInterceptor;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.hsqldb.jdbc.JDBCDataSource;
import org.mybatis.guice.MyBatisModule;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public class MyBatisIntegrationModule extends MyBatisModule {
    private final DataSource dataSource;

    public MyBatisIntegrationModule( DataSource dataSource ) {
        this.dataSource = dataSource;
    }

    @Override
    protected void initialize() {
        // Basic stuff
        environmentId( "integration test" );
        bindTransactionFactoryType( JdbcTransactionFactory.class );
        bindDataSourceProvider( Providers.of( dataSource ) );
        addMapperClass( FakeMapper.class );
        failFast( true );

        // Add our interceptor
        addInterceptorClass( InstrumentingInterceptor.class );
    }
}
