package com.javabruse;


import com.javabruse.RxMaster.Observable;
import com.javabruse.RxMaster.interfaces.Observer;

public class Main {
    public static void main(String[] args) {
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
}