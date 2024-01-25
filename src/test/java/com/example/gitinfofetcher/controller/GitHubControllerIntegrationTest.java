package com.example.gitinfofetcher.controller;

import com.example.gitinfofetcher.domain.GitHubBranch;
import com.example.gitinfofetcher.domain.GitHubCommit;
import com.example.gitinfofetcher.domain.GitHubRepository;
import com.example.gitinfofetcher.domain.GitHubUser;
import com.example.gitinfofetcher.service.GitHubService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest
public class GitHubControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private GitHubService gitHubService;

    @Test
    void testGetUserRepos() {
        // Given
        String username = "octocat";
        GitHubUser owner = new GitHubUser("octocat");

        GitHubCommit masterCommit = new GitHubCommit("master-commit-sha-here");
        GitHubCommit patchCommit = new GitHubCommit("patch-commit-sha-here");
        GitHubCommit testCommit = new GitHubCommit("test-commit-sha-here");

        GitHubBranch masterBranch = new GitHubBranch("master", masterCommit);
        GitHubBranch patchBranch = new GitHubBranch("octocat-patch-1", patchCommit);
        GitHubBranch testBranch = new GitHubBranch("test", testCommit);

        List<GitHubBranch> repo1Branches = Arrays.asList(masterBranch, patchBranch, testBranch);
        GitHubRepository repo1 = new GitHubRepository("Hello-World", owner, false, repo1Branches);

        List<GitHubBranch> repo2Branches = Collections.singletonList(masterBranch);
        GitHubRepository repo2 = new GitHubRepository("git-consortium", owner, false, repo2Branches);

        when(gitHubService.listUserRepositories(username)).thenReturn(Flux.just(repo1, repo2));
        when(gitHubService.getRepositoryBranches(eq("octocat"), eq("Hello-World")))
                .thenReturn(Flux.fromIterable(repo1Branches));
        when(gitHubService.getRepositoryBranches(eq("octocat"), eq("git-consortium")))
                .thenReturn(Flux.fromIterable(repo2Branches));
        // When
        webTestClient.get().uri("/api/github/users/{username}/repos", username)
                .exchange()
                // Then
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].repositoryName").isEqualTo("Hello-World")
                .jsonPath("$[0].ownerLogin").isEqualTo("octocat")
                .jsonPath("$[0].branches[0].name").isEqualTo("master")
                .jsonPath("$[0].branches[0].commit.sha").isEqualTo("master-commit-sha-here")
                .jsonPath("$[1].repositoryName").isEqualTo("git-consortium")
                .jsonPath("$[1].ownerLogin").isEqualTo("octocat")
                .jsonPath("$[1].branches[0].name").isEqualTo("master")
                .jsonPath("$[1].branches[0].commit.sha").isEqualTo("master-commit-sha-here");
    }

}

