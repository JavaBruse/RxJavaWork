package com.javabruse;

import rx.Observable;
import rx.Observer;

public class RxJavaMaster  extends Observable implements Observer {


    protected RxJavaMaster(OnSubscribe f) {
        super(f);
    }

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onNext(Object object) {

    }

}

