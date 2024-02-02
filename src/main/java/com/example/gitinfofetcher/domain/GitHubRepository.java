package com.example.gitinfofetcher.domain;

import java.util.List;

public record GitHubRepository(String name, GitHubUser owner, boolean fork, List<GitHubBranch> branches) {

    @Override
    public String name() {
        return name;
    }
}
