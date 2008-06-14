package org.globsframework.gui.utils;

import org.globsframework.gui.GuiTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.DummyChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.model.utils.GlobMatcher;
import org.uispec4j.UISpec4J;

public abstract class GuiComponentTestCase extends GuiTestCase {
  protected Key key1;
  protected Key key2;
  protected Key key3;
  protected GlobRepository repository;
  protected DummyChangeSetListener changeListener = new DummyChangeSetListener();

  static {
    UISpec4J.init();
  }

  protected void setUp() throws Exception {
    super.setUp();
    key1 = newKey(DummyObject.TYPE, 1);
    key2 = newKey(DummyObject.TYPE, 2);
    key3 = newKey(DummyObject.TYPE, 3);
  }

  protected void tearDown() throws Exception {
    repository = null;
    changeListener = null;
  }

  protected GlobMatcher createNameMatcher(final String substring) {
    return new GlobMatcher() {
      public boolean matches(Glob dummyObject, GlobRepository repository) {
        return dummyObject.get(DummyObject.NAME).contains(substring);
      }
    };
  }
}
