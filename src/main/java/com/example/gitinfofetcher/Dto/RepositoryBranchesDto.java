package com.example.gitinfofetcher.Dto;

import com.example.gitinfofetcher.domain.GitHubBranch;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.util.List;


public class RepositoryBranchesDto {
    private String repositoryName;
    private String ownerLogin;
    private List<GitHubBranch> branches;


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

    public void setRepositoryName(String repositoryName) {
        this.repositoryName = repositoryName;
    }

    public void setOwnerLogin(String ownerLogin) {
        this.ownerLogin = ownerLogin;
    }

    public void setBranches(List<GitHubBranch> branches) {
        this.branches = branches;
    }
}

