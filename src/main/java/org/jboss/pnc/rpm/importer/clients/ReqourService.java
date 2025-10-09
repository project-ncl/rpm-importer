package org.jboss.pnc.rpm.importer.clients;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.api.reqour.dto.TranslateRequest;
import org.jboss.pnc.api.reqour.dto.TranslateResponse;

import io.quarkus.rest.client.reactive.Url;

@ApplicationScoped
@RegisterRestClient(configKey = "reqour-service")
public interface ReqourService {

    @Path("/external-to-internal")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    TranslateResponse external_to_internal(
            @Url String url,
            TranslateRequest externalToInternalRequestDto);
}
