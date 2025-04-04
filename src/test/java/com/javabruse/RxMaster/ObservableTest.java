package com.javabruse.RxMaster;

import com.javabruse.RxMaster.interfaces.Observer;
import com.javabruse.RxMaster.interfaces.Scheduler;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class ObservableTest {

    @Test
    void testMapOperator() {
        List<Integer> result = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable.map(x -> x * 2)
                .subscribe(new Observer<>() {
                    public void onNext(Integer item) {
                        result.add(item);
                    }

                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t.getMessage());
                    }

                    public void onComplete() {
                        // do nothing
                    }
                });

        assertEquals(List.of(2, 4, 6), result);
    }

    @Test
    void testFilterOperator() {
        List<Integer> result = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onNext(4);
            emitter.onComplete();
        });

        observable.filter(x -> x % 2 == 0)
                .subscribe(new Observer<>() {
                    public void onNext(Integer item) {
                        result.add(item);
                    }

                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t.getMessage());
                    }

                    public void onComplete() {
                    }
                });

        assertEquals(List.of(2, 4), result);
    }

    @Test
    void testFlatMapOperator() {
        List<Integer> result = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable.flatMap(x -> Observable.create(inner -> {
                    inner.onNext(x * 10);
                    inner.onNext(x * 100);
                    inner.onComplete();
                }))
                .subscribe(new Observer<>() {

                    @Override
                    public void onNext(Object item) {
                        result.add((Integer) item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        assertEquals(List.of(10, 100, 20, 200), result);
    }

    @Test
    void testSubscribeOn() throws InterruptedException {
        List<String> threadNames = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Scheduler backgroundScheduler = action -> {
            Thread thread = new Thread(() -> {
                action.run();
                latch.countDown();
            });
            thread.start();
        };

        Observable<String> observable = Observable.create(emitter -> {
            threadNames.add(Thread.currentThread().getName());
            emitter.onNext("Hello");
            emitter.onComplete();
        });

        observable.subscribeOn(backgroundScheduler)
                .subscribe(new Observer<>() {
                    public void onNext(String item) {
                    }

                    public void onError(Throwable t) {
                        fail("Unexpected error");
                    }

                    public void onComplete() {
                    }
                });
        latch.await(2, TimeUnit.SECONDS);
        assertTrue(threadNames.get(0).startsWith("Thread-"));
    }

    @Test
    void testObserveOn() throws InterruptedException {
        List<String> callbackThreadNames = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        Scheduler scheduler = action -> {
            new Thread(() -> {
                action.run();
                latch.countDown();
            }).start();
        };

        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Hello");
            emitter.onComplete();
        });

        observable.observeOn(scheduler)
                .subscribe(new Observer<>() {
                    public void onNext(String item) {
                        callbackThreadNames.add(Thread.currentThread().getName());
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                    }
                });

        latch.await(2, TimeUnit.SECONDS);
        assertTrue(callbackThreadNames.get(0).startsWith("Thread-"));
    }

    @Test
    void testDispose() {
        AtomicBoolean received = new AtomicBoolean(false);

        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("One");
            emitter.onNext("Two");
            emitter.onComplete();
        });

        var disposable = observable.subscribe(new Observer<>() {
            public void onNext(String item) {
                received.set(true);
            }

            public void onError(Throwable t) {
            }

            public void onComplete() {
            }
        });

        disposable.dispose();
        assertTrue(disposable.isDisposed());
    }

    @Test
    void testMapWithError() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable.filter(x -> {
            if (x == 2) throw new NullPointerException("NullPointerException");
            return true;
        })
                .subscribe(new Observer<>() {
                    @Override
                    public void onNext(Integer item) {
                    }

                    @Override
                    public void onError(Throwable t) {
                        errors.add(t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                    }
                });
        assertEquals(List.of("NullPointerException"), errors);
    }

    @Test
    void testFilterWithErrorInPredicate() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable.filter(x -> {
            if (x == 2) throw new IllegalStateException("IllegalStateException");
            return true;
        }).subscribe(new Observer<>() {
            public void onNext(Integer item) {
            }

            public void onError(Throwable t) {
                errors.add(t.getMessage());
            }

            public void onComplete() {
            }
        });
        assertEquals(List.of("IllegalStateException"), errors);
    }

    @Test
    void testFlatMapWithError() {
        List<String> errors = new ArrayList<>();

        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        observable.flatMap(x -> {
            if (x == 2) throw new RuntimeException("RuntimeException");
            return Observable.create(e -> {
                e.onNext(x);
                e.onComplete();
            });
        }).subscribe(new Observer<>() {
            public void onNext(Object item) {
            }

            public void onError(Throwable t) {
                errors.add(t.getMessage());
            }

            public void onComplete() {
            }
        });

        assertEquals(List.of("RuntimeException"), errors);
    }
}
