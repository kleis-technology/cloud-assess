# Cloud Assess

## Requirements

- ch.kleis.lcaac.core
- ch.kleis.lcaac.grammar

To request an access, please write to contact@kleis.ch

Then create a [Github personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic) with access to kleis lca-plugin private repository

Finally, set the environment variables

```bash
export GITHUB_ACTOR=<username>
export GITHUB_TOKEN=<token>
```

### Build

```bash
./gradlew build
```

### Run

```bash
./gradlew bootRun
```

### Build and publish docker images

#### Requirements

Docker desktop comes with a pre-configured builder. 

- Docker desktop ``brew install docker --cask``

If you installed Docker Engine manually, please refer to [buildx setup documentation](https://docs.docker.com/build/guide/multi-platform/)

Build the project

```bash
./gradlew clean bootJar
```

Login to Github package manager

```bash
echo $GITHUB_TOKEN | docker login ghcr.io --username $GITHUB_ACTOR --password-stdin
```

#### Build tag and push multi-platform images

```bash
docker buildx build --push --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version> .
docker buildx build --push --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN --platform=linux/arm64,linux/amd64,linux/amd64/v2,linux/arm/v7 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest .
```

Published packages are available here: https://github.com/kleis-technology/cloud-assess/pkgs/container/cloud-assess%2Fcloud-assess-app



