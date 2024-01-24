package com.example.gitinfofetcher.domain;


public class GitHubRepository {
    private String name;
    private GitHubUser owner;
    private boolean fork;

    public String getName() {
        return name;
    }

    public GitHubUser getOwner() {
        return owner;
    }

    public boolean isFork() {
        return fork;
    }
}
