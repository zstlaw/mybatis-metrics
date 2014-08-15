package com.tguzik.mybatismetrics.integrationtests.xmlfiles;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public interface FakeMapper {
    int doSelect( String arg1, int arg2, Object arg3 );

    int doFailingSelect( String arg1, int arg2, Object arg3 );

    void doInsert( String arg1, int arg2, Object arg3 );

    void doFailingInsert( String arg1, int arg2, Object arg3 );

    void doUpdate( String arg1, int arg2, Object arg3 );

    void doFailingUpdate( String arg1, int arg2, Object arg3 );

    void doDelete( String arg1, int arg2, Object arg3 );

    void doFailingDelete( String arg1, int arg2, Object arg3 );
}
