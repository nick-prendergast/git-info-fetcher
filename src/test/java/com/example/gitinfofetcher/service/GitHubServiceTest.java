package com.example.gitinfofetcher.service;

import com.example.gitinfofetcher.domain.GitHubBranch;
import com.example.gitinfofetcher.domain.GitHubCommit;
import com.example.gitinfofetcher.domain.GitHubRepository;
import com.example.gitinfofetcher.domain.GitHubUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class GitHubServiceTest {

    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Test
    public void testListUserRepositories() {
        // Given
        String username = "octocat";
        GitHubUser owner = new GitHubUser("octocat");
        boolean fork = false;

        // Creating mock branches
        List<GitHubBranch> branches = Arrays.asList(
                new GitHubBranch("master", new GitHubCommit("master-commit-sha")),
                new GitHubBranch("develop", new GitHubCommit("develop-commit-sha"))
        );

        // Creating a mock repository with branches
        GitHubRepository mockRepo = new GitHubRepository("repository-name", owner, fork, branches);

        // Mocking the web client behavior
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.just(mockRepo));

        // Mocking the GitHubService
        GitHubService service = new GitHubService(webClient);

        // When
        Flux<GitHubRepository> result = service.listUserRepositories(username);

        // Then
        assertNotNull(result);
        StepVerifier.create(result)
                .expectNextMatches(repo ->
                        repo.getName().equals("repository-name") &&
                                repo.getOwner().equals(owner) &&
                                !repo.isFork() &&
                                repo.getBranches().equals(branches)
                )
                .verifyComplete();

        // Verify that the web client's get method was called
        verify(webClient).get();
    }



    @Test
    void testListUserRepositoriesHandlesWebClientError() {
        // Arrange
        String username = "octocat";
        WebClientResponseException notFoundException = WebClientResponseException.create(
                404, "Not Found", HttpHeaders.EMPTY, null, null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.error(notFoundException));

        GitHubService service = new GitHubService(webClient);

        // Act & Assert
        StepVerifier.create(service.listUserRepositories(username))
                .expectError(WebClientResponseException.NotFound.class)
                .verify();
    }

    @Test
    void testListUserRepositoriesWithNoRepos() {
        // Arrange
        String username = "newuser";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.empty());

        GitHubService service = new GitHubService(webClient);

        // Act
        Flux<GitHubRepository> result = service.listUserRepositories(username);

        // Assert
        StepVerifier.create(result)
                .expectNextCount(0) // expect no items
                .verifyComplete(); // verify that the flux completes
    }



}
