# rpm-importer

This project is to import repositories from dist-git to gitlab and patch the resulting repository (o a per branch basis) with a suitable pom for building RPMs in PNC.

Options:

```
Usage: rpm-importer [-hvV] [--overwrite] [--push] [--skip-sync] [--branch=<branch>] [-p=<configPath>] [--profile=<profile>] [--repository=<repository>]
                    --url=<url>

      --branch=<branch>     Branch in distgit repository
  -h, --help                Show this help message and exit.
      --overwrite           Overwrites existing pom. Dangerous!
  -p, --configPath=<configPath>
                            Path to PNC configuration folder
      --profile=<profile>   PNC Configuration profile
      --push                Pushes changes to the remote repository. Will still commit
      --repository=<repository>
                            Skips cloning and uses existing repository
      --skip-sync           Skips any syncing and only clones the repository and performs the patching
      --url=<url>           External URL to distgit repository
  -v, --verbose             Verbose output
  -V, --version             Print version information and exit.

```


Typical usage:

```
java -jar target/rpm-importer-parent-1.0.0-SNAPSHOT.jar --url=https://pkgs.devel...../git/rpms/<repository> --branch=jb-eap-8.1-rhel-9
```
