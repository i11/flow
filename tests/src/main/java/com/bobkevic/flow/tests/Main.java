package com.bobkevic.flow.tests;

import static com.bobkevic.flow.services.ConfigurationModule.binding;

import com.bobkevic.flow.http.spring.Server;
import com.bobkevic.flow.modules.SpringServiceModule;
import com.bobkevic.flow.modules.annotations.SpringPublisherExecutor;
import com.bobkevic.flow.modules.annotations.SpringPublisherQueue;
import com.bobkevic.flow.services.PublisherSupplier;
import com.bobkevic.flow.services.ServiceResolver;
import com.bobkevic.flow.services.SubscriberSupplier;
import com.bobkevic.flow.services.http.Http;
import com.bobkevic.flow.services.http.HttpBuilder;
import com.google.inject.Key;
import com.google.inject.Module;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

  public static void main(final String... args) {
    final List<Module> configurationModules = binding(Server.class, new Server("localhost", 8080))
        .and(Key.get(ExecutorService.class, SpringPublisherExecutor.class),
            Executors.newCachedThreadPool())
        .and(Key.get(SpringServiceModule.QUEUE_TYPE_LITERAL, SpringPublisherQueue.class),
            new ArrayBlockingQueue<>(1))
        .collect();
    final ServiceResolver serviceResolver = new ServiceResolver(configurationModules);
    final Set<PublisherSupplier<Http>> publisherSuppliers = serviceResolver.resolve(
        SpringServiceModule.PUBLISHER_SUPPLIER_TYPE_LITERAL);
    final Set<SubscriberSupplier<Http>> subscriberSuppliers = serviceResolver.resolve(
        SpringServiceModule.SUBSCRIBER_SUPPLIER_TYPE_LITERAL);

    final Http http = HttpBuilder.builder()
        .uri("/")
        .method("GET")
        .build();

    publisherSuppliers.stream()
        .map(PublisherSupplier::get)
        .forEach(publisher -> subscriberSuppliers.stream()
            .map(supplier -> supplier.apply(http))
            .forEach(publisher::subscribe));

  }
}
