package fi.vincit.multiusertest;

import fi.vincit.multiusertest.annotation.MultiUserConfigClass;
import fi.vincit.multiusertest.annotation.MultiUserTestConfig;
import fi.vincit.multiusertest.annotation.RunWithUsers;
import fi.vincit.multiusertest.configuration.ConfiguredTest;
import fi.vincit.multiusertest.rule.AuthorizationRule;
import fi.vincit.multiusertest.runner.junit.MultiUserTestRunner;
import fi.vincit.multiusertest.util.LoginRole;
import fi.vincit.multiusertest.util.SecurityUtil;
import fi.vincit.multiusertest.util.User;
import fi.vincit.multiusertest.util.UserIdentifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static fi.vincit.multiusertest.rule.Authentication.toFail;
import static fi.vincit.multiusertest.util.UserIdentifiers.ifAnyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWithUsers(producers = {RunWithUsers.ANONYMOUS},
        consumers = {"role:ROLE_ADMIN", "role:ROLE_USER"})
@MultiUserTestConfig
@RunWith(MultiUserTestRunner.class)
public class AnonymousProducerTest {

    @MultiUserConfigClass
    private ConfiguredTest configuredTest = new ConfiguredTest();

    @Rule
    public AuthorizationRule authorizationRule = new AuthorizationRule();

    @Test
    public void producerLoggedIn() {
        assertThat(SecurityUtil.getLoggedInUser(), nullValue());
    }

    @Test
    public void consumerLoggedIn() {
        configuredTest.logInAs(LoginRole.CONSUMER);
        assertThat(SecurityUtil.getLoggedInUser().getUsername(), is(configuredTest.getConsumer().getUsername()));
    }

    @Test
    public void producerLoggedInAfterUser() {
        configuredTest.logInAs(LoginRole.CONSUMER);
        configuredTest.logInAs(LoginRole.PRODUCER);

        assertThat(SecurityUtil.getLoggedInUser(), nullValue());
    }

    @Test
    public void expectFailureAnonymousProducer() {
        configuredTest.logInAs(LoginRole.PRODUCER);
        authorizationRule.expect(toFail(ifAnyOf(RunWithUsers.PRODUCER)));
        throwIfUserRole(RunWithUsers.ANONYMOUS);
    }

    @Test
    public void dontExpectFailure() {
        authorizationRule.dontExpectToFail();
    }

    private void throwIfUserRole(String identifier) {
        if (identifier.equals(RunWithUsers.ANONYMOUS)) {
            throw new IllegalStateException("Thrown when role was ANONYMOUS");
        } else {
            User.Role identifierRole = configuredTest.stringToRole(UserIdentifier.parse(identifier).getIdentifier());
            if (SecurityUtil.getLoggedInUser().getRole() == identifierRole) {
                throw new IllegalStateException("Thrown when role was " + identifier);
            }
        }
    }

}
