package com.example.gitinfofetcher.controller;

// ... (other imports) ...

import com.example.gitinfofetcher.TestConfig;
import com.example.gitinfofetcher.service.GitHubService;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WebFluxTest(controllers = GitHubController.class)
@Import({GitHubService.class, TestConfig.class})
@WireMockTest(httpPort = 8089)
public class GitHubControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GitHubService gitHubService;

    @BeforeEach
    public void setUp() {
        stubFor(get(urlPathMatching("/users/.*/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"message\": \"Not Found\", \"documentation_url\": \"https://docs.github.com/rest/repos/repos#list-repositories-for-a-user\" }")));
    }

    @Test
    public void userRepositoriesNotFoundTest() {
        webTestClient.get().uri("/api/github/users/nonexistentuser/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Not Found");
    }

}
