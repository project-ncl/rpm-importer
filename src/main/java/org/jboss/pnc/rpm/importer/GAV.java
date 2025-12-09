package org.jboss.pnc.rpm.importer;

import org.commonjava.atlas.maven.ident.ref.SimpleArtifactRef;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@AllArgsConstructor
@ToString
public class GAV {
    private String groupId;
    private String artifactId;
    private String version;

    public static GAV parse(String gav) {
        SimpleArtifactRef parsed = SimpleArtifactRef.parse(gav);
        return new GAV(
                parsed.getGroupId(),
                parsed.getArtifactId(),
                parsed.getVersionString());
    }
}
