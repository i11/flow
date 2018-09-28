package com.bobkevic.flow.services;

import java.util.concurrent.Flow;
import java.util.function.Supplier;

@FunctionalInterface
public interface PublisherSupplier<T> extends Supplier<Flow.Publisher<T>> {

  Flow.Publisher<T> get();

}
