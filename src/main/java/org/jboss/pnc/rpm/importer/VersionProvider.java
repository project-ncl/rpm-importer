package org.jboss.pnc.rpm.importer;

import org.commonjava.maven.ext.common.util.ManifestUtils;

import picocli.CommandLine;

public class VersionProvider implements CommandLine.IVersionProvider {

    @Override
    public String[] getVersion() {
        return new String[] {
                "RPM-Importer ",
                ManifestUtils.getManifestInformation(VersionProvider.class) };
    }
}
