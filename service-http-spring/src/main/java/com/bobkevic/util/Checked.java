package com.bobkevic.util;

public interface Checked {

  @FunctionalInterface
  interface Function<T, R> {

    R apply(T t) throws Exception;

    static <T, R> java.util.function.Function<T, R> uncheckFunction(Function<T, R> unsafe) {
      return param -> {
        try {
          return unsafe.apply(param);
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      };
    }
  }

  @FunctionalInterface
  interface Runnable {

    void run() throws Exception;

    static java.lang.Runnable uncheckRunnable(Runnable unsafe) {
      return () -> {
        try {
          unsafe.run();
        } catch (final Exception e) {
          throw new RuntimeException(e);
        }
      };
    }
  }
}
