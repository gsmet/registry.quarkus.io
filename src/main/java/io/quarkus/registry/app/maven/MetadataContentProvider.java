package io.quarkus.registry.app.maven;

import java.io.IOException;
import java.io.StringWriter;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.quarkus.registry.app.model.Platform;
import io.quarkus.registry.app.model.PlatformRelease;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Snapshot;
import org.apache.maven.artifact.repository.metadata.SnapshotVersion;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.apache.maven.artifact.repository.metadata.io.xpp3.MetadataXpp3Writer;

@ApplicationScoped
public class MetadataContentProvider implements ArtifactContentProvider {

    private static final MetadataXpp3Writer METADATA_WRITER = new MetadataXpp3Writer();

    private static final String MAVEN_METADATA_XML = "maven-metadata.xml";

    @Inject
    MavenConfig mavenConfig;

    @Override
    public boolean supports(Artifact artifact, UriInfo uriInfo) {
        return MAVEN_METADATA_XML.equals(artifact.getType());
    }

    @Override
    public Response provide(Artifact artifact, UriInfo uriInfo) throws IOException {
        Metadata metadata = generateMetadata();
        String output = writeMetadata(metadata);
        return Response.ok(output)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML)
                .build();
    }

    private Metadata generateMetadata() {
        Metadata newMetadata = new Metadata();
        newMetadata.setGroupId(MavenConfig.GROUP_ID);
        newMetadata.setArtifactId(MavenConfig.PLATFORM_ARTIFACT_ID);

        Versioning versioning = new Versioning();
        newMetadata.setVersioning(versioning);

        versioning.updateTimestamp();

        Snapshot snapshot = new Snapshot();
        versioning.setSnapshot(snapshot);
        snapshot.setTimestamp(versioning.getLastUpdated().substring(0, 8) + "." + versioning.getLastUpdated().substring(8));
        snapshot.setBuildNumber(1);

//        for (PlatformRelease release : platform.releases) {
//            final String baseVersion = release.version;
//            addSnapshotVersion(versioning, snapshot, baseVersion, "pom");
//            addSnapshotVersion(versioning, snapshot, baseVersion, "json");
//
//            versioning.addVersion(release.version);
//        }
        return newMetadata;
    }

    private String writeMetadata(Metadata metadata) throws IOException {
        StringWriter sw = new StringWriter();
        METADATA_WRITER.write(sw, metadata);
        return sw.toString();
    }

    private void addSnapshotVersion(Versioning versioning, Snapshot snapshot, final String baseVersion,
                                    String extension) {
        final SnapshotVersion sv = new SnapshotVersion();
        sv.setExtension(extension);
        sv.setVersion(baseVersion + snapshot.getTimestamp() + "-" + snapshot.getBuildNumber());
        sv.setUpdated(versioning.getLastUpdated());
        versioning.addSnapshotVersion(sv);
    }

}
