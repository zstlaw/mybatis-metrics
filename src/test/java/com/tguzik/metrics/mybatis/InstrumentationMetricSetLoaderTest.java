package com.tguzik.metrics.mybatis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.codahale.metrics.MetricRegistry;
import com.tguzik.metrics.BasicInstrumentation;
import org.apache.ibatis.plugin.Invocation;
import org.junit.Before;
import org.junit.Test;

public class InstrumentationMetricSetLoaderTest {
    private MetricRegistry registry;
    private InstrumentationMetricSetLoader loader;
    private Invocation invocation;

    @Before
    public void setUp() throws NoSuchMethodException {
        this.registry = new MetricRegistry();
        this.loader = new InstrumentationMetricSetLoader( registry );
        this.invocation = new Invocation( new TestObject(),
                                          TestObject.class.getDeclaredMethod( "testMethod",
                                                                              int.class,
                                                                              String.class,
                                                                              Object.class ),
                                          new Object[] { 123, "argument value", Long.valueOf( 234 ) } );
    }

    @Test
    public void testLoad() throws Exception {
        BasicInstrumentation instrumentation = loader.load( invocation );

        assertNotNull( instrumentation );
    }

    @Test
    public void testDeriveBaseName() throws Exception {
        assertEquals( "com.tguzik.metrics.mybatis.InstrumentationMetricSetLoaderTest.TestObject#testMethod",
                      loader.deriveBaseName( invocation ) );
    }

    private static class TestObject {
        public void testMethod( int arg1, String arg2, Object arg3 ) {
            // do nothing
        }
    }
}
