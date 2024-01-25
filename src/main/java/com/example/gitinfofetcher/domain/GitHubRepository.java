package com.example.gitinfofetcher.domain;


import java.util.List;

public class GitHubRepository {
    private final String name;
    private final GitHubUser owner;
    private final boolean fork;

    private final List<GitHubBranch> branches;

    public String getName() {
        return name;
    }

    public GitHubUser getOwner() {
        return owner;
    }

    public boolean isFork() {
        return fork;
    }

    public List<GitHubBranch> getBranches() {
        return branches;
    }

    public GitHubRepository(String name, GitHubUser owner, boolean fork, List<GitHubBranch> branches) {
        this.name = name;
        this.owner = owner;
        this.fork = fork;
        this.branches = branches;
    }
}
