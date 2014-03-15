package org.globsframework.gui;

import org.globsframework.gui.splits.SplitsRepeatTest;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.PanelBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.components.GlobRepeat;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.directory.DefaultDirectory;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.UISpecTestCase;

import javax.swing.*;

public class GlobsPanelBuilderTest extends UISpecTestCase {

  public void testRepeatListensToChangeSets() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    Key key1 = Key.create(DummyObject.TYPE, 1);
    repository.create(key1, FieldValue.value(DummyObject.NAME, "a"));
    repository.create(Key.create(DummyObject.TYPE, 2), FieldValue.value(DummyObject.NAME, "c"));

    Directory directory = new DefaultDirectory();
    directory.add(new ColorService());

    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), null, repository, directory);
    builder.setSource(
      "<splits>" +
      "  <repeat ref='repeat'>" +
      "    <label ref='name'/>" +
      "  </repeat>" +
      "</splits>");

    GlobRepeat repeat =
      builder.addRepeat("repeat", DummyObject.TYPE, GlobMatchers.ALL,
                        new GlobFieldComparator(DummyObject.NAME), new RepeatComponentFactory<Glob>() {
          public void registerComponents(PanelBuilder cellBuilder, Glob item) {
            cellBuilder.add("name", new JLabel(item.get(DummyObject.NAME)));
          }
        });

    DummyGlobRepeatListener listener = new DummyGlobRepeatListener();
    repeat.addListener(listener);

    assertFalse(repeat.isEmpty());
    assertEquals(2, repeat.size());

    JPanel component = (JPanel)builder.doLoad().getComponent();
    SplitsRepeatTest.checkPanel(component, "label:a\n" +
                                           "label:c\n");

    repository.create(Key.create(DummyObject.TYPE, 3), FieldValue.value(DummyObject.NAME, "b"));
    SplitsRepeatTest.checkPanel(component, "label:a\n" +
                                           "label:b\n" +
                                           "label:c\n");
    listener.check("a,b,c");
    TestUtils.assertEquals(repeat.getCurrentGlobs().getSortedSet(DummyObject.NAME), "a", "b", "c");

    repository.delete(key1);
    SplitsRepeatTest.checkPanel(component, "label:b\n" +
                                           "label:c\n");
    listener.check("b,c");

    repeat.setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "b"));
    SplitsRepeatTest.checkPanel(component, "label:b\n");
    listener.check("b");

    repeat.setFilter(GlobMatchers.NONE);
    SplitsRepeatTest.checkPanel(component, "");
    listener.check("");

    assertTrue(repeat.isEmpty());
    assertEquals(0, repeat.size());
    assertEquals(0, repeat.getCurrentGlobs().size());
  }
}
