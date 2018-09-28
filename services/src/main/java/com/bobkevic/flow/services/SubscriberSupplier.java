package com.bobkevic.flow.services;

import java.util.concurrent.Flow;
import java.util.function.Function;

@FunctionalInterface
public interface SubscriberSupplier<T> extends Function<T, Flow.Subscriber<T>> {

}
