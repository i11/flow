package com.bobkevic.flow.http.spring;

import static com.bobkevic.util.Checked.Runnable.uncheckRunnable;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

public class Server implements AutoCloseable {

  private static final RouterFunction<ServerResponse>
      DEFAULT_ROUTE =
      RouterFunctions.route(GET("/"), request -> ServerResponse.ok().build());
  private NettyContext context;
  private String host;
  private Integer port;
  private RouterFunction<ServerResponse> route;

  public Server(final String host, final Integer port) {
    this.host = host;
    this.port = port;
  }

  public void start() {
    start(DEFAULT_ROUTE);
  }

  // TODO: Fix restarts on every route modification
  // TODO: Remove synchronization
  public synchronized void start(final RouterFunction<ServerResponse> route) {
    this.route = Optional.ofNullable(this.route).map(r -> r.and(route)).orElse(route);
    Optional.ofNullable(context)
        .filter(c -> !c.isDisposed())
        .map(c -> uncheckRunnable(this::close))
        .ifPresent(Runnable::run);

    final HttpHandler httpHandler = RouterFunctions.toHttpHandler(this.route);
    final ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
    final HttpServer server = HttpServer.create(host, port);
    context = server.newHandler(adapter).block();
  }

  @Override
  public void close() throws Exception {
    final ChannelFuture close = context.channel().close();
    if (close.await(10, TimeUnit.SECONDS)) {
      close.cancel(true);
    }
  }
}
