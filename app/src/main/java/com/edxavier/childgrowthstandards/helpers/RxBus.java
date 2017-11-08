package com.edxavier.childgrowthstandards.helpers;

import org.reactivestreams.Subscription;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.Subject;


/**
 * @author : Eder Xavier Rojas
 * @date : 26/08/2016 - 00:55
 * @package : com.vynil.domain
 * @project : Vynil
 */
public class RxBus{

	private static final RxBus INSTANCE = new RxBus();

	private final PublishSubject<Object> mBus = PublishSubject.create();


	public static RxBus getInstance() {
		return INSTANCE;
	}

	public void post(Object event) {
		mBus.onNext(event);
	}
	public boolean hasObservers() {
		return this.mBus.hasObservers();
	}

	public <T> Disposable register(final Class<T> eventClass, Consumer<T> onNext) {
		return mBus
				.filter(event -> event.getClass().equals(eventClass))
				.map(obj -> (T) obj)
				.subscribe(onNext);
	}

	public Observable<Object> toObservable() {
		return mBus;
	}

}