package org.crossbowlabs.globs.utils.logging;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObject2;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;

public class DebugTest extends TestCase {
  public void test() throws Exception {
    GlobChecker checker = new GlobChecker();
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    checker.parse(repository,
                  "<dummyObject id='1' name='obj1'/>" +
                  "<dummyObject id='2' name='obj2' link='1'/>" +
                  "<dummyObject2 id='1' label='otherObj1'/>");

    Debug.printChanges(repository);

    Debug.enter("Step 1");

    Debug.print("First", repository);

    Debug.print("Hello");

    repository.enterBulkDispatchingMode();
    repository.create(DummyObject2.TYPE,
                      value(DummyObject2.ID, 2),
                      value(DummyObject2.LABEL, "lbl"));
    repository.update(Key.create(DummyObject.TYPE, 2), DummyObject.NAME, "newObj2");
    repository.delete(Key.create(DummyObject.TYPE, 1));
    repository.completeBulkDispatchingMode();

    Debug.exit();

    Debug.enter("Step 2");

    Debug.print(repository);

    Debug.exit();
  }
}
