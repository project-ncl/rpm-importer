package org.jboss.pnc.rpm.importer.clients;

import static org.jboss.pnc.rest.configuration.SwaggerConstants.MATCH_QUERY_PARAM;

import javax.ws.rs.PathParam;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.pnc.dto.Artifact;
import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.dto.requests.CreateAndSyncSCMRequest;
import org.jboss.pnc.dto.response.ArtifactInfo;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.dto.response.RepositoryCreationResponse;

import io.quarkus.rest.client.reactive.Url;

/**
 * Clone service used for cloning of the repository to the internal repository.
 * </p>
 * This is effectively very similar to the apis defined in org.jboss.pnc.api.reqour.rest
 * with small additions to pass the URL and HeaderParams.
 */
@ApplicationScoped
@RegisterRestClient(configKey = "orch-service")
@Path("/pnc-rest/v2")
public interface OrchService {

    @Path("/scm-repositories/create-and-sync")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @POST
    RepositoryCreationResponse createNew(
            @Url String url,
            @HeaderParam("Authorization") String accessToken,
            CreateAndSyncSCMRequest createAndSyncSCMRequest);

    @Path("/scm-repositories")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Page<SCMRepository> getAll(
            @Url String url,
            @HeaderParam("Authorization") String accessToken,
            @QueryParam(MATCH_QUERY_PARAM) String matchUrl);

    @Path("/artifacts/filter")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Page<ArtifactInfo> getArtifactsFiltered(
            @Url String url,
            @HeaderParam("Authorization") String accessToken,
            @QueryParam("identifier") String identifier);

    @Path("/artifacts/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Artifact getSpecific(
            @Url String url,
            @HeaderParam("Authorization") String accessToken,
            @PathParam("id") String id);

    @Path("/builds/{id}/artifacts/built")
    @Produces(MediaType.APPLICATION_JSON)
    @GET
    Page<Artifact> getBuiltArtifacts(
            @Url String url,
            @HeaderParam("Authorization") String accessToken,
            @PathParam("id") String id);
}
