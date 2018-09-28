package com.bobkevic.flow.services.http;

public interface RequestMapping {

  default String path() {
    return "/";
  }

  default RequestMethod method() {
    return RequestMethod.GET;
  }

  default String[] headers() {
    return new String[]{};
  }

  default String[] consumes() {
    return new String[]{};
  }

  default String[] produces() {
    return new String[]{};
  }

}
