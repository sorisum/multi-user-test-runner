package fi.vincit.mutrproject.feature.todo;

import fi.vincit.multiusertest.annotation.RunWithUsers;
import fi.vincit.multiusertest.util.LoginRole;
import fi.vincit.mutrproject.testconfig.AbstractConfiguredMultiRoleIT;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;

import static fi.vincit.multiusertest.rule.expectation2.TestExpectations.expectExceptionInsteadOfValue;
import static fi.vincit.multiusertest.rule.expectation2.TestExpectations.expectNotToFailIgnoringValue;
import static fi.vincit.multiusertest.util.UserIdentifiers.anyOf;

/**
 * Basic examples on how to use MUTR with Expectation 2 API.
 * This test demonstrates the usage of roles and built-in user roles
 * PRODUCER and ANONYMOUS.
 */
@RunWithUsers(
        producers = {"role:ROLE_SYSTEM_ADMIN", "role:ROLE_ADMIN", "role:ROLE_USER", "role:ROLE_USER"},
        consumers = {"role:ROLE_SYSTEM_ADMIN", "role:ROLE_ADMIN", "role:ROLE_USER",
                RunWithUsers.PRODUCER, RunWithUsers.ANONYMOUS}
)
public class TodoServiceExpectation2IT extends AbstractConfiguredMultiRoleIT {

    // Service under test
    @Autowired
    private TodoService todoService;

    @Test
    public void getPrivateTodoList() throws Throwable {
        // At this point the producer has been logged in automatically
        long id = todoService.createTodoList("Test list", false);

        // Change user to consumer in order to test how getTodoList works
        config().logInAs(LoginRole.CONSUMER);

        // Initialize the assertion rule and call service
        authorization()
                .testCall(() -> todoService.getTodoList(id))
                .whenCalledWith(anyOf("role:ROLE_USER", RunWithUsers.ANONYMOUS))
                .then(expectExceptionInsteadOfValue(AccessDeniedException.class))
                .test();
    }

    @Test
    public void getPublicTodoList() throws Throwable {
        long id = todoService.createTodoList("Test list", true);
        config().logInAs(LoginRole.CONSUMER);
        // This is expected to succeed for all roles
        todoService.getTodoList(id);
    }

    /**
     * This test is run for all producers but only for the given consumer roles
     * @throws Throwable
     */
    @Test
    @RunWithUsers(consumers = {"role:ROLE_SYSTEM_ADMIN", "role:ROLE_ADMIN", "role:ROLE_USER", RunWithUsers.PRODUCER})
    public void addTodoItem() throws Throwable {
        long listId = todoService.createTodoList("Test list", false);
        config().logInAs(LoginRole.CONSUMER);

        authorization()
                .testCall(() -> todoService.addItemToList(listId, "Write tests"))
                .whenCalledWith(anyOf("role:ROLE_ADMIN", "role:ROLE_SYSTEM_ADMIN", RunWithUsers.PRODUCER))
                .then(expectNotToFailIgnoringValue())
                .otherwise(expectExceptionInsteadOfValue(AccessDeniedException.class))
                .test();
    }

    /**
     * This test is run for all producers but only when the consumer is ANONYMOUS
     * See {@link TodoServiceJava8IT#addTodoItem()} for more advanced version
     * @throws Throwable
     */
    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    @RunWithUsers(consumers = RunWithUsers.ANONYMOUS)
    public void addTodoItemAnonymous() throws Throwable {
        long listId = todoService.createTodoList("Test list", false);
        config().logInAs(LoginRole.CONSUMER);
        todoService.addItemToList(listId, "Write tests");
    }

}