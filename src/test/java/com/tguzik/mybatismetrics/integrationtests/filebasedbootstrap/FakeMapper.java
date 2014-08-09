package com.tguzik.mybatismetrics.integrationtests.filebasedbootstrap;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public interface FakeMapper {
    int doSelect( String arg1, int arg2, Object arg3 );

    void doInsert( String arg1, int arg2, Object arg3 );

    void doUpdate( String arg1, int arg2, Object arg3 );

    void doDelete( String arg1, int arg2, Object arg3 );
}
