# Cloud Assess

## Requirements 

Request an to access to ch.kleis.lcaac.core and ch.kleis.lcaac.grammar dependencies

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

### Build and publish Docker image

Build the project

```bash
./gradlew clean bootJar
```

Build the docker image

```bash
docker build . -t cloud-assess-app:<version> --build-arg GITHUB_ACTOR=$GITHUB_ACTOR --build-arg GITHUB_TOKEN=$GITHUB_TOKEN
```

Login to Github package manager

```bash
echo $GITHUB_TOKEN | docker login ghcr.io --username $GITHUB_ACTOR --password-stdin
```

Tag and push images

```bash
docker tag cloud-assess-app:<version> ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version>
docker tag cloud-assess-app:latest ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest
```

```
docker push ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version>
docker push ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest
```



### With local access to lca-plugin repository

Need to install core and grammar libraries locally first.
Check out <https://github.com/kleis-technology/lca-plugin.git>

```bash
cd lca-plugin
./gradlew :grammar:publishToMavenLocal :core:publishToMavenLocal
```

Then in the repository `cloud-assess`
```bash
cd cloud-assess
./gradlew build
```



