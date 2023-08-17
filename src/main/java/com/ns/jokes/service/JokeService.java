package com.ns.jokes.service;

import com.ns.jokes.dto.JokeDTO;
import reactor.core.publisher.Mono;

public interface JokeService {

    Mono<JokeDTO> fetchJoke();

}
