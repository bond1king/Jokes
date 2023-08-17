package com.ns.jokes.integration;

import com.ns.jokes.JokesAppApplication;
import com.ns.jokes.controller.JokesController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {JokesAppApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JokesApplicationIntegrationTest {

    @Autowired
    public JokesController jokesController;

    @Autowired
    private WebTestClient webTestClient;


    @Test
    public void testSuccess() {
        var response = this.webTestClient.get()
                .uri(uriBuilder -> uriBuilder.pathSegment("api","jokes").build())
                .exchange()
                .expectStatus().is2xxSuccessful()
                .expectBody();
    }

}
