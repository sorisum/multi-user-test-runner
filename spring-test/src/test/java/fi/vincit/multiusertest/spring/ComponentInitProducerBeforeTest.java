package fi.vincit.multiusertest.spring;

import fi.vincit.multiusertest.annotation.MultiUserConfigClass;
import fi.vincit.multiusertest.annotation.RunWithUsers;
import fi.vincit.multiusertest.context.ComponentTestContext;
import fi.vincit.multiusertest.context.TestConfiguration;
import fi.vincit.multiusertest.rule.AuthorizationRule;
import fi.vincit.multiusertest.runner.junit.MultiUserTestRunner;
import fi.vincit.multiusertest.test.MultiUserConfig;
import fi.vincit.multiusertest.util.LoginRole;
import fi.vincit.multiusertest.util.User;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import static org.junit.Assert.assertTrue;

@RunWithUsers(producers = {"user:test-user"},
        consumers = {"role:ROLE_ADMIN", "role:ROLE_USER"})
@ContextConfiguration(classes = {TestConfiguration.class, ComponentTestContext.class})
@RunWith(MultiUserTestRunner.class)
public class ComponentInitProducerBeforeTest {

    private static boolean producerCreated = false;

    @Autowired
    @MultiUserConfigClass
    private MultiUserConfig<User, User.Role> multiUserConfig;

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Rule
    public AuthorizationRule authorizationRule = new AuthorizationRule();

    @Before
    public void init() {
        multiUserConfig.createUser("test-user", "Test", "Consumer", User.Role.ROLE_USER, LoginRole.PRODUCER);
        producerCreated = true;
    }


    @Test
    public void producerLoggedIn() {
        if (!multiUserConfig.getProducer().getUsername().equals("test-user")) {
            throw new AssertionError("Wrong producer user, should be test-user");
        }
        assertTrue(producerCreated);
    }

}
