package org.crossbowlabs.globs.model.impl;

import junit.framework.TestCase;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObject2;
import org.crossbowlabs.globs.metamodel.DummyObjectWithLinks;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.KeyBuilder.newKey;

public abstract class DefaultGlobRepositoryTestCase extends TestCase {
  protected GlobChecker checker = new GlobChecker();
  protected DummyChangeSetListener changeListener = new DummyChangeSetListener();
  protected GlobRepository repository;
  protected DummyChangeSetListener trigger = new DummyChangeSetListener();

  protected void init(String xml) {
    init(checker.parse(xml));
  }

  protected void initRepository() {
    init(GlobRepositoryBuilder.createEmpty());
  }

  protected void init(GlobRepository repository) {
       this.repository = repository;
       repository.addChangeListener(changeListener);
       repository.addTrigger(trigger);
     }

  protected Key initWithReadOnlyGlob(int value) {
       init(GlobRepositoryBuilder.init()
               .add(new ReadOnlyGlob(DummyObject.TYPE,
                                     FieldValuesBuilder
                                             .init(DummyObject.ID, value)
                                             .set(DummyObject.NAME, "name")
                                             .get()))
               .get());
       return getKey(value);
     }

  protected Key getKey(int value) {
    return newKey(DummyObject.TYPE, value);
  }

  protected Key getKey2(int value) {
    return newKey(DummyObject2.TYPE, value);
  }

  protected Key getLinksKey(int value) {
    return newKey(DummyObjectWithLinks.TYPE, value);
  }
}
