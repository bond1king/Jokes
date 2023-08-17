package com.ns.jokes.model;

import lombok.Data;

import java.util.Map;

@Data
public class Joke {
    String category;
    String type;
    String joke;
    Map<JokeFlag, Boolean> flags;
    Integer id;
    boolean safe;
    String lang;
}
