package com.example.gitinfofetcher.domain;

public class GitHubCommit {

    private String sha;

    public GitHubCommit(String sha) {
        this.sha = sha;
    }

    public String getSha() {
        return sha;
    }

    public GitHubCommit() {
    }

}
