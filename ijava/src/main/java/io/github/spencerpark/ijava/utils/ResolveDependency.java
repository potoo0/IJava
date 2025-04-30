package io.github.spencerpark.ijava.utils;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.DefaultRepositoryCache;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.supplier.RepositorySystemSupplier;
import org.eclipse.aether.transport.classpath.ClasspathTransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.ChecksumExtractor;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator;

import java.io.File;
import java.util.*;

public class ResolveDependency {
    private static final String DEFAULT_REPO_LOCAL = String.format("%s/.m2/repository", System.getProperty("user.home"));
    private static final RemoteRepository DEFAULT_REPO_REMOTE = new RemoteRepository.Builder("central", "default", "https://repo1.maven.org/maven2/").build();
    private static final Set<String> DEFAULT_SCOPES = Set.of(JavaScopes.RUNTIME);

    private static final RepositorySystem system;

    static {
        system = new RepositorySystemSupplierWithCustom().get();
    }

    public static List<String> resolve(String... coords) throws DependencyResolutionException, NoLocalRepositoryManagerException {
        return resolve(List.of(coords), DEFAULT_SCOPES, DEFAULT_REPO_LOCAL, List.of(DEFAULT_REPO_REMOTE));
    }

    /**
     * resolve
     *
     * @param coords      eg: org.apache.logging.log4j:log4j-core:2.19.0
     * @param scopes      default to DEFAULT_SCOPES if null or empty
     * @param localRepo   default to DEFAULT_REPO_LOCAL if null
     * @param remoteRepos default to DEFAULT_REPO_REMOTE if null or empty
     * @return jar files absolute path
     **/
    public static List<String> resolve(List<String> coords, Set<String> scopes, String localRepo, List<RemoteRepository> remoteRepos) throws DependencyResolutionException, NoLocalRepositoryManagerException {
        if (coords.isEmpty()) return Collections.emptyList();
        if (scopes == null || scopes.isEmpty()) scopes = DEFAULT_SCOPES;
        if (localRepo == null) localRepo = DEFAULT_REPO_LOCAL;
        if (remoteRepos == null || remoteRepos.isEmpty()) remoteRepos = List.of(DEFAULT_REPO_REMOTE);

        RepositorySystemSession session = buildSession(localRepo);

        List<Dependency> dependencies = coords.stream().map(DefaultArtifact::new).map(artifact -> new Dependency(artifact, null)).toList();
        var collectRequest = new CollectRequest(dependencies, null, remoteRepos);

        var request = new DependencyRequest(collectRequest, DependencyFilterUtils.classpathFilter(scopes));
        DependencyResult result = system.resolveDependencies(session, request);

        var nodeListGenerator = new PreorderNodeListGenerator();
        result.getRoot().accept(nodeListGenerator);

        return nodeListGenerator.getFiles().stream().map(File::getAbsolutePath).toList();
    }

    private static RepositorySystemSession buildSession(String localRepo) {
        var session = MavenRepositorySystemUtils.newSession();
        session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, new LocalRepository(localRepo)));
        session.setCache(new DefaultRepositoryCache());
        return session;
    }

    static class RepositorySystemSupplierWithCustom extends RepositorySystemSupplier {
        @Override
        protected Map<String, TransporterFactory> getTransporterFactories(Map<String, ChecksumExtractor> extractors) {
            HashMap<String, TransporterFactory> result = new HashMap<>();
            result.put(FileTransporterFactory.NAME, new FileTransporterFactory());
            result.put(HttpTransporterFactory.NAME, new HttpTransporterFactory(extractors));
            result.put(ClasspathTransporterFactory.NAME, new ClasspathTransporterFactory());
            return result;
        }
    }
}
