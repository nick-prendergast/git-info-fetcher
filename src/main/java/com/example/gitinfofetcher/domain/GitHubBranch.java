package com.example.gitinfofetcher.domain;

public class GitHubBranch {
    private final String name;
    private final GitHubCommit commit;

    public GitHubBranch(String name, GitHubCommit commit) {
        this.name = name;
        this.commit = commit;
    }


    public String getName() {
        return name;
    }

    public GitHubCommit getCommit() {
        return commit;
    }
}
