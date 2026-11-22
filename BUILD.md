# Build

The image runs `lcaac serve` on the trusted library.
The version of lcaac is set by the build argument `LCAAC_VERSION` in the `Dockerfile`.

## Requirements

On GitHub
- access to cloud assess repository
- a personal access token with the permission `write:packages` (and `read:packages`)

## Procedure

Build and test the image locally
```bash
docker build -t cloud-assess-app:dev .
docker run -d --name cloud-assess -p 8080:8080 cloud-assess-app:dev
python3 tests/smoke_test.py http://localhost:8080
docker rm -f cloud-assess
```

Environment variables
- `GITHUB_ACTOR` contains your GitHub username.
- `GITHUB_TOKEN` contains your personal access token with the relevant permissions.

Login on ghcr
```bash
docker login ghcr.io -u $GITHUB_ACTOR
```

Docker build and push to GitHub packages.
```bash
 docker buildx build --push --platform=linux/arm64,linux/amd64 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:<version> .
 docker buildx build --push --platform=linux/arm64,linux/amd64 --tag ghcr.io/kleis-technology/cloud-assess/cloud-assess-app:latest .
```
