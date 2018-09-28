module com.bobkevic.flow.services {
  uses com.google.inject.Module;

  requires static org.immutables.value;
  requires static com.fasterxml.jackson.databind;
  requires static com.fasterxml.jackson.annotation;

  requires com.google.guice;
  requires com.google.common;
  requires error.prone.annotations;
  requires jsr305;

  exports com.bobkevic.flow.services.http;
  exports com.bobkevic.flow.services;

}

