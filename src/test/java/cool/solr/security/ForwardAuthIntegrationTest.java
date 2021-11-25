package cool.solr.security;

import java.io.File;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.testcontainers.containers.ContainerState;
import org.testcontainers.containers.DockerComposeContainer;

public class ForwardAuthIntegrationTest {

    public static final int SOLR_PORT = 8983;

    private static final String[] SOLR_ENABLE_SECURITY_CMD = { "solr", "zk", "cp",
            "file:/opt/solr/server/solr/security.json", "zk:/security.json", "-z", "zookeeper:2181" };

    @ClassRule
    public static DockerComposeContainer environment = new DockerComposeContainer(
            new File("src/test/resources/docker-compose.yaml")).withLocalCompose(true);

    @BeforeClass
    public static void configureSecurity() throws Exception {
        ContainerState solr = (ContainerState) environment.getContainerByServiceName("solr_1").get();

        solr.execInContainer(SOLR_ENABLE_SECURITY_CMD);
    }

    @Test
    public void solrIsSecured() {

    }
}
