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
        String username = "octocat";
        GitHubUser owner = new GitHubUser("octocat");
        boolean fork = false;

        List<GitHubBranch> branches = Arrays.asList(
                new GitHubBranch("master", new GitHubCommit("master-commit-sha")),
                new GitHubBranch("develop", new GitHubCommit("develop-commit-sha"))
        );

        GitHubRepository mockRepo = new GitHubRepository("repository-name", owner, fork, branches);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.just(mockRepo));

        GitHubService service = new GitHubService(webClient);

        Flux<GitHubRepository> result = service.listUserRepositories(username);

        assertNotNull(result);
        StepVerifier.create(result)
                .expectNextMatches(repo ->
                        repo.name().equals("repository-name") &&
                                repo.owner().equals(owner) &&
                                !repo.fork() &&
                                repo.branches().equals(branches)
                )
                .verifyComplete();

        verify(webClient).get();
    }



    @Test
    void testListUserRepositoriesHandlesWebClientError() {
        String username = "octocat";
        WebClientResponseException notFoundException = WebClientResponseException.create(
                404, "Not Found", HttpHeaders.EMPTY, null, null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.error(notFoundException));

        GitHubService service = new GitHubService(webClient);

        StepVerifier.create(service.listUserRepositories(username))
                .expectError(WebClientResponseException.NotFound.class)
                .verify();
    }

    @Test
    void testListUserRepositoriesWithNoRepos() {
        String username = "newuser";
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.empty());

        GitHubService service = new GitHubService(webClient);

        Flux<GitHubRepository> result = service.listUserRepositories(username);

        StepVerifier.create(result)
                .expectNextCount(0)
                .verifyComplete();
    }

}
