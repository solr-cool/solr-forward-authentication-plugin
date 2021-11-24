package cool.solr.security;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.apache.solr.security.RuleBasedAuthorizationPlugin;

public class DefaultRuleBasedAuthorizationPlugin extends RuleBasedAuthorizationPlugin {

    public static final String ARG_DEFAULT_ROLES = "defaultRole";
    private String defaultRole;

    @Override
    public void init(Map<String, Object> args) {
        super.init(args);

        if (args.containsKey(ARG_DEFAULT_ROLES)) {
            this.defaultRole = (String) args.get(ARG_DEFAULT_ROLES);
        }
    }

    @Override
    public Set<String> getUserRoles(Principal principal) {
        Set<String> roles = super.getUserRoles(principal);

        if ((roles == null || roles.isEmpty()) && defaultRole != null) {
            return Collections.singleton(defaultRole);
        }

        return roles;
    }

}
