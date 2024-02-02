package com.example.gitinfofetcher.service;

import com.example.gitinfofetcher.domain.GitHubBranch;
import com.example.gitinfofetcher.domain.GitHubCommit;
import com.example.gitinfofetcher.domain.GitHubRepository;
import com.example.gitinfofetcher.domain.GitHubUser;
import com.example.gitinfofetcher.dto.RepositoryBranchesDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    public void testListUserRepositoriesWithBranches() {
        String username = "octocat";
        GitHubUser owner = new GitHubUser(username);
        GitHubRepository repo1 = new GitHubRepository("repo1", owner, false, null);
        List<GitHubBranch> branchesForRepo1 = Arrays.asList(
                new GitHubBranch("master", new GitHubCommit("sha-master-repo1")),
                new GitHubBranch("develop", new GitHubCommit("sha-develop-repo1"))
        );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Mock the response for the repositories list
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.just(repo1));

        // Mock the response for the branches of repo1
        when(requestHeadersUriSpec.uri("/repos/{owner}/{repo}/branches", owner.login(), repo1.name())).thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToFlux(GitHubBranch.class)).thenReturn(Flux.fromIterable(branchesForRepo1));

        GitHubService service = new GitHubService(webClient);

        Flux<RepositoryBranchesDto> result = service.listUserRepositoriesWithBranches(username);

        assertNotNull(result);
        StepVerifier.create(result)
                .expectNextMatches(repoBranchesDto ->
                        repoBranchesDto.repositoryName().equals("repo1") &&
                                repoBranchesDto.ownerLogin().equals(username) &&
                                repoBranchesDto.branches().size() == 2 &&
                                repoBranchesDto.branches().containsAll(branchesForRepo1)
                )
                .verifyComplete();

        // Verify webClient calls
        verify(webClient, times(2)).get(); // Assuming one call for repos list and one for branches list
    }

    @Test
    void testListUserRepositoriesWithNoRepos() {
        String username = "newuser";
        // Setup WebClient mocks
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        // Simulate an empty list of repositories
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.empty());

        GitHubService service = new GitHubService(webClient);

        // Call the method under test
        Flux<RepositoryBranchesDto> result = service.listUserRepositoriesWithBranches(username);

        // Verify the result
        StepVerifier.create(result)
                .expectNextCount(0) // Expect no items since there are no repositories
                .verifyComplete(); // Ensure the flux completes successfully

        verify(webClient).get();
    }

    @Test
    void testListUserRepositories_ErrorFetchingRepositories() {
        String username = "octocat";
        WebClientResponseException notFoundException = WebClientResponseException.create(
                404, "Not Found", null, null, null);

        // simulate an HTTP 404 error for fetching repos
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.error(notFoundException));

        // Call the method under test
        GitHubService service = new GitHubService(webClient);
        Flux<RepositoryBranchesDto> result = service.listUserRepositoriesWithBranches(username);

        // Use StepVerifier to check that the correct error is propagated
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void testListUserRepositoriesWithBranches_ErrorFetchingBranches() {
        String username = "octocat";
        GitHubRepository repo = new GitHubRepository("repo1", new GitHubUser(username), false, null);
        WebClientResponseException exception = WebClientResponseException.create(
                404, "Not Found", null, null, null);

        // Mock the WebClient to simulate a successful fetch of repositories
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.just(repo));

        // Then simulate an HTTP 404 error for fetching branches
        when(requestHeadersUriSpec.uri("/repos/{owner}/{repo}/branches", username, repo.name())).thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToFlux(GitHubBranch.class)).thenReturn(Flux.error(exception));

        // Call the method under test
        GitHubService service = new GitHubService(webClient);
        Flux<RepositoryBranchesDto> result = service.listUserRepositoriesWithBranches(username);

        // Use StepVerifier to check that the correct error is propagated
        StepVerifier.create(result)
                .expectErrorMatches(throwable -> throwable instanceof WebClientResponseException &&
                        ((WebClientResponseException) throwable).getStatusCode().value() == 404)
                .verify();
    }

    @Test
    void testListUserRepositoriesWithBranches_NoBranches() {
        String username = "octocat";
        GitHubRepository repo = new GitHubRepository("repo1", new GitHubUser(username), false, null);

        // Mock the WebClient to simulate a successful fetch of repositories
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("/users/{username}/repos", username)).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(GitHubRepository.class)).thenReturn(Flux.just(repo));

        // Simulate an empty list of branches for the fetched repository
        when(requestHeadersUriSpec.uri("/repos/{owner}/{repo}/branches", username, repo.name())).thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToFlux(GitHubBranch.class)).thenReturn(Flux.empty());

        // Call the method under test
        GitHubService service = new GitHubService(webClient);
        Flux<RepositoryBranchesDto> result = service.listUserRepositoriesWithBranches(username);

        StepVerifier.create(result)
                .expectNextMatches(dto -> dto.repositoryName().equals(repo.name()) &&
                        dto.ownerLogin().equals(repo.owner().login()) &&
                        dto.branches().isEmpty()) // Expect the branches list to be empty
                .verifyComplete();

    }

}
