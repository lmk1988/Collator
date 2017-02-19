package com.github.lmk1988.collator;

import com.github.lmk1988.collator.callbacks.OnCompletionCallback;

import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CompletionCollatorTest {

    private CompletionCollator collator;

    @Before
    public void setUp() throws Exception {
        collator = new CompletionCollator();
    }

    @Test
    public void testAwaitNoNodes() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCompletion(new OnCompletionCallback() {
                @Override
                public void onComplete() {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();
    }

    @Test
    public void testExceptionWhenAwaitTwice() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCompletion(new OnCompletionCallback() {
                @Override
                public void onComplete() {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            collator.awaitCompletion(new OnCompletionCallback() {
                @Override
                public void onComplete() {
                    //Should not complete here
                    fail();
                }
            });

            //Should have thrown RunTimeException
            fail();
        } catch (Exception e) {
            //Will catch RunTimeException
        }
    }

    @Test
    public void testExceptionWhenReserveAfterAwait() throws Exception {
        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCompletion(new OnCompletionCallback() {
                @Override
                public void onComplete() {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            CompletionNode node = collator.reserveCompletion();
            fail();
        } catch (Exception e) {
            //Will catch RunTimeException
        }
    }

    @Test
    public void testExceptionWhenReserveAfterAwait2() throws Exception {

        CompletionNode node1 = collator.reserveCompletion();
        node1.completed();

        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCompletion(new OnCompletionCallback() {
                @Override
                public void onComplete() {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            CompletionNode node2 = collator.reserveCompletion();
            fail();
        } catch (Exception e) {
            //Will catch RunTimeException
        }
    }

    private void testAsyncCompletion(int count, final int sleepMilli) throws Exception {

        final CountDownLatch collatorSignal = new CountDownLatch(1);
        final CountDownLatch nodeSignal = new CountDownLatch(count);

        for (int i = 0; i < count; i++) {
            final CompletionNode node = collator.reserveCompletion();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(sleepMilli);
                    } catch (Exception e) {
                        fail();
                    }
                    node.completed();
                    nodeSignal.countDown();
                }
            }).start();
        }

        //Wait for variable time given (give an extra count for wait delay)
        nodeSignal.await(sleepMilli * (count + 1), TimeUnit.MILLISECONDS);

        collator.awaitCompletion(new OnCompletionCallback() {
            @Override
            public void onComplete() {
                collatorSignal.countDown();
            }
        });

        collatorSignal.await();
    }

    @Test
    public void testSingleAsyncCompletion() throws Exception {
        testAsyncCompletion(1, 100);
    }

    @Test
    public void testDoubleAsyncCompletion() throws Exception {
        testAsyncCompletion(2, 100);
    }

    @Test
    public void testThreeAsyncCompletion() throws Exception {
        testAsyncCompletion(3, 100);
    }

    @Test
    public void testLargeAsyncCompletion() throws Exception {
        testAsyncCompletion(50, 100);
    }

    private void testSerialCompletion(int count) throws Exception {

        final CountDownLatch collatorSignal = new CountDownLatch(1);

        for (int i = 0; i < count; i++) {
            final CompletionNode node = collator.reserveCompletion();
            node.completed();
        }

        collator.awaitCompletion(new OnCompletionCallback() {
            @Override
            public void onComplete() {
                collatorSignal.countDown();
            }
        });

        collatorSignal.await();
    }

    @Test
    public void testSingleSerialCompletion() throws Exception {
        testSerialCompletion(1);
    }

    @Test
    public void testDoubleSerialCompletion() throws Exception {
        testSerialCompletion(2);
    }

    @Test
    public void testThreeSerialCompletion() throws Exception {
        testSerialCompletion(3);
    }

    @Test
    public void testLargeSerialCompletion() throws Exception {
        testSerialCompletion(50);
    }
}
