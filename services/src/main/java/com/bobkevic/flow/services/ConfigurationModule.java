package com.bobkevic.flow.services;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import java.util.List;
import java.util.function.Supplier;

@FunctionalInterface
public interface ConfigurationModule<T> extends Supplier<ConfigurationModule<T>> {

  static <T> ConfigurationModule<T> binding(Class<T> clazz, T instance) {
    return new PublicAbstractModule<>(null, clazz, instance);
  }

  default <E> ConfigurationModule<E> and(Class<E> clazz, E instance) {
    return new PublicAbstractModule<>(this, clazz, instance);
  }

  default <E> ConfigurationModule<E> and(Key<E> key, E instance) {
    return new PublicAbstractModule<>(this, key, instance);
  }

  default List<Module> collect() {
    var parent = ((PublicAbstractModule) this).parent;

    final List<Module> modules = Lists.newArrayList((PublicAbstractModule) this);
    while (parent != null) {
      modules.add((Module) parent);
      parent = ((PublicAbstractModule) parent).parent;
    }

    return modules;
  }

  class PublicAbstractModule<M> extends AbstractModule implements ConfigurationModule<M> {

    final ConfigurationModule parent;
    private final Key<M> key;
    private final M instance;

    public PublicAbstractModule(final ConfigurationModule parent,
                                final Class<M> clazz,
                                final M instance) {
      this.parent = parent;
      this.key = Key.get(clazz);
      this.instance = instance;
    }

    public PublicAbstractModule(final ConfigurationModule parent,
                                final Key<M> key,
                                final M instance) {
      this.parent = parent;
      this.key = key;
      this.instance = instance;
    }

    @Override
    public ConfigurationModule<M> get() {
      return this;
    }

    @Override
    protected void configure() {
      bind(key).toInstance(instance);
    }
  }

}
