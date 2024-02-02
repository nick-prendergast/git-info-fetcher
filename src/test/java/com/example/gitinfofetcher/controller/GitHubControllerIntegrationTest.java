package com.example.gitinfofetcher.controller;

import com.example.gitinfofetcher.WebClientTestConfig;
import com.example.gitinfofetcher.config.WebFluxErrorHandlingConfig;
import com.example.gitinfofetcher.service.GitHubService;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;

@WebFluxTest(controllers = GitHubController.class)
@Import({GitHubService.class, WebClientTestConfig.class, WebFluxErrorHandlingConfig.class})
@WireMockTest(httpPort = 8089)
public class GitHubControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void shouldReturnNotFoundForNonExistentUserRepositories() {
        stubForWhenUserDoesNotExist();

        webTestClient.get().uri("/api/github/users/nonexistentuser/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.Message").isEqualTo("Not Found");

        verify(WireMock.getRequestedFor(urlPathMatching("/users/nonexistentuser/repos")));
    }

    private static void stubForWhenUserDoesNotExist() {
        stubFor(get(urlPathMatching("/users/nonexistentuser/repos"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{ \"message\": \"Not Found\", \"documentation_url\": " +
                                "\"https://docs.github.com/rest/repos/repos#list-repositories-for-a-user\" }")));
    }

    @Test
    public void shouldReturnRepositoriesForExistingUser() {
        String[] repositories = {"git-consortium", "hello-worId", "Hello-World", "octocat.github.io",
                "Spoon-Knife", "test-repo1"};
        for (String repo : repositories) {
            stubForRepositoryBranches(repo, repo + ".json");
        }

        stubForUserRepos();

        String jsonContentResponse = readJsonFromFile("__files/json/response/octocat-response.json");
        webTestClient.get().uri("/api/github/users/octocat/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json(jsonContentResponse);

        verify(WireMock.getRequestedFor(urlPathMatching("/users/octocat/repos")));
    }

    private void stubForRepositoryBranches(String repositoryName, String jsonFileName) {
        stubFor(WireMock.get(WireMock.urlPathMatching("/repos/octocat/" + repositoryName + "/branches"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/branches/" + jsonFileName)));
    }

    private static void stubForUserRepos() {
        stubFor(WireMock.get(WireMock.urlPathMatching("/users/octocat/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/repos/repos.json")));
    }


    @Test
    public void shouldReturnNotAcceptableForXmlHeaderRequest() {
        stubForRequestWithXmlHeader();

        webTestClient.get().uri("/api/github/users/xmltest/repos")
                .accept(MediaType.APPLICATION_XML)
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_ACCEPTABLE)
                .expectBody()
                .jsonPath("$.status").isEqualTo(HttpStatus.NOT_ACCEPTABLE.value())
                .jsonPath("$.Message").isEqualTo("The requested media type is not supported");

        verify(0, getRequestedFor(urlPathMatching("/users/.+/repos")));
    }

    private static void stubForRequestWithXmlHeader() {
        stubFor(get(urlPathMatching("/api/github/users/xmltest/repos"))
                .withHeader("Accept", equalTo("application/xml"))
                .willReturn(aResponse()
                        .withStatus(415)
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("json/error/xml-error.json")));
    }

    @Test
    public void userExistsButHasNoRepositoriesTest() {
        stubForUserWithNoRepos();

        webTestClient.get().uri("/api/github/users/some-user/repos")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody().json("[]");

        verify(WireMock.getRequestedFor(urlPathMatching("/users/some-user/repos")));
    }

    private static void stubForUserWithNoRepos() {
        stubFor(WireMock.get(WireMock.urlPathMatching("/users/some-user/repos"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));
    }

    @Test
    public void testListUserRepositoriesWithEmptyUsername() {
        webTestClient.get().uri("/api/github/users//repos")
                .exchange()
                .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
                .expectBody().isEmpty();

        verify(0, anyRequestedFor(urlPathMatching("/api/github/users/.+/repos")));
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
