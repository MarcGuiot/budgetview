package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepositoryChecker;
import org.uispec4j.ComboBox;

import javax.swing.*;

import static org.globsframework.model.FieldValue.value;

public class GlobComboEditorTest extends GuiComponentTestCase {
  protected Glob glob;
  private GlobRepositoryChecker repoChecker;

  protected void setUp() throws Exception {
    super.setUp();
    repository = checker.parse("<dummyObject id='1' name='name1' count='3'/>");
    repository.addChangeListener(changeListener);
    repoChecker = new GlobRepositoryChecker(repository);
    glob = repository.get(key1);
  }

  private ComboBox initStringEditor() {
    JComboBox combo = GlobComboEditor.init(key1, DummyObject.NAME,
                                           new String[]{"name0", "name1", "name2"},
                                           repository, directory).getComponent();
    return new ComboBox(combo);
  }

  private ComboBox initIntegerEditor() {
    JComboBox combo = GlobComboEditor.init(key1, DummyObject.COUNT,
                                           new int[]{1, 3, 5, 7},
                                           repository, directory).getComponent();
    return new ComboBox(combo);
  }

  public void testStandardUsageWithInts() throws Exception {
    ComboBox combo = initIntegerEditor();
    assertThat(combo.contentEquals("1", "3", "5", "7"));
    assertThat(combo.selectionEquals("3"));

    combo.select("5");
    repoChecker.checkFields(glob, value(DummyObject.COUNT, 5));

    combo.select("7");
    repoChecker.checkFields(glob, value(DummyObject.COUNT, 7));

    repository.update(key1, value(DummyObject.COUNT, 1));
    assertThat(combo.selectionEquals("1"));
  }

  public void testStandardUsageWithStrings() throws Exception {
    ComboBox combo = initStringEditor();
    assertThat(combo.contentEquals("name0", "name1", "name2"));
    assertThat(combo.selectionEquals("name1"));

    combo.select("name0");
    repoChecker.checkFields(glob, value(DummyObject.NAME, "name0"));

    combo.select("name2");
    repoChecker.checkFields(glob, value(DummyObject.NAME, "name2"));
  }

  public void testObjectDoesNotExistOnCreation() throws Exception {
    repository.delete(key1);
    ComboBox combo = initStringEditor();
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(null));

    repository.create(key1, value(DummyObject.NAME, "name2"));
    assertThat(combo.selectionEquals("name2"));
  }

  public void testObjectDeletedAndCreated() throws Exception {
    ComboBox combo = initIntegerEditor();
    assertThat(combo.selectionEquals("3"));

    repository.delete(glob.getKey());
    assertFalse(combo.isEnabled());
    assertTrue(combo.selectionEquals(null));

    repository.create(key1, value(DummyObject.COUNT, 7));
    assertThat(combo.selectionEquals("7"));
  }

  public void testSetName() throws Exception {
    JComboBox combo = GlobComboEditor.init(key1, DummyObject.NAME,
                                           new String[]{"name0", "name1", "name2"},
                                           repository, directory).setName("componentName")
      .getComponent();
    assertEquals("componentName", combo.getName());
  }
}
