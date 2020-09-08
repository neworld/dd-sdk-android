/*
 * Unless explicitly stated otherwise all files in this repository are licensed under the Apache License Version 2.0.
 * This product includes software developed at Datadog (https://www.datadoghq.com/).
 * Copyright 2016-Present Datadog, Inc.
 */

package com.datadog.android.rx

import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single

/**
 * Returns an [Observable<T>] which intercepts any Exception thrown in the upper stream to
 * send it as a RUM error event.
 * The interceptor will not consume the error and you will be able to threat the error
 * down the stream by using any of the available RxJava existing operators.
 */
inline fun <reified T> Observable<T>.publishErrorsToRum(): Observable<T> {
    return this.doOnError(DatadogErrorConsumer())
}

/**
 * Returns a [Single<T>] which intercepts any Exception thrown in the upper stream to
 * send it as a RUM error event.
 * The interceptor will not consume the error and you will be able to threat the error
 * down the stream by using any of the available RxJava existing operators.
 */
inline fun <reified T> Single<T>.publishErrorsToRum(): Single<T> {
    return this.doOnError(DatadogErrorConsumer())
}

/**
 * Returns a [Flowable<T>] which intercepts any Exception thrown in the upper stream to
 * send it as a RUM error event.
 * The interceptor will not consume the error and you will be able to threat the error
 * down the stream by using any of the available RxJava existing operators.
 */
inline fun <reified T> Flowable<T>.publishErrorsToRum(): Flowable<T> {
    return this.doOnError(DatadogErrorConsumer())
}

/**
 * Returns a [Maybe<T>] which intercepts any Exception thrown in the upper stream to
 * send it as a RUM error event.
 * The interceptor will not consume the error and you will be able to threat the error
 * down the stream by using any of the available RxJava existing operators.
 */
inline fun <reified T> Maybe<T>.publishErrorsToRum(): Maybe<T> {
    return this.doOnError(DatadogErrorConsumer())
}

/**
 * Returns a [Completable<T>] which intercepts any Exception thrown in the upper stream to
 * send it as a RUM error event.
 * The interceptor will not consume the error and you will be able to threat the error
 * down the stream by using any of the available RxJava existing operators.
 */
fun Completable.publishErrorsToRum(): Completable {
    return this.doOnError(DatadogErrorConsumer())
}
