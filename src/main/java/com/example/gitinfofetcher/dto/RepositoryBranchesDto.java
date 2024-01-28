package com.example.gitinfofetcher.dto;

import com.example.gitinfofetcher.domain.GitHubBranch;
import java.util.List;

public record RepositoryBranchesDto(String repositoryName, String ownerLogin, List<GitHubBranch> branches) {
}
