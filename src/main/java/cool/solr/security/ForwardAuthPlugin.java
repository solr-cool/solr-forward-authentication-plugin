package cool.solr.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Map;
import java.util.Objects;

import javax.security.auth.Subject;
import javax.servlet.FilterChain;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.annotation.Contract;
import org.apache.http.annotation.ThreadingBehavior;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.security.AuthenticationPlugin;
import org.apache.solr.security.BasicAuthPlugin;

public class ForwardAuthPlugin extends AuthenticationPlugin {

    public static final String ARG_USER_HEADER = "httpUserHeader";
    public static final String HTTP_HEADER_USER_DEFAULT = "X-Forwarded-User";
    public static final String X_REQUESTED_WITH_HEADER = "X-Requested-With";

    private String httpUserHeader;
    private boolean blockUnknown = false;

    @Override
    public void init(Map<String, Object> args) {
        this.httpUserHeader = (String) args.getOrDefault(ARG_USER_HEADER, HTTP_HEADER_USER_DEFAULT);

        Object o = args.get(BasicAuthPlugin.PROPERTY_BLOCK_UNKNOWN);
        if (o != null) {
            try {
                blockUnknown = Boolean.parseBoolean(o.toString());
            } catch (Exception e) {
                throw new SolrException(ErrorCode.BAD_REQUEST,
                        "Invalid value for parameter " + BasicAuthPlugin.PROPERTY_BLOCK_UNKNOWN);
            }
        }
    }

    @Override
    public boolean doAuthenticate(ServletRequest servletRequest, ServletResponse servletResponse,
            FilterChain filterChain)
            throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        final String username = request.getHeader(httpUserHeader);
        if (username != null) {
            HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                @Override
                public Principal getUserPrincipal() {
                    return new ForwardAuthUserPrincipal(username);
                }
            };
            numAuthenticated.inc();
            filterChain.doFilter(wrapper, response);
            return true;
        } else if (blockUnknown) {
            numMissingCredentials.inc();
            return false;
        } else {
            numPassThrough.inc();
            filterChain.doFilter(request, response);
            return true;
        }
    }

    @Contract(threading = ThreadingBehavior.IMMUTABLE)
    private static class ForwardAuthUserPrincipal implements Principal, Serializable {
        private String username;

        public ForwardAuthUserPrincipal(String username) {
            this.username = username;
        }

        @Override
        public String getName() {
            return this.username;
        }

        @Override
        public boolean implies(Subject subject) {
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ForwardAuthUserPrincipal that = (ForwardAuthUserPrincipal) o;
            return Objects.equals(username, that.username);
        }

        @Override
        public int hashCode() {
            return Objects.hash(username);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this).append("username", username).toString();
        }
    }
}
