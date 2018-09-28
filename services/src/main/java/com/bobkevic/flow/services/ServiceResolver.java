package com.bobkevic.flow.services;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toSet;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.StreamSupport;

public class ServiceResolver {

  private final Injector injector;

  public ServiceResolver() {
    injector = Guice.createInjector(
        ServiceLoader.load(Module.class));
  }

  public ServiceResolver(final List<Module> configurationModules) {
    final List<Module> modules =
        StreamSupport.stream(ServiceLoader.load(Module.class).spliterator(), false)
            .collect(toCollection(() -> Lists.newArrayList(configurationModules)));
    injector = Guice.createInjector(modules);
  }

  public ServiceResolver(final Module... configurationModules) {
    this(Arrays.asList(configurationModules));
  }

  public <T> Set<T> resolve(final Class<T> clazz) {
    return injector.getAllBindings().keySet().stream()
        .filter(key -> clazz.isAssignableFrom(key.getTypeLiteral().getRawType()))
        .map(key -> injector.getInstance(clazz))
        .collect(toSet());
  }

  public <T> Set<T> resolve(final TypeLiteral<T> typeLiteral) {
    return injector.getAllBindings().keySet().stream()
        .filter(key -> typeLiteral.equals(key.getTypeLiteral()))
        .map(key -> (Key<T>) key)
        .map(injector::getInstance)
        .collect(toSet());
  }

}
