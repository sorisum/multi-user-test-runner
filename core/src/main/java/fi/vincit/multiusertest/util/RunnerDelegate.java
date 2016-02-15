package fi.vincit.multiusertest.util;

import fi.vincit.multiusertest.annotation.MultiUserConfigClass;
import fi.vincit.multiusertest.test.MultiUserConfig;
import fi.vincit.multiusertest.test.UserRoleIT;
import org.junit.Before;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;

public class RunnerDelegate {

    private final UserIdentifier producerIdentifier;
    private final UserIdentifier userIdentifier;
    private final TestMethodFilter shouldRunChecker;

    public RunnerDelegate(UserIdentifier producerIdentifier, UserIdentifier consumerIdentifier) {
        Objects.requireNonNull(producerIdentifier);
        Objects.requireNonNull(consumerIdentifier);

        this.producerIdentifier = producerIdentifier;
        this.userIdentifier = consumerIdentifier;
        this.shouldRunChecker = new TestMethodFilter(producerIdentifier, consumerIdentifier);
    }

    // Only for testing
    RunnerDelegate(UserIdentifier producerIdentifier, UserIdentifier userIdentifier, TestMethodFilter shouldRunChecker) {
        Objects.requireNonNull(producerIdentifier);
        Objects.requireNonNull(userIdentifier);
        Objects.requireNonNull(shouldRunChecker);

        this.producerIdentifier = producerIdentifier;
        this.userIdentifier = userIdentifier;
        this.shouldRunChecker = shouldRunChecker;
    }

    public List<FrameworkMethod> filterMethods(List<FrameworkMethod> methods) {
        List<FrameworkMethod> filteredMethods = shouldRunChecker.filter(methods);
        if (!filteredMethods.isEmpty()) {
            return filteredMethods;
        } else {
            return methods;
        }
    }

    public boolean isIgnored(FrameworkMethod child, boolean isIgnoredByParent) {
        return !shouldRunChecker.shouldRun(child) || isIgnoredByParent;
    }

    public String testName(FrameworkMethod method) {
        return String.format("%s: %s", method.getName(), getIdentifierDescription());
    }

    public String getName(TestClass testClass) {
        return String.format("%s: %s", testClass.getName(), getIdentifierDescription());
    }

    private String getIdentifierDescription() {
        return String.format("producer = %s; consumer = %s", producerIdentifier, userIdentifier);
    }

    public Object createTest(Object testInstance) {

        if (testInstance instanceof UserRoleIT) {
            UserRoleIT roleItInstance = (UserRoleIT) testInstance;
            roleItInstance.setUsers(producerIdentifier, userIdentifier);
            return roleItInstance;
        } else {
            return testInstance;
        }

    }

    private MultiUserTestConfigProvider getConfigComponent(Object testInstance) {
        Optional<MultiUserTestConfigProvider> config = Optional.empty();
        for (Field field : testInstance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MultiUserConfigClass.class)) {
                config = Optional.<MultiUserTestConfigProvider>of(new MultiUserTestConfigProxy(field, testInstance));
                break;
            }
        }

        if (config.isPresent()) {
            return config.get();
        } else {
            throw new IllegalStateException("MultiUserConfigClass not found on " + testInstance.getClass().getSimpleName());
        }
    }

    public Statement withBefores(final TestClass testClass, final Object target, final Statement statement) {
        List<FrameworkMethod> befores = testClass.getAnnotatedMethods(
                Before.class);
        final Statement runLoginBeforeTestMethod = new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (target instanceof UserRoleIT) {
                    ((UserRoleIT)target).logInAs(LoginRole.PRODUCER);
                } else {
                    MultiUserTestConfigProvider config = getConfigComponent(target);
                    MultiUserConfig c = config.getConfig();
                    c.setUsers(producerIdentifier, userIdentifier);
                    c.logInAs(LoginRole.PRODUCER);
                }

                statement.evaluate();
            }
        };
        return befores.isEmpty() ? runLoginBeforeTestMethod : new RunBefores(runLoginBeforeTestMethod,
                befores, target);


    }
}
