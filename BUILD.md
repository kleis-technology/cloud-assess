# How to build Cloud Assess

The build procedure is work in progress. 

## GitHub Access Token

Create a [Github personal access token (classic)](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) 
with the permission `read:packages` to download packages from GitHub Package Registry.

Then, set the environment variables
```bash
export GITHUB_ACTOR=<username>
export GITHUB_TOKEN=<token>
```

## Build and run on your machine

To build the server, simply run a gradle build.
```bash
./gradlew build
```

The following command starts the server.
```bash
./gradlew bootRun
```

## Docker build

```bash
docker buildx build --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version> .
docker buildx build --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest .
```
