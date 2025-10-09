package org.jboss.pnc.rpm.importer.utils;

import io.smallrye.common.process.ProcessBuilder;

public class Brew {
    private static final String BREW = "/usr/bin/brew";

    public static String getBuildInfo(String nvr) {
        return ProcessBuilder.execToString(
                BREW,
                "call",
                "--json-output",
                "getBuild",
                nvr);
    }
}
