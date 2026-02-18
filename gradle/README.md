# Verification metadata

## Adding or Updating a Library 

If you add a new dependency to build.gradle or bump a version, the build will fail because the new library isn't in our "allow-list" yet. To update the metadata, run:

```Bash
./gradlew --write-verification-metadata pgp,sha256 --export-keys
```

## Cleaning up the Metadata

Over time, the XML file can get bloated with old versions. You can prune unused entries by running:

```Bash
./gradlew --write-verification-metadata pgp,sha256 --export-keys --dry-run
```


## Sources:
- https://docs.gradle.org/current/userguide/dependency_verification.html