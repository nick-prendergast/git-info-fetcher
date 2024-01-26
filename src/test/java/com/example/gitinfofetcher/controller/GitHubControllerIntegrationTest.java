package com.example.gitinfofetcher.controller;

import com.example.gitinfofetcher.TestConfig;
import com.example.gitinfofetcher.service.GitHubService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;

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

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/git-consortium/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/git-consortium.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/hello-worId/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/hello-worId.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/Hello-World/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/Hello-World.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/octocat.github.io/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/octocat.github.io.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/Spoon-Knife/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/Spoon-Knife.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/test-repo1/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/test-repo1.json"))); // Change the file name as needed

        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/users/octocat/repos"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/repos.json"))); // Change the file name as needed


        stubFor(get(urlPathMatching("/users/nonexistentuser/repos"))
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

    @Test
    public void userRepositoriesFoundTest() throws IOException {
        ClassPathResource resource = new ClassPathResource("__files/json/result.json");
        String jsonContent = Files.readString(resource.getFile().toPath());

        webTestClient.get().uri("/api/github/users/octocat/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(jsonContent);
    }

}
