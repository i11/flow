package com.bobkevic.flow.services.http;

import com.bobkevic.flow.services.ImmutableStyle;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.util.Map;
import java.util.Optional;
import org.immutables.value.Value;

@Value.Immutable
@ImmutableStyle
@JsonSerialize
@JsonDeserialize(builder = HttpBuilder.class)
public interface Http {

  @Value.Default
  default String method() {
    return "GET";
  }

  @Value.Default
  default String uri() {
    return "/";
  }

  Optional<String> body();

  Optional<String> accept();

  Optional<String> contentType();

  Optional<Map<String, String>> parameters();

//  HttpServletRequest httpServletRequest();
//
//  RequestMapping mapping();
//
//  Optional<List<RequestParameter>> parameters();
//
//  Optional<RequestBody> body();

}
