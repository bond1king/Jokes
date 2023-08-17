package com.ns.jokes.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JokesResponseBody {
    boolean error;
    Integer amount;
    List<Joke> jokes;

    public static final JokesResponseBody ERROR_RESPONSE = new JokesResponseBody(true, 0, Collections.emptyList());

}
