package cool.solr.security;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

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
            new File("src/test/resources/docker-compose.yaml"))
                .withLocalCompose(true);

    @BeforeClass
    public static void configureSecurity() throws Exception {
        ContainerState solr = (ContainerState) environment.getContainerByServiceName("solr_1").get();

        synchronized(solr) {
            solr.wait(2000);
        }

        // enable the loaded security.json
        solr.execInContainer(SOLR_ENABLE_SECURITY_CMD);
    }

    @Test
    public void deniesUnauthenticatedRequests() throws Exception {
        // given
        String solrUri = String.format("http://%s:%s", environment.getServiceHost("traefik_1", SOLR_PORT), SOLR_PORT);
        HttpClient client = HttpClient.newHttpClient();

        // when
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(solrUri)).GET().build();

        // then
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, response.statusCode());
    }

    @Test
    public void allowsAuthenticatedRequests() throws Exception {
        // given
        String solrUri = String.format("http://%s:%s", environment.getServiceHost("traefik_1", SOLR_PORT), SOLR_PORT);
        HttpClient client = HttpClient.newBuilder()
            .authenticator(new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication("solr", "solr".toCharArray());
                }
            })
            .build();

        // when
        HttpRequest request = HttpRequest.newBuilder()
            .uri(new URI(solrUri))
            .GET().build();

        // then
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(302, response.statusCode());
    }
}
