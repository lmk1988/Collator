package com.github.lmk1988.collator;

import android.support.annotation.NonNull;

import com.github.lmk1988.collator.callbacks.CollatedResultsCallback;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class CallbackCollatorStringTest {

    private CallbackCollator<String> collator;

    @Before
    public void setUp() {
        collator = new CallbackCollator<>();
    }

    @Test
    public void testAwaitNoNodes() throws Exception {

        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCallbacks(new CollatedResultsCallback<String>() {
                @Override
                public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                    assertEquals(collatedResults.size(), 0);
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
            collator.awaitCallbacks(new CollatedResultsCallback<String>() {
                @Override
                public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            collator.awaitCallbacks(new CollatedResultsCallback<String>() {
                @Override
                public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
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
            collator.awaitCallbacks(new CollatedResultsCallback<String>() {
                @Override
                public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            CallbackCollatorNode<String> node = collator.reserveCallback();
            fail();
        } catch (Exception e) {
            //Will catch RunTimeException
        }
    }

    @Test
    public void testExceptionWhenReserveAfterAwait2() throws Exception {

        CallbackCollatorNode<String> node1 = collator.reserveCallback();
        node1.returnCallbackResult("hello");

        final CountDownLatch signal = new CountDownLatch(1);

        try {
            collator.awaitCallbacks(new CollatedResultsCallback<String>() {
                @Override
                public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                    assertEquals(collatedResults.size(), 1);
                    assertEquals(collatedResults.get(0), "hello");
                    signal.countDown();
                }
            });
        } catch (Exception e) {
            fail();
        }

        signal.await();

        try {
            CallbackCollatorNode<String> node2 = collator.reserveCallback();
            fail();
        } catch (Exception e) {
            //Will catch RunTimeException
        }
    }

    private void testAsyncCompletion(final int count, final int sleepMilli) throws Exception {

        final CountDownLatch collatorSignal = new CountDownLatch(1);
        final CountDownLatch nodeSignal = new CountDownLatch(count);
        final ArrayList<String> randomStringArray = new ArrayList<>();

        Random r = new Random();
        for (int i = 0; i < count; i++) {
            final CallbackCollatorNode<String> node = collator.reserveCallback();

            final String randString = Long.toString(r.nextLong());
            randomStringArray.add(randString);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(sleepMilli);
                    } catch (Exception e) {
                        fail();
                    }
                    node.returnCallbackResult(randString);
                    nodeSignal.countDown();
                }
            }).start();
        }

        //Wait for variable time given (give an extra count for wait delay)
        nodeSignal.await(sleepMilli * (count + 1), TimeUnit.MILLISECONDS);

        collator.awaitCallbacks(new CollatedResultsCallback<String>() {
            @Override
            public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                assertEquals(collatedResults.size(), count);
                assertEquals(randomStringArray.size(), count);
                for (int i = 0; i < count; i++) {
                    //This may not be in sequence
                    assertTrue(randomStringArray.contains(collatedResults.get(i)));
                }
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

    private void testSerialCompletion(final int count) throws Exception {

        final CountDownLatch collatorSignal = new CountDownLatch(1);
        final ArrayList<String> randomStringArray = new ArrayList<>();

        Random r = new Random();

        for (int i = 0; i < count; i++) {
            final String randString = Long.toString(r.nextLong());
            randomStringArray.add(randString);

            final CallbackCollatorNode<String> node = collator.reserveCallback();
            node.returnCallbackResult(randString);
        }

        collator.awaitCallbacks(new CollatedResultsCallback<String>() {
            @Override
            public void onCompleteCallbacks(@NonNull List<String> collatedResults) {
                assertEquals(collatedResults.size(), count);
                assertEquals(randomStringArray.size(), count);
                for (int i = 0; i < count; i++) {
                    assertEquals(collatedResults.get(i), randomStringArray.get(i));
                }
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
