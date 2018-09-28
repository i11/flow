package com.bobkevic.flow.http.spring;

import com.bobkevic.flow.modules.annotations.SpringPublisherExecutor;
import com.bobkevic.flow.modules.annotations.SpringPublisherQueue;
import com.bobkevic.flow.services.http.Http;
import com.google.inject.Inject;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;

public class SpringPublisher implements Flow.Publisher<Http> {

  private final Server server;
  private final ExecutorService executor;
  private final Queue<Http> queue;

  @Inject
  public SpringPublisher(final Server server,
                         @SpringPublisherExecutor final ExecutorService executor,
                         @SpringPublisherQueue final Queue<Http> queue) {
    this.server = server;
    this.executor = executor;
    this.queue = queue;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super Http> subscriber) {
    final SpringSubscription subscription =
        new SpringSubscription(subscriber, server, queue, executor);
    subscriber.onSubscribe(subscription);
  }

}
