package com.bobkevic.flow.services.http;

public interface RequestParameter {

  default String name() {
    return "";
  }

  default boolean required() {
    return true;
  }

  default String defaultValue() {
    return "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n";
  }

}
