# Cloud Assess

## Temporary build

Need to install core and grammar libraries locally first.
Check out <https://github.com/kleis-technology/lca-plugin.git>, branch `feature/antlr-grammar`.

```bash
cd lca-plugin
./gradlew :grammar:publishToMavenLocal :core:publishToMavenLocal
```

Then in the repository `cloud-assess`
```bash
cd cloud-assess
./gradlew build
```
