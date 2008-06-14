package org.globsframework.utils.logging;

import junit.framework.TestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobChecker;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.Key;

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
