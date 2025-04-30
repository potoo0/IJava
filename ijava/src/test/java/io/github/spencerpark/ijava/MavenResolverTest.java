package io.github.spencerpark.ijava;

import io.github.spencerpark.ijava.utils.ResolveDependency;
import org.eclipse.aether.repository.NoLocalRepositoryManagerException;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.junit.jupiter.api.Test;

import java.util.List;

public class MavenResolverTest {
    @Test
    void test() throws DependencyResolutionException, NoLocalRepositoryManagerException {
        String localRepo = "out";
        List<RemoteRepository> remotes = List.of(
                new RemoteRepository.Builder("aliyun", "default", "https://maven.aliyun.com/repository/central").build()
        );
        var coords = List.of(
                "org.apache.logging.log4j:log4j-core:2.24.3"
        );
        List<String> jars = ResolveDependency.resolve(coords, null, localRepo, remotes);
        System.out.printf(">>>>>> jars: %s%n", jars);
    }
}
