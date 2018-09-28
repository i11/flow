import com.bobkevic.flow.modules.SpringServiceModule;

module com.bobkevic.flow.http.spring {
  exports com.bobkevic.flow.modules;
  exports com.bobkevic.flow.http.spring;
  exports com.bobkevic.flow.modules.annotations;

  requires com.bobkevic.flow.services;
  requires spring.webflux;
  requires spring.web;
  requires reactor.core;
  requires reactor.netty;
  requires io.netty.transport;
  requires spring.core;
  requires slf4j.api;
  requires com.google.guice;

  provides com.google.inject.Module with SpringServiceModule;
}

