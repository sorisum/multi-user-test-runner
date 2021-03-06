package fi.vincit.multiusertest.rule.expectation2;

import fi.vincit.multiusertest.util.UserIdentifier;
import fi.vincit.multiusertest.util.UserIdentifiers;

public interface Then<T extends TestExpectation> {

    /**
     * Make sure to call {@link When#whenCalledWith(UserIdentifier...)} or {@link When#whenCalledWith(UserIdentifiers...)}
     * first to set the user identifiers this call will add the expectation.
     * @param testExpectation
     * @return
     */
    WhenThen<T> then(T testExpectation);

    /**
     * Define default expectation.
     * @param testExpectation Default expectation
     * @return
     * @since 0.5
     */
    WhenThen<T> otherwise(T testExpectation);

    /**
     * Define default expectation.
     * Alias for {@link this#otherwise(TestExpectation)}.
     * @param testExpectation
     * @return
     * @since 0.5
     */
    WhenThen<T> byDefault(T testExpectation);
}
