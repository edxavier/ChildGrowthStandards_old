package com.edxavier.childgrowthstandards.helpers;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * @author : Eder Xavier Rojas
 * @date : 26/08/2016 - 00:55
 * @package : com.vynil.domain
 * @project : Vynil
 */
public class RxBus implements Serializable {

	private static final RxBus INSTANCE = new RxBus();
	//Will be used to emit events to subscribers listening for those events.
	private final Subject<Object, Object> mBus = new SerializedSubject<>(PublishSubject.create());

	public static RxBus getInstance() {
		return INSTANCE;
	}

	public void post(Object event) {
		mBus.onNext(event);
	}

	public <T> Subscription register(final Class<T> eventClass, Action1<T> onNext) {
		return mBus.filter(event -> event.getClass().equals(eventClass))
				.map(obj -> (T) obj)
				.subscribe(onNext);
	}

	public <T> Subscription registerWithDebounce(int milliSeconds, final Class<T> eventClass, Action1<T> onNext) {
		return mBus
				.debounce(milliSeconds, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.filter(event -> event.getClass().equals(eventClass))
				.map(obj -> (T) obj)
				.subscribe(onNext);
	}
}