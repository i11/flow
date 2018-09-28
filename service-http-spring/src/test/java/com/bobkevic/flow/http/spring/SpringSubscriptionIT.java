package com.bobkevic.flow.http.spring;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.bobkevic.flow.services.http.Http;
import com.bobkevic.flow.services.http.HttpBuilder;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;

class SpringSubscriptionIT {


  private SpringSubscription subscription;
  private Server server;
  private Flow.Subscriber<? super Http> subscriber;

  @BeforeEach
  void setUp() {
//    final Server server = mock(Server.class);
    final BlockingQueue<Http> queue = new ArrayBlockingQueue<>(1);
    final ExecutorService executor = Executors.newSingleThreadExecutor();
    subscriber = new TestSubscriber();
    server = new Server("localhost", 8080);
    subscription = new SpringSubscription(subscriber, server, queue, executor);
  }

  @Test
  void start() throws Exception {
    subscription.start(HttpBuilder.builder().build());
    final WebClient client = WebClient.create("http://localhost:8080/");

    final ClientResponse response = client.get()
        .uri("/")
        .accept(APPLICATION_JSON)
        .exchange()
        .block();
    final Optional<ClientResponse> maybeResponse = Optional.ofNullable(response);

    assertThat(maybeResponse
            .map(ClientResponse::statusCode)
            .map(HttpStatus::isError)
            .orElse(true),
        is(false));

    assertThat(maybeResponse
            .map(r -> r.bodyToMono(String.class).block())
            .orElse("BOGUS"),
        is("{\"status\": \"queued\"}"));
    server.close();
  }

  @Test
  void request() {
    subscription.request(1);
    subscriber.
  }

//  @AfterEach
//  void tearDown() throws Exception {
//
//  }


  class TestSubscriber implements Flow.Subscriber<Http> {

    Flow.Subscription subscription;
    Http next;
    Throwable error;
    boolean isComplete;

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
      this.subscription = subscription;
    }

    @Override
    public void onNext(final Http item) {
      this.next = item;
    }

    @Override
    public void onError(final Throwable throwable) {
      this.error = throwable;
    }

    @Override
    public void onComplete() {
      this.isComplete = true;
    }
  }
}