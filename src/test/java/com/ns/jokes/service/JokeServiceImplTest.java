package com.ns.jokes.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.EvictingQueue;
import com.ns.jokes.dto.JokeDTO;
import com.ns.jokes.model.Joke;
import com.ns.jokes.model.JokeFlag;
import com.ns.jokes.model.JokesResponseBody;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class JokeServiceImplTest {

    public static MockWebServer mockBackEnd;
    public JokesServiceImpl jokeService;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @BeforeAll
    public static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    public static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    public void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        jokeService = new JokesServiceImpl(WebClient.builder().baseUrl(baseUrl).build(), Optional.of(EvictingQueue.create(10)));
    }


    private Joke generateJokes(int length, boolean safe, boolean explicit, boolean sexist) {
        String content = RandomStringUtils.random(length, true, false);
        Joke joke = new Joke();
        joke.setJoke(content);
        joke.setSafe(safe);
        joke.setFlags(Map.of(JokeFlag.EXPLICIT, explicit, JokeFlag.SEXIST, sexist));
        return joke;
    }


    @Test
    public void testSuccess() throws Exception{
        var joke1 = generateJokes(5, true, false, false);
        var joke2 = generateJokes(5, false, false, true);
        var joke3 = generateJokes(7, true, false, false);
        var joke4 = generateJokes(3, true, true, false);

        JokesResponseBody jokesResponseBody = new JokesResponseBody();
        jokesResponseBody.setJokes(List.of(joke1, joke2, joke3, joke4));
        jokesResponseBody.setAmount(3);
        jokesResponseBody.setError(false);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(jokesResponseBody))
                .addHeader("Content-Type", "application/json"));

        Mono<JokeDTO> jokeMono = jokeService.fetchJoke();

        StepVerifier.create(jokeMono)
                .expectNextMatches(joke -> joke.getRandomJoke()
                        .equals(joke1.getJoke()))
                .verifyComplete();

    }

    @Test
    public void testSuccessEmpty() throws Exception{
        var joke1 = generateJokes(5, false, false, false);
        var joke2 = generateJokes(5, false, false, false);
        var joke3 = generateJokes(7, false, false, false);

        JokesResponseBody jokesResponseBody = new JokesResponseBody();
        jokesResponseBody.setJokes(List.of(joke1, joke2, joke3));
        jokesResponseBody.setAmount(3);
        jokesResponseBody.setError(false);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(jokesResponseBody))
                .addHeader("Content-Type", "application/json"));

        Mono<JokeDTO> jokeMono = jokeService.fetchJoke();

        StepVerifier.create(jokeMono)
                .expectNextCount(0)
                .verifyComplete();

    }

    @Test
    public void testSuccessCache() throws Exception{
        var joke1 = generateJokes(5, true, false, false);
        var joke2 = generateJokes(5, false, false, false);
        var joke3 = generateJokes(7, true, false, false);

        JokesResponseBody jokesResponseBody = new JokesResponseBody();
        jokesResponseBody.setJokes(List.of(joke1, joke2, joke3));
        jokesResponseBody.setAmount(3);
        jokesResponseBody.setError(false);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(jokesResponseBody))
                .addHeader("Content-Type", "application/json"));

        Mono<JokeDTO> jokeMono = jokeService.fetchJoke();

        StepVerifier.create(jokeMono)
                .expectNextMatches(joke -> joke.getRandomJoke()
                        .equals(joke1.getJoke()))
                .verifyComplete();

        var error = new MockResponse().setResponseCode(500);

        mockBackEnd.enqueue(error);
        mockBackEnd.enqueue(error);
        mockBackEnd.enqueue(error);
        mockBackEnd.enqueue(error);

        jokeMono = jokeService.fetchJoke();

        StepVerifier.create(jokeMono)
                .expectNextMatches(joke -> joke.getRandomJoke()
                        .equals(joke1.getJoke()))
                .verifyComplete();
    }


    @Test
    public void testErrorResponse() throws Exception {

        JokesResponseBody jokesResponseBody = new JokesResponseBody();
        jokesResponseBody.setError(true);

       var error = new MockResponse()
               .setBody(objectMapper.writeValueAsString(jokesResponseBody))
               .addHeader("Content-Type", "application/json");

       mockBackEnd.enqueue(error);
       mockBackEnd.enqueue(error);
       mockBackEnd.enqueue(error);
       mockBackEnd.enqueue(error);

        Mono<JokeDTO> jokeMono = jokeService.fetchJoke();

        StepVerifier.create(jokeMono)
                .expectNextCount(0)
                .verifyComplete();
    }

}
