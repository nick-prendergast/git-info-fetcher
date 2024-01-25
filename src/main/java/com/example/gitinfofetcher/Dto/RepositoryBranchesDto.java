package com.example.gitinfofetcher.Dto;

import com.example.gitinfofetcher.domain.GitHubBranch;

import java.util.List;


public class RepositoryBranchesDto {
    private final String repositoryName;
    private final String ownerLogin;
    private final List<GitHubBranch> branches;


    public RepositoryBranchesDto(String repositoryName, String ownerLogin, List<GitHubBranch> branches) {
        this.repositoryName = repositoryName;
        this.ownerLogin = ownerLogin;
        this.branches = branches;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getOwnerLogin() {
        return ownerLogin;
    }

    public List<GitHubBranch> getBranches() {
        return branches;
    }

}

