# Build

## Requirements

On GitHub
- access to cloud assess repository
- a personal access token with the permission `write:packages` (and `read:packages`)

## Procedure

Environment variables
- `GITHUB_ACTOR` contains your GitHub username.
- `GITHUB_TOKEN` contains your personal access token with the relevant permissions.

Login on ghcr
```bash
docker login ghcr.io -u $GITHUB_ACTOR
```

Docker build and push to GitHub packages.
```bash
 docker buildx build --push --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:1.6.6 .
 docker buildx build --push --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest .
```
