package com.ns.jokes.controller;

import com.ns.jokes.dto.JokeDTO;
import com.ns.jokes.service.JokeService;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SpringBootTest( webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JokesControllerTest {

    private WebTestClient webTestClient;
    private JokesController controller;
    private JokeService jokeService;

    @Before
    public void setUp() {
        jokeService = Mockito.mock(JokeService.class);
        controller = new JokesController(jokeService);
        webTestClient = WebTestClient.bindToController(controller).build();
    }


    @Test
    public void testSuccessResponse() {
        JokeDTO joke = new JokeDTO(1, "test joke");
        when(jokeService.fetchJoke()).thenReturn(Mono.just(joke));
        webTestClient
                .get()
                .uri("/api/jokes")
                .exchange()
                .expectStatus().isOk()
                .expectBody();

    }

    @Test
    public void testSuccessWithEmptyResponse() {
        when(jokeService.fetchJoke()).thenReturn(Mono.empty());
        webTestClient
                .get()
                .uri("/api/jokes")
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody();
    }

    @Test
    public void testFailureResponse() {
        when(jokeService.fetchJoke()).thenReturn(Mono.error(new RuntimeException("test error")));
        webTestClient
                .get()
                .uri("/api/jokes")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody();

    }

}
