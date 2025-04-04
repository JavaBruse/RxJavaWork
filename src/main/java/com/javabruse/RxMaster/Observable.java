package com.javabruse.RxMaster;

import com.javabruse.RxMaster.interfaces.Disposable;
import com.javabruse.RxMaster.interfaces.Observer;
import com.javabruse.RxMaster.interfaces.Scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;

public class Observable<T> {

    public interface OnSubscribe<T> {
        void subscribe(Observer<T> observer);
    }

    public final OnSubscribe<T> onSubscribe;

    public Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> source) {
        return new Observable<>(source);
    }

    public Disposable subscribe(Observer<T> observer) {
        AtomicBoolean isDisposed = new AtomicBoolean(false);

        Observer<T> safeObserver = new Observer<T>() {
            @Override
            public void onNext(T item) {
                if (!isDisposed.get()) observer.onNext(item);
            }

            @Override
            public void onError(Throwable t) {
                if (!isDisposed.get()) observer.onError(t);
            }

            @Override
            public void onComplete() {
                if (!isDisposed.get()) observer.onComplete();
            }
        };

        onSubscribe.subscribe(safeObserver);

        return new Disposable() {
            @Override
            public void dispose() {
                isDisposed.set(true);
            }

            @Override
            public boolean isDisposed() {
                return isDisposed.get();
            }
        };
    }

    public <R> Observable<R> map(Function<T, R> mapper) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            R mapped = mapper.apply(item);
                            observer.onNext(mapped);
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> filter(Predicate<T> predicate) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            if (predicate.test(item)) {
                                observer.onNext(item);
                            }
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return Observable.create(observer ->
                scheduler.execute(() -> Observable.this.subscribe(observer))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        scheduler.execute(() -> observer.onNext(item));
                    }

                    @Override
                    public void onError(Throwable t) {
                        scheduler.execute(() -> observer.onError(t));
                    }

                    @Override
                    public void onComplete() {
                        scheduler.execute(observer::onComplete);
                    }
                })
        );
    }

    public <R> Observable<R> flatMap(Function<T, Observable<R>> mapper) {
        return Observable.create(observer ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        try {
                            Observable<R> inner = mapper.apply(item);
                            inner.subscribe(new Observer<R>() {
                                @Override
                                public void onNext(R r) {
                                    observer.onNext(r);
                                }

                                @Override
                                public void onError(Throwable t) {
                                    observer.onError(t);
                                }

                                @Override
                                public void onComplete() {
                                }
                            });
                        } catch (Throwable t) {
                            observer.onError(t);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        observer.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        observer.onComplete();
                    }
                })
        );
    }
}