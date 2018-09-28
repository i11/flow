package com.bobkevic.flow.http.spring;

import static java.util.concurrent.CompletableFuture.runAsync;
import static org.springframework.web.reactive.function.BodyInserters.fromObject;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.bobkevic.flow.services.http.Http;
import com.bobkevic.flow.services.http.HttpBuilder;
import java.lang.invoke.MethodHandles;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Flow;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.MimeType;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

public class SpringSubscription implements Flow.Subscription, AutoCloseable {

  private Flow.Subscriber<? super Http> subscriber;
  private final Server server;
  private final Queue<Http> resources;
  private final AtomicBoolean isCanceled;
  private final ExecutorService executor;
  private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  public SpringSubscription(
      final Flow.Subscriber<? super Http> subscriber,
      final Server server,
      final Queue<Http> resources,
      final ExecutorService executor) {
    this.subscriber = subscriber;
    this.server = server;
    this.resources = resources;
    this.executor = executor;
    this.isCanceled = new AtomicBoolean(false);
  }

  public void start(final Http context) {
    final RequestPredicate predicate =
        method(HttpMethod.valueOf(context.method().toUpperCase()))
            .and(RequestPredicates.path(context.uri()));
    // TODO: Support multiple
    context.accept()
        .ifPresent(mediaType -> predicate.and(accept(MediaType.valueOf(mediaType))));
    // TODO: Support multiple
    context.contentType()
        .ifPresent(mediaType -> predicate.and(contentType(MediaType.valueOf(mediaType))));

    final RouterFunction<ServerResponse> route = route(predicate, request -> {
      final HttpBuilder httpBuilder = HttpBuilder.builder()
          .method(request.methodName())
          .uri(request.path())
          .parameters(request.pathVariables());

      request.headers().contentType()
          .ifPresent(contentType -> httpBuilder.contentType(contentType.toString()));

      Optional.of(request.headers().accept())
          .filter(accepts -> accepts.size() > 0)
          .map(accepts -> accepts.get(0))
          .map(MimeType::toString)
          .ifPresent(httpBuilder::accept);

      return request
          .bodyToMono(String.class)
          .map(httpBuilder::body)
          .defaultIfEmpty(httpBuilder)
          .map(HttpBuilder::build)
          .map(http -> resources.add(http))
          .flatMap(v -> ServerResponse
              .ok()
              .contentType(MediaType.APPLICATION_JSON)
              .body(fromObject("{\"status\": \"queued\"}")))
          .switchIfEmpty(ServerResponse.badRequest().build());


    });
    server.start(route);
  }

  @Override
  public void request(long maximumCapacity) {
    Optional.ofNullable(isCanceled)
        .map(AtomicBoolean::get)
        .filter(v -> !v)
        .map(v -> maximumCapacity)
        .filter(n -> n < 0)
        .ifPresentOrElse(
            n -> runAsync(() -> subscriber
                    .onError(new IllegalArgumentException("Maximum capacity is less than 0")),
                executor)
                .orTimeout(10, TimeUnit.SECONDS)
                .exceptionally(ex -> {
                  log.error("Failed onError for {}", subscriber, ex);
                  return null;
                }),
            () -> Optional.ofNullable(resources)
                .map(Queue::size)
//                .map(size -> Math.min(maximumCapacity, size))
                .ifPresent(size -> {
//                  log.info("Current number of items: {}", size);
                  for (int i = 0; i < maximumCapacity; i++) {
                    final Http next = resources.poll();
                    runAsync(() -> subscriber.onNext(next), executor)
                        .orTimeout(10, TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                          log.error("Failed onNext for {}", subscriber, ex);
                          return null;
                        });
                  }
//                  long remaining = resources.size() - size;
//                  log.info("Remaining items: {}", remaining);
//
//                  Optional.of(remaining)
//                      .filter(r -> r == 0)
//                      .ifPresent(r -> subscriber.onComplete());
                })
        );
  }

  @Override
  public void cancel() {
    try {
      close();
    } catch (final Exception e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void close() throws Exception {
    isCanceled.set(true);
    log.info("{} shutdown initiated", this);
    server.close();
    executor.shutdown();
    executor.awaitTermination(10, TimeUnit.SECONDS);
  }
}
