package com.ns.jokes.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JokeFlag {

    @JsonProperty("explicit") EXPLICIT,
    @JsonProperty("nsfw") NSFW,
    @JsonProperty("religious") RELIGIOUS,
    @JsonProperty("political") POLITICAL,
    @JsonProperty("racist") RACIST,
    @JsonProperty("sexist") SEXIST

}
