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
import org.apache.solr.security.AuthenticationPlugin;

public class ForwardAuthPlugin extends AuthenticationPlugin {

    public static final String ARG_USER_HEADER = "httpUserHeader";
    public static final String HTTP_HEADER_USER_DEFAULT = "X-Forwarded-User";

    private String httpUserHeader;

    @Override
    public void init(Map<String, Object> args) {
        this.httpUserHeader = (String) args.getOrDefault(ARG_USER_HEADER, HTTP_HEADER_USER_DEFAULT);
    }

    @Override
    public boolean doAuthenticate(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
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
        }

        return false;
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
