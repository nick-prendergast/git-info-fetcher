package com.example.gitinfofetcher.controller;

import com.example.gitinfofetcher.TestConfig;
import com.example.gitinfofetcher.config.WebFluxErrorHandlingConfig;
import com.example.gitinfofetcher.service.GitHubService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@WebFluxTest(controllers = GitHubController.class)
@Import({GitHubService.class, TestConfig.class, WebFluxErrorHandlingConfig.class})
@WireMockTest(httpPort = 8089)
public class GitHubControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private GitHubService gitHubService;

    @BeforeEach
    public void setUp() {
        stubForRepositoryBranches("git-consortium", "git-consortium.json");
        stubForRepositoryBranches("hello-worId", "hello-worId.json");
        stubForRepositoryBranches("Hello-World", "Hello-World.json");
        stubForRepositoryBranches("octocat.github.io", "octocat.github.io.json");
        stubForRepositoryBranches("Spoon-Knife", "Spoon-Knife.json");
        stubForRepositoryBranches("test-repo1", "test-repo1.json");

        //stub for userRepositoriesFoundTest
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/users/octocat/repos"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/repos.json")));

        //stub for userRepositoriesNotFoundTest
        stubFor(get(urlPathMatching("/users/nonexistentuser/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"message\": \"Not Found\", \"documentation_url\": \"https://docs.github.com/rest/repos/repos#list-repositories-for-a-user\" }")));

        //stub for whenAcceptHeaderIsXmlThenReceiveNotAcceptableStatus
        stubFor(get(urlPathMatching("/api/github/users/xmltest/repos"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(415)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/xml-error.json")));

    }

    private void stubForRepositoryBranches(String repositoryName, String jsonFileName) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/" + repositoryName + "/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/" + jsonFileName)));
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
    public void userRepositoriesFoundTest() {
        String jsonContent;
        try {
            ClassPathResource resource = new ClassPathResource("__files/json/result.json");
            jsonContent = Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file for test", e);
        }

        webTestClient.get().uri("/api/github/users/octocat/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(jsonContent);
    }

    @Test
    public void whenAcceptHeaderIsXmlThenReceiveNotAcceptableStatus() {
         webTestClient.get().uri("/api/github/users/xmltest/repos")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_ACCEPTABLE.value())
                .jsonPath("$.Message").isEqualTo("The requested media type is not supported")
                .returnResult();
    }

}
