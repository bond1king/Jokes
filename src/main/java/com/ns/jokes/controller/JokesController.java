package com.ns.jokes.controller;

import com.ns.jokes.dto.JokeDTO;
import com.ns.jokes.service.JokeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/jokes")
@RequiredArgsConstructor
public class JokesController {

    private final JokeService jokesService;

    @GetMapping
    Mono<ResponseEntity<JokeDTO>> fetchJoke() {
        return jokesService.fetchJoke()
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.status(HttpStatus.NO_CONTENT).build())));
    }

}
