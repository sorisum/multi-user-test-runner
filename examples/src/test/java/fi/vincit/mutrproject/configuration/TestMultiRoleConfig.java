package fi.vincit.mutrproject.configuration;

import fi.vincit.multiusertest.test.AbstractMultiUserAndRoleConfig;
import fi.vincit.multiusertest.util.LoginRole;
import fi.vincit.mutrproject.feature.user.UserService;
import fi.vincit.mutrproject.feature.user.model.Role;
import fi.vincit.mutrproject.feature.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;

/**
 * Example on how to configure users with multiple roles. Uses a custom role
 * {@link RoleGroup} to configure which roles to configure for the user. RoleGroup
 * can be any enum or object that just defines all the combinations that need to
 * be tested.
 */
public class TestMultiRoleConfig extends AbstractMultiUserAndRoleConfig<User, Role> {

    @Autowired
    private UserService userService;

    @Override
    public void loginWithUser(User user) {
        userService.loginUser(user);
    }

    @Override
    public User createUser(String username, String firstName, String lastName, Collection<Role> userRoles, LoginRole loginRole) {
        return userService.createUser(username, username, userRoles);
    }

    /**
     * Map role group to real system roles
     * @param roleGroup Role group to map
     * @return Role group mapped to system roles
     */
    private Collection<Role> roleGroupToRoles(RoleGroup roleGroup) {
        switch (roleGroup) {
            case ADMINISTRATOR: return Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_MODERATOR, Role.ROLE_USER);
            case REGULAR_USER: return Arrays.asList(Role.ROLE_USER);
            default: throw new IllegalArgumentException("Invalid role group " + roleGroup);
        }
    }

    @Override
    protected Role identifierPartToRole(String identifier) {
        return Role.valueOf("ROLE_" + identifier);
    }

    @Override
    public User getUserByUsername(String username) {
        return userService.loadUserByUsername(username);
    }
}
