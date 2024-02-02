# Git-Info-Fetcher Service

Git-Info-Fetcher is a Spring Boot application designed to interact with GitHub's API to fetch information about a user's GitHub repositories, focusing on repositories that are not forks. It provides details about each repository, including branch information and the latest commit SHA.

## Features

- List non-fork repositories of a given GitHub user.
- Provide repository name, owner login, and details of each branch including the last commit SHA.

## Running with Docker

Ensure Docker is installed on your machine. For installation instructions, refer to the [official Docker documentation](https://docs.docker.com/get-docker/).

### Steps to Run

1. **Clone the Repository**
    ```sh
    git clone https://github.com/nick-prendergast/git-info-fetcher.git
    cd git-info-fetcher
    ```
   
2. **Build the Project with Maven**

   ```sh
   mvn clean package
   ```

3. **Build the Docker Image**
    ```sh
    docker build -t git-info-fetcher .
    ```

4. **Run the Docker Container**
    ```sh
    docker run -p 8080:8080 git-info-fetcher
    ```

   The application will be accessible at `http://localhost:8080`.

## Using the API

To use the service, make an HTTP GET request to the `/api/github/users/{username}/repos` endpoint with the header “Accept: application/json”. Replace `{username}` with the GitHub username.

Example Request:
```http
GET /api/github/users/octocat/repos
Accept: application/json
```

## Swagger UI

This service includes a Swagger UI, which provides interactive documentation for the APIs provided by the service. Once the application is running, you can access the Swagger UI at:

[http://localhost:8080/webjars/swagger-ui/index.html](http://localhost:8080/webjars/swagger-ui/index.html)

This will give you an overview of all the REST endpoints, their expected input, output, and you can also directly test API calls from the UI.


