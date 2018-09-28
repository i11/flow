package com.bobkevic.flow.http.spring;

import com.bobkevic.flow.services.http.Http;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.concurrent.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringSubscriber implements Flow.Subscriber<Http> {

  private final Http context;
  private SpringSubscription subscription;
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public SpringSubscriber(final Http context) {
    this.context = context;
  }

  @Override
  public void onSubscribe(final Flow.Subscription subscription) {
    this.subscription = Optional.ofNullable(subscription)
        .map(sub -> (SpringSubscription) subscription)
        .orElseThrow(() -> new RuntimeException("Invalid subscription"));
    this.subscription.start(context);
    this.subscription.request(1);
  }

  @Override
  public void onNext(Http item) {
    log.info("Got next: {}", item);
    this.subscription.request(1);
  }

  @Override
  public void onError(Throwable throwable) {
    log.info("{} got an error", this, throwable);
  }

  @Override
  public void onComplete() {
    log.info("{} completed", this);
    this.subscription.cancel();
  }
}
