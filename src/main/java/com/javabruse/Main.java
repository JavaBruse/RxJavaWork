package com.javabruse;


import com.javabruse.RxMaster.ComputationScheduler;
import com.javabruse.RxMaster.IOThreadScheduler;
import com.javabruse.RxMaster.Observable;
import com.javabruse.RxMaster.interfaces.Disposable;
import com.javabruse.RxMaster.interfaces.Observer;
import com.javabruse.RxMaster.interfaces.Scheduler;

public class Main {
    public static void main(String[] args) {
        filterAndMap();
        flatMap();
        subscribeOnAndBack();
        andObserveOn();
        disposable();
    }

    private static void filterAndMap() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(10);
            emitter.onNext(20);
            emitter.onNext(30);
            emitter.onNext(15);
            emitter.onNext(12);
            emitter.onComplete();
        });

        observable
                .filter(x -> x > 16)
                .map(x -> "Значение: " + x)
                .subscribe(new Observer<>() {
                    public void onNext(String item) {
                        System.out.println(item);
                    }

                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    public void onComplete() {
                        System.out.println("Завершено");
                    }
                });
    }

    private static void flatMap() {
        Observable<Integer> observable = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
            emitter.onComplete();
        });

        observable
                .flatMap(x -> Observable.create(inner -> {
                    inner.onNext(x * 10);
                    inner.onNext(x * 100);
                    inner.onComplete();
                }))
                .subscribe(new Observer<>() {
                    public void onNext(Object item) {
                        System.out.println("Получено: " + item);
                    }

                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }

                    public void onComplete() {
                        System.out.println("Завершено");
                    }
                });
    }

    private static void subscribeOnAndBack() {
        Scheduler computation = new ComputationScheduler();

        Observable<String> observable = Observable.create(emitter -> {
            System.out.println("Источник работает в потоке: " + Thread.currentThread().getName());
            emitter.onNext("Rx");
            emitter.onNext("Master");
            emitter.onComplete();
        });
        observable
                .subscribeOn(computation)
                .subscribe(new Observer<>() {
                    public void onNext(String item) {
                        System.out.println("Получено: " + item);
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                        System.out.println("Готово");
                    }
                });
    }

    private static void andObserveOn() {
        Scheduler io = new IOThreadScheduler();

        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("Rx");
            emitter.onNext("Java");
            emitter.onComplete();
        });

        observable
                .observeOn(io)
                .subscribe(new Observer<>() {
                    public void onNext(String item) {
                        System.out.println("Обработка в потоке: " + Thread.currentThread().getName());
                        System.out.println("Данные: " + item);
                    }

                    public void onError(Throwable t) {
                    }

                    public void onComplete() {
                        System.out.println("Завершено");
                    }
                });
    }

    private static void disposable() {
        Observable<String> observable = Observable.create(emitter -> {
            emitter.onNext("A");
            emitter.onNext("B");
            emitter.onComplete();
        });
        Disposable disposable = observable.subscribe(new Observer<>() {
            public void onNext(String item) {
                System.out.println("Получено: " + item);
            }

            public void onError(Throwable t) {
            }

            public void onComplete() {
                System.out.println("Завершено");
            }
        });
        disposable.dispose();
        System.out.println("Подписка остановлена: " + disposable.isDisposed());
    }

}