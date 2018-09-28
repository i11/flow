package com.bobkevic.flow.modules;

import com.bobkevic.flow.http.spring.Server;
import com.bobkevic.flow.http.spring.SpringPublisher;
import com.bobkevic.flow.http.spring.SpringSubscriber;
import com.bobkevic.flow.modules.annotations.SpringPublisherExecutor;
import com.bobkevic.flow.modules.annotations.SpringPublisherQueue;
import com.bobkevic.flow.services.PublisherSupplier;
import com.bobkevic.flow.services.SubscriberSupplier;
import com.bobkevic.flow.services.http.Http;
import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import java.util.Queue;
import java.util.concurrent.ExecutorService;

public class SpringServiceModule extends AbstractModule {

  public static final TypeLiteral<SubscriberSupplier<Http>>
      SUBSCRIBER_SUPPLIER_TYPE_LITERAL = new TypeLiteral<>() {
  };

  public static final TypeLiteral<PublisherSupplier<Http>>
      PUBLISHER_SUPPLIER_TYPE_LITERAL = new TypeLiteral<>() {
  };

  public static final TypeLiteral<Queue<Http>>
      QUEUE_TYPE_LITERAL = new TypeLiteral<>() {
  };

  @Override
  protected void configure() {
    final Provider<Server> serverProvider = getProvider(Server.class);
    final Provider<ExecutorService> executorServiceProvider =
        getProvider(Key.get(ExecutorService.class, SpringPublisherExecutor.class));
    final Provider<Queue<Http>> queueProvider =
        getProvider(Key.get(QUEUE_TYPE_LITERAL, SpringPublisherQueue.class));
    Multibinder.newSetBinder(binder(), SUBSCRIBER_SUPPLIER_TYPE_LITERAL)
        .addBinding().toInstance(SpringSubscriber::new);
    Multibinder.newSetBinder(binder(), PUBLISHER_SUPPLIER_TYPE_LITERAL)
        .addBinding()
        .toInstance(() ->
            new SpringPublisher(
                serverProvider.get(),
                executorServiceProvider.get(),
                queueProvider.get()));
  }

}
