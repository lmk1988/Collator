package com.github.lmk1988.collator;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class SuccessCollatorTest {

    private ArrayList<Boolean> resultList;

    @Before
    public void setUp() {
        resultList = new ArrayList<>();
    }

    @Test
    public void testIsAllSuccess1() throws Exception {
        assertTrue(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess2() throws Exception {
        resultList.add(true);
        assertTrue(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess3() throws Exception {
        resultList.add(false);
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess4() throws Exception {
        resultList.add(true);
        resultList.add(false);
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess5() throws Exception {
        resultList.add(false);
        resultList.add(true);
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess6() throws Exception {
        resultList.add(true);
        resultList.add(true);
        assertTrue(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess7() throws Exception {
        resultList.add(false);
        resultList.add(false);
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess8() throws Exception {
        for (int i = 0; i < 50; i++) {
            resultList.add(false);
        }
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess9() throws Exception {
        for (int i = 0; i < 50; i++) {
            resultList.add(true);
        }
        assertTrue(SuccessCollator.isAllSuccess(resultList));
    }

    @Test
    public void testIsAllSuccess10() throws Exception {
        resultList.add(false);
        for (int i = 0; i < 50; i++) {
            resultList.add(true);
        }
        assertFalse(SuccessCollator.isAllSuccess(resultList));
    }
}
