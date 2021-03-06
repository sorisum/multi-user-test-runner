package fi.vincit.multiusertest.rule.expectation2.value;


import fi.vincit.multiusertest.rule.AuthorizationRule;
import fi.vincit.multiusertest.rule.expection.ReturnValueCall;
import fi.vincit.multiusertest.util.UserIdentifier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.isA;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

public class ReturnValueWhenThenTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void getDefaultExpectation() {
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                () -> 1, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        ReturnValueCallNoExceptionExpectation<Integer> defaultExpectation =
                (ReturnValueCallNoExceptionExpectation)sut.getDefaultExpectation(UserIdentifier.getAnonymous());

        assertThat(defaultExpectation, isA(ReturnValueCallNoExceptionExpectation.class));
    }

    @Test
    public void test_AssertsValue() throws Throwable {
        ReturnValueCall<Integer> call = () -> 1;
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                call, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        TestValueExpectation expectation = mock(TestValueExpectation.class);
        sut.test(expectation, UserIdentifier.getAnonymous());

        verify(expectation).callAndAssertValue(call);
    }

    @Test
    public void test_CallThrows_WhenNotExpected() throws Throwable {
        IllegalArgumentException originalException = new IllegalArgumentException("Thrown from call");
        ReturnValueCall<Integer> call = () -> 1;
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                call, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        TestValueExpectation expectation = mock(TestValueExpectation.class);
        doThrow(originalException)
                .when(expectation)
                .callAndAssertValue(any());
        doThrow(new IllegalStateException("Thrown from expectation"))
                .when(expectation)
                .handleThrownException(UserIdentifier.getAnonymous(), originalException);

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Thrown from expectation");
        sut.test(expectation, UserIdentifier.getAnonymous());
    }

    @Test
    public void test_CallThrows_WhenExpectedButNotThrown() throws Throwable {
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                () -> 1, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        TestValueExpectation expectation = mock(TestValueExpectation.class);
        doThrow(new IllegalStateException("Thrown from expectation"))
                .when(expectation)
                .handleExceptionNotThrown(UserIdentifier.getAnonymous());

        expectedException.expect(IllegalStateException.class);
        expectedException.expectMessage("Thrown from expectation");
        sut.test(expectation, UserIdentifier.getAnonymous());
    }

    @Test
    public void test_CallDoesntThrow_WhenExpectedAndThrown() throws Throwable {
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                () -> 1, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        TestValueExpectation expectation = mock(TestValueExpectation.class);
        doThrow(new IllegalStateException(""))
                        .when(expectation)
                        .callAndAssertValue(any());

        sut.test(expectation, UserIdentifier.getAnonymous());
    }

    @Test
    public void test_CallDoesntThrown_WhenNotExpectedAndNotThrown() throws Throwable {
        ReturnValueWhenThen<Integer> sut = new ReturnValueWhenThen<>(
                () -> 1, UserIdentifier.getAnonymous(),
                mock(AuthorizationRule.class)
        );

        TestValueExpectation expectation = mock(TestValueExpectation.class);

        sut.test(expectation, UserIdentifier.getAnonymous());
    }
}