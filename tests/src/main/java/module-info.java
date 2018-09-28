module com.bobkevic.flow.tests {

  requires com.bobkevic.flow.services;
  requires com.bobkevic.flow.http.spring;
  requires com.google.guice;
  requires java.sql;

  uses com.bobkevic.flow.modules.SpringServiceModule;
}
