package io.quarkus.registry.app.maven;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.maven.artifact.Artifact;

public interface ArtifactContentProvider {
    boolean supports(Artifact artifact, UriInfo uriInfo);

    Response provide(Artifact artifact, UriInfo uriInfo) throws Exception;

}
