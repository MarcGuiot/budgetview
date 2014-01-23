package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.*;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.KeyChangeListener;
import org.globsframework.utils.Strings;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.Panel;

import javax.swing.*;
import java.awt.*;

import static org.globsframework.model.FieldValue.value;

public class GlobRepeatViewTest extends GuiComponentTestCase {
  private StringBuilder disposeLogger;

  protected void setUp() throws Exception {
    super.setUp();
    disposeLogger = new StringBuilder();
    repository = GlobRepositoryBuilder.createEmpty();
  }

  public void testWithExistingContent() throws Exception {
    create(1, "a");

    Panel panel = initComponent();
    checkComponents(panel, "a - 1");
  }

  public void testCreation() throws Exception {
    Panel panel = initComponent();

    checkNoComponents(panel);

    create(2, "b");
    checkComponents(panel, "b - 2");
    checkNoDispose();

    create(1, "a");
    checkComponents(panel, "a - 1", "b - 2");
    checkNoDispose();

    repository.delete(KeyBuilder.newKey(DummyObject.TYPE, 1));
    checkComponents(panel, "b - 2");
    checkDisposeAndClear("a;");

    repository.delete(KeyBuilder.newKey(DummyObject.TYPE, 2));
    checkDisposeAndClear("b;");
    checkComponents(panel);
  }

  public void testWithComparatorAndFieldUpdates() throws Exception {
    Panel panel = initComponent();
    checkNoComponents(panel);

    create(1, null);
    checkComponents(panel, "null - 1");
    checkNoDispose();

    update(1, "b");
    checkComponents(panel, "b - 1");
    checkNoDispose();

    create(2, null);
    checkComponents(panel, "null - 2", "b - 1");
    checkNoDispose();

    update(2, "a");
    checkComponents(panel, "a - 2", "b - 1");
    checkNoDispose();

    update(2, "c");
    checkComponents(panel, "b - 1", "c - 2");
    checkNoDispose();
  }

  private void checkDisposeAndClear(String expected) {
    assertEquals(expected, disposeLogger.toString());
    disposeLogger = new StringBuilder();
  }

  private void checkNoDispose() {
    assertTrue(Strings.isNullOrEmpty(disposeLogger.toString()));
  }

  private void checkNoComponents(Panel panel) {
    checkComponents(panel);
  }

  private void checkComponents(Panel panel, String... expected) {
    Component[] labels = panel.getSwingComponents(JLabel.class);
    String[] actual = new String[labels.length];
    for (int i = 0; i < labels.length; i++) {
      actual[i] = ((JLabel)labels[i]).getText();
    }
    TestUtils.assertEquals(expected, actual);
  }

  private void create(int id, String name) {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, id),
                      value(DummyObject.NAME, name));
  }

  private void update(int id, String name) {
    repository.update(Key.create(DummyObject.TYPE, id), DummyObject.NAME, name);
  }

  private Panel initComponent() {
    JPanel jPanel =
      GlobRepeatView.init(DummyObject.TYPE, repository, directory, new GlobFieldComparator(DummyObject.NAME),
                          new GlobRepeatView.Factory() {
                            public ComponentHolder getComponent(final Glob glob, GlobRepository repository, Directory directory) {
                              return new DummyComponentHolder(glob);
                            }
                          })
        .getComponent();
    return new Panel(jPanel);
  }

  private class DummyComponentHolder implements ComponentHolder {
    private Key key;
    private String name;
    private JLabel label = new JLabel();
    private final ChangeSetListener changeListener;

    public DummyComponentHolder(Glob glob) {
      this.key = glob.getKey();
      this.name = glob.get(DummyObject.NAME);
      this.label.setName(name);
      this.changeListener = new KeyChangeListener(key) {
        public void update() {
          doUpdate();
        }
      };
      repository.addChangeListener(changeListener);
      doUpdate();
    }

    private void doUpdate() {
      Glob glob = repository.find(key);
      if (glob == null) {
        label.setText("");
      }
      else {
        label.setText(glob.get(DummyObject.NAME) + " - " + glob.get(DummyObject.ID));
      }
    }

    public ComponentHolder setName(String name) {
      this.name = name;
      return this;
    }

    public JComponent getComponent() {
      return label;
    }

    public void dispose() {
      disposeLogger.append(name).append(";");
      repository.removeChangeListener(changeListener);
    }
  }
}
