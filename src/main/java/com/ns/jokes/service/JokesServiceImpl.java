package com.ns.jokes.service;

import com.google.common.collect.EvictingQueue;
import com.ns.jokes.dto.JokeDTO;
import com.ns.jokes.model.Joke;
import com.ns.jokes.model.JokeFlag;
import com.ns.jokes.model.JokesResponseBody;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.Comparator;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JokesServiceImpl implements JokeService {

    private final WebClient webClient;
    private final Optional<EvictingQueue<Joke>> cache;

    @Override
    public Mono<JokeDTO> fetchJoke() {
        return this.queryJokesAPI()
                .map(JokesResponseBody::getJokes)
                .flatMapMany(Flux::fromIterable)
                .filter(joke -> joke.isSafe() && !joke.getFlags().get(JokeFlag.EXPLICIT) && !joke.getFlags().get(JokeFlag.SEXIST))
                .sort(Comparator.comparing(s -> s.getJoke().length()))
                .doOnNext(this::cacheJoke)
                .switchIfEmpty(Flux.defer(this::pollCache))
                .next()
                .map(joke -> new JokeDTO(joke.getId(), joke.getJoke()));
    }

    private Mono<JokesResponseBody> queryJokesAPI() {
       return webClient.get()
               .uri(uriBuilder -> uriBuilder.pathSegment("joke","Any")
                       .queryParam("type","single")
                       .queryParam("amount", "16")
                       .build())
                .retrieve()
                .bodyToMono(JokesResponseBody.class)
               .flatMap(body -> {
                   if (isErrorResponse(body)) {
                       return Mono.error(new WebClientResponseException(500, "Response has error true", null, null, null));
                   }
                   return Mono.just(body);
               })
                .doOnError(this::logErrorResponse)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2))
                        .filter(this::is5xxServerError))
                .onErrorReturn(JokesResponseBody.ERROR_RESPONSE);
    }

    private boolean isErrorResponse(JokesResponseBody body) {
        return body.isError();
    }
    private boolean is5xxServerError(Throwable throwable) {
        return throwable instanceof WebClientResponseException &&
                ((WebClientResponseException) throwable).getStatusCode().is5xxServerError();
    }

    private void logErrorResponse(Throwable throwable) {
        log.error("There was an error fetching jokes from the Jokes API - {}", throwable.getMessage());
    }

    private Flux<Joke> pollCache() {
        if (cache.isPresent()) {
            Joke joke = cache.get().peek();
            if ( joke != null) {
                return Flux.just(joke);
            }
        }
        return Flux.empty();
    }

    @Async("cacheThreadPool")
    private void cacheJoke(Joke joke) {
        log.debug("received joke: {}", joke);
        if (cache.isEmpty()) return;
        var queue = cache.get();
        if (joke != null) {
            queue.offer(joke);
        }
    }

}
