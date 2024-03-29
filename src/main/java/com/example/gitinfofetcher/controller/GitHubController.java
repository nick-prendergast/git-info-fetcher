package com.example.gitinfofetcher.controller;

import com.example.gitinfofetcher.dto.RepositoryBranchesDto;
import com.example.gitinfofetcher.service.GitHubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/github")
public class GitHubController {

    private static final Logger logger = LoggerFactory.getLogger(GitHubController.class);

    private final GitHubService gitHubService;

    public GitHubController(GitHubService gitHubService) {
        this.gitHubService = gitHubService;
    }

    @GetMapping("/users/{username}/repos")
    public Flux<RepositoryBranchesDto> listUserRepos(@PathVariable String username) {
        logger.info("Request received to list repositories with branches for user: {}", username);
        return gitHubService.listUserRepositoriesWithBranches(username);
    }
}
