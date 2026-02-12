---
---


* Contents
{:toc}

### Overview

This tool allows the user to specify a branch in a dist-git (cgit) repository and it can import it into internal Red Hat CEE GitLab. Note that it is possible to rerun the tool against the gitlab repository to regenerate the pom

The advantages over manually cloning within the PNC UI and hand-crafting the pom file are:

* All 'setup' functionality is self-contained into a single tool.
* The pom file is created with a unique GAV - specifically -
   * The groupId defaults to the wrapper build's groupId.
   * The artifactId defaults to the wrapped build's artifactId combined with the indicator `rpm` and branch name e.g. `guava-parent-rpm-jb-eap-8.0-rhel-9`
   * The version defaults to the original version (without the redhat suffix). Note in prior versions it was `1.0.0`.
   * Note in prior versions the prefix `org.jboss.pnc.rpm` was prepended to the wrapper build's groupId.
* The artifacts from the wrapped build are all included in the pom file. The tool interrogates PNC to find the last build and grab all the artifacts. Note that not all of the artifacts may be used by the spec file so it is possible to optimise further by hand-crafting at this point.

### Setup

This tool reuses the Bacon configuration file and so requires that you have a working [Bacon](https://project-ncl.github.io/bacon/) setup. The only addition is
for reqour:

```
  reqour:
    url: "https://reqour.pnc.engineering...."
```

You must be a member of `jboss-prod` so you have access to repositories created in CEE GitLab under the `pnc-workspace` group.

Brew access via Kerberos is also required **unless** you override both the Brew tag lookup to determine macros (via `--macros`) and the MEAD NVR lookup via `--gav` (or `--lastMeadBuild`).

This tool is tested and supported on **JDK 21**.

### Usage


Options:

```
Usage: rpm-importer [-hvV] [--overwrite] [--push] [--skip-sync] --branch=<branch> [-p=<configPath>] [--profile=<profile>] [--repository=<repository>]
                    --url=<url> [--macros=<String=String>]... [--gav=<gavOverride> --originalVersion=<originalVersionOverride>]

      --branch=<branch>     Branch in git repository
      --gav, --lastMeadBuild=<gavOverride>
                            Override the value found from last-mead-build. Accepts a Maven GAV with RH version.
  -h, --help                Show this help message and exit.
      --macros=<String=String>
                            Pass in a (comma separated) set of macros to use
      --originalVersion=<originalVersionOverride>
                            Supply the original version (without the RH version)
      --overwrite           Overwrites existing pom. Dangerous!
  -p, --configPath=<configPath>
                            Path to PNC configuration folder
      --profile=<profile>   PNC Configuration profile
      --push                Pushes changes to the remote repository. Will still commit
      --repository=<repository>
                            Skips cloning and uses existing repository
      --skip-sync           Skips any syncing and only clones the repository and performs the patching
      --url=<url>           External URL to git repository
  -v, --verbose             Verbose output
  -V, --version             Print version information and exit.
```


Typical usage:

```
java -jar target/rpm-importer-parent-<version>.jar --url=https://pkgs.devel...../git/rpms/<repository> --branch=jb-eap-8.1-rhel-9
```

Notes:

* Unless `--push` is supplied the tool will only commit changes locally and **not** push to the remote. It is highly recommended that the user checks the resulting `pom.xml` before _manually_ running any git push.
* Skipping repository syncing only makes sense if the repository has already been mirrored to GitLab. This might be the case if the user is switching between multiple branches or regenerating the pom.
* Using an existing locally cloned repository is useful for local debugging or regenerating the pom.
* It uses last-mead-build to retrieve the NVR and examine the Brew extra information and the typeinfo. If this typeinfo that contains a Maven GAV does not correspond to a valid type (which we have seen happen with Hibernate) then use the `--gav` (or `--lastMeadBuild`) to pass in a valid GAV from the build within PNC. It must be a GAV from a Red Hat build from PNC. When using this option the `--originalGAV` must also be supplied.
* If using `--gav`/`--lastMeadBuild` the GAV that is passed in **MUST** be the top level GAV in the build.
* It uses the following files from ETT when generating the POM:
  * `last-mead-build`
  * `mead-pkg-name`
  * `version-release-serial`
