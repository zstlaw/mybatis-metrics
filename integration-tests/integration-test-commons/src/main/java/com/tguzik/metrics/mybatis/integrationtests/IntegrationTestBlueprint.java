package com.tguzik.metrics.mybatis.integrationtests;

/**
 * @author Tomasz Guzik <tomek@tguzik.com>
 */
public interface IntegrationTestBlueprint {
    public void testMyBatisConfiguration_containsInstanceOfInterceptor();

    public void testMapperOperation_select_success();

    public void testMapperOperation_select_failure();

    public void testMapperOperation_update_success();

    public void testMapperOperation_update_failure();

    public void testMapperOperation_insert_success();

    public void testMapperOperation_insert_failure();

    public void testMapperOperation_delete_success();

    public void testMapperOperation_delete_failure();
}
