package com.kafkatemplate.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Status {
    
    @JsonProperty("ok")
    OK,
    
    @JsonProperty("fail")
    FAIL
}