package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.KeyBuilder;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.Panel;

import javax.swing.*;
import java.awt.*;

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

    assertTrue(panel.getSwingComponents(JLabel.class).length == 1);
    assertThat(panel.getTextBox().textEquals("a - 1"));
  }

  public void testCreation() throws Exception {
    Panel panel = initComponent();

    assertTrue(panel.getSwingComponents(JLabel.class).length == 0);

    create(2, "b");
    assertTrue(panel.getSwingComponents(JLabel.class).length == 1);
    assertThat(panel.getTextBox().textEquals("b - 2"));
    assertTrue(Strings.isNullOrEmpty(disposeLogger.toString()));

    create(1, "a");
    Component[] labels = panel.getSwingComponents(JLabel.class);
    assertTrue(labels.length == 2);
    assertEquals("a - 1", ((JLabel)labels[0]).getText());
    assertEquals("b - 2", ((JLabel)labels[1]).getText());
    assertTrue(Strings.isNullOrEmpty(disposeLogger.toString()));

    repository.delete(KeyBuilder.newKey(DummyObject.TYPE, 1));
    assertEquals("a;", disposeLogger.toString());
    disposeLogger = new StringBuilder();

    assertTrue(panel.getSwingComponents(JLabel.class).length == 1);
    assertThat(panel.getTextBox().textEquals("b - 2"));
    assertTrue(Strings.isNullOrEmpty(disposeLogger.toString()));

    repository.delete(KeyBuilder.newKey(DummyObject.TYPE, 2));
    assertEquals("b;", disposeLogger.toString());
  }

  private void create(int id, String name) {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, id),
                      value(DummyObject.NAME, name));
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
    String name;
    private final Glob glob;

    public DummyComponentHolder(Glob glob) {
      this.glob = glob;
      name = glob.get(DummyObject.NAME);
    }

    public ComponentHolder setName(String name) {
      this.name = name;
      return this;
    }

    public JComponent getComponent() {
      return new JLabel(glob.get(DummyObject.NAME) + " - " + glob.get(DummyObject.ID));
    }

    public void dispose() {
      disposeLogger.append(name).append(";");
    }
  }
}
