package cool.solr.security;

import java.io.IOException;

import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.solr.api.Command;
import org.apache.solr.api.EndPoint;
import org.apache.solr.client.solrj.SolrRequest.METHOD;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.security.PermissionNameProvider;

@EndPoint(method = METHOD.GET, path = "/*", permission = PermissionNameProvider.Name.CONFIG_READ_PERM)
public class ForwardAuthPluginLoader implements ResourceLoaderAware {

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        loader.newInstance(ForwardAuthPlugin.class.getName(), ForwardAuthPlugin.class);
        loader.newInstance(DefaultRuleBasedAuthorizationPlugin.class.getName(), DefaultRuleBasedAuthorizationPlugin.class);
    }

    @Command
    public void call(SolrQueryRequest req, SolrQueryResponse rsp) throws IOException {
        // noop
    }
}
