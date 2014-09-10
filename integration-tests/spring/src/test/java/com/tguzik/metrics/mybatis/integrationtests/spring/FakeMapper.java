package com.tguzik.metrics.mybatis.integrationtests.spring;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public interface FakeMapper {
    @Select( "select count(*) from test" )
    int doSelect( String arg1, int arg2, Object arg3 );

    @Select( "not sql" )
    int doFailingSelect( String arg1, int arg2, Object arg3 );

    @Insert( "insert into test (key, value) values ('key', 'value')" )
    void doInsert( String arg1, int arg2, Object arg3 );

    @Insert( "not sql" )
    void doFailingInsert( String arg1, int arg2, Object arg3 );

    @Update( "update test set value = 'new value'" )
    void doUpdate( String arg1, int arg2, Object arg3 );

    @Update( "not sql" )
    void doFailingUpdate( String arg1, int arg2, Object arg3 );

    @Delete( "delete from test" )
    void doDelete( String arg1, int arg2, Object arg3 );

    @Delete( "not sql" )
    void doFailingDelete( String arg1, int arg2, Object arg3 );
}
