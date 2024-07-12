/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.xagau.mq;

import com.xagau.atomiic.Log;
import junit.framework.TestCase;

/**
 *
 * @author Sean
 */
public class UtilityTest extends TestCase {
    
    public UtilityTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of lag method, of class Utility.
     */
    public void testLag() {
        Log.info("lag");
        Utility.lag();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }

    /**
     * Test of fixKeyLength method, of class Utility.
     */
    public void testFixKeyLength() {
        Log.info("fixKeyLength");
        Utility.fixKeyLength();
        // TODO review the generated test code and remove the default call to fail.
        //fail("The test case is a prototype.");
    }
    
}
