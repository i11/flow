package com.bobkevic.util;

import java.util.function.Supplier;

@FunctionalInterface
public interface Stack<V> extends Supplier<V> {

  static <V> Stack<V> stack(V value) {
    return new Head<>(null, value);
  }

  static <V> Stack<V> stack(Supplier<V> supplier) {
    return new Head<>(null, supplier);
  }

  default <T> Stack<T> push(T value) {
    return new Head<>(this, value);
  }

  default <T> Stack<T> push(Supplier<T> supplier) {
    return new Head<>(this, supplier);
  }

  default <TP> Stack<TP> pop() {
    return ((Head<TP, V>) this).parent;
  }

  final class Head<P, N> implements Stack<N> {

    Stack<P> parent;
    private N value;
    private Supplier<N> supplier;

    Head(final Stack<P> parent, final N value) {
      this.parent = parent;
      this.value = value;
    }

    Head(final Stack<P> parent, final Supplier<N> supplier) {
      this.parent = parent;
      this.supplier = supplier;
    }

    @Override
    @SuppressWarnings("unchecked")
    public N get() {
      return null != value ? value : supplier.get();
    }
  }

}
