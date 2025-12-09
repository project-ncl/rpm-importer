package org.jboss.pnc.rpm.importer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class Macros {
    private String scl;
    private String dist;
}
