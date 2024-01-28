package com.example.gitinfofetcher.service;

import com.example.gitinfofetcher.domain.GitHubBranch;
import com.example.gitinfofetcher.domain.GitHubRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Service
public class GitHubService {

    private static final Logger logger = LoggerFactory.getLogger(GitHubService.class);
    private final WebClient webClient;

    public GitHubService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<GitHubRepository> listUserRepositories(String username) {
        logger.info("Fetching repositories for user: {}", username);
        return webClient.get()
                .uri("/users/{username}/repos", username)
                .retrieve()
                .bodyToFlux(GitHubRepository.class)
                .filter(repo -> !repo.fork())
                .doOnNext(repo -> logger.debug("Received repo: {}", repo.name()))
                .doOnError(e -> logger.error("Error fetching repositories for user: {}", username, e));
    }

    public Flux<GitHubBranch> getRepositoryBranches(String owner, String repoName) {
        logger.info("Fetching branches for repository: {}/{}", owner, repoName);
        return webClient.get()
                .uri("/repos/{owner}/{repo}/branches", owner, repoName)
                .retrieve()
                .bodyToFlux(GitHubBranch.class)
                .doOnNext(branch -> logger.info("Received branch: {} in repo: {}/{}", branch.name(), owner, repoName))
                .doOnError(e -> logger.error("Error fetching branches for repository: {}/{}", owner, repoName, e));
    }
}
