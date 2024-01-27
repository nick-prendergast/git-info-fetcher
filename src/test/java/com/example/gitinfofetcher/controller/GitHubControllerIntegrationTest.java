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

    @BeforeEach
    public void setUp() {
        setupStubForRepositoryBranches();
        setupStubForUserRepositories();
    }

    private void setupStubForRepositoryBranches() {
        String[] repositories = {"git-consortium", "hello-worId", "Hello-World", "octocat.github.io", "Spoon-Knife", "test-repo1"};
        for (String repo : repositories) {
            stubForRepositoryBranches(repo, repo + ".json");
        }
    }

    private void setupStubForUserRepositories() {
        stubForUserRepositoryFound();
        stubForUserRepositoryNotFound();
        stubForUserRepositoryWithXmlHeader();
        stubForUserRepositoryNotExisting();
    }

    private static void stubForUserRepositoryNotExisting() {
        stubFor(WireMock.get(WireMock.urlPathMatching("/users/some-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));
    }

    private void stubForRepositoryBranches(String repositoryName, String jsonFileName) {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/" + repositoryName + "/branches"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/" + jsonFileName)));
    }

    private void stubForUserRepositoryFound() {
        WireMock.stubFor(WireMock.get(WireMock.urlPathMatching("/users/octocat/repos"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/repos.json")));
    }

    private void stubForUserRepositoryNotFound() {
        stubFor(get(urlPathMatching("/users/nonexistentuser/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"message\": \"Not Found\", \"documentation_url\": \"https://docs.github.com/rest/repos/repos#list-repositories-for-a-user\" }")));
    }

    private void stubForUserRepositoryWithXmlHeader() {
        stubFor(get(urlPathMatching("/api/github/users/xmltest/repos"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(415)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/xml-error.json")));
    }

    @Test
    public void shouldReturnNotFoundForNonExistentUserRepositories() {
        webTestClient.get().uri("/api/github/users/nonexistentuser/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.message").isEqualTo("Not Found");
    }

    @Test
    public void shouldReturnRepositoriesForExistingUser() {
        String jsonContent = readJsonFromFile("__files/json/result.json");
        webTestClient.get().uri("/api/github/users/octocat/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(jsonContent);
    }

    @Test
    public void shouldReturnNotAcceptableForXmlHeaderRequest() {
        webTestClient.get().uri("/api/github/users/xmltest/repos")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_ACCEPTABLE.value())
                .jsonPath("$.Message").isEqualTo("The requested media type is not supported");
    }

    @Test
    public void userExistsButHasNoRepositoriesTest() {
        webTestClient.get().uri("/api/github/users/some-user/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");
    }

    private String readJsonFromFile(String path) {
        try {
            ClassPathResource resource = new ClassPathResource(path);
            return Files.readString(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read JSON file for test", e);
        }
    }
}
