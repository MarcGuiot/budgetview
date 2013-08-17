package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.utils.GlobBuilder;

import javax.swing.*;

import static org.globsframework.model.FieldValue.value;

public class GlobToggleEditorTest extends GuiComponentTestCase {
  private GlobToggleEditor editor;
  private JToggleButton toggle;
  private Glob glob1;
  private Glob glob2;

  protected void setUp() throws Exception {
    super.setUp();
    repository =
      checker.parse("<dummyObject id='1' name='name1' present='true'/>" +
                    "<dummyObject id='2' name='name2' present='false'/>");
    editor = GlobToggleEditor.init(DummyObject.PRESENT, repository, directory);
    toggle = editor.getComponent();

    glob1 = repository.get(key1);
    glob2 = repository.get(key2);
  }

  public void tearDown() throws Exception {
    super.tearDown();
    editor.dispose();
    editor = null;
    toggle = null;
  }

  public void testMonoSelection() throws Exception {
    assertFalse(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    selectionService.select(glob1);
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());

    toggle.setSelected(false);
    assertFalse(glob1.get(DummyObject.PRESENT));

    repository.update(key1, DummyObject.PRESENT, true);
    assertTrue(toggle.isSelected());

    repository.update(key2, DummyObject.PRESENT, true);
    repository.update(key2, DummyObject.PRESENT, false);
    assertTrue(toggle.isSelected());
  }

  public void testMultiSelection() throws Exception {
    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertTrue(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    toggle.setSelected(true);
    assertTrue(glob1.get(DummyObject.PRESENT));
    assertTrue(glob2.get(DummyObject.PRESENT));

    toggle.setSelected(false);
    assertFalse(glob1.get(DummyObject.PRESENT));
    assertFalse(glob2.get(DummyObject.PRESENT));

    selectionService.select(glob1);
    assertFalse(glob1.get(DummyObject.PRESENT));
    assertFalse(glob2.get(DummyObject.PRESENT));

    toggle.setSelected(true);
    assertTrue(glob1.get(DummyObject.PRESENT));
    assertFalse(glob2.get(DummyObject.PRESENT));

    repository.update(key1, DummyObject.PRESENT, false);
    assertFalse(toggle.isSelected());

    repository.update(key2, DummyObject.PRESENT, true);
    assertFalse(toggle.isSelected());
  }

  public void testDeletingSelectedItems() throws Exception {
    selectionService.select(new GlobList(glob1, glob2), DummyObject.TYPE);
    assertTrue(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    repository.delete(key2);
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());

    toggle.setSelected(false);
    assertFalse(glob1.get(DummyObject.PRESENT));
  }

  public void testForceSelection() throws Exception {

    editor.forceSelection(key1);
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());

    selectionService.select(glob2);
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());

    repository.update(key1, DummyObject.PRESENT, false);
    assertTrue(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    repository.delete(key1);
    assertFalse(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    repository.create(key1, value(DummyObject.PRESENT, true));
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());

    repository.delete(key1);
    assertFalse(toggle.isEnabled());
    assertFalse(toggle.isSelected());

    glob1 = GlobBuilder.create(DummyObject.TYPE,
                               value(DummyObject.ID, 1),
                               value(DummyObject.PRESENT, true));
    repository.reset(new GlobList(glob1), DummyObject.TYPE);
    assertTrue(toggle.isEnabled());
    assertTrue(toggle.isSelected());
  }

  public void testForceSelectionWithNull() throws Exception {
    editor.forceSelection(null);
    assertFalse(toggle.isEnabled());
    assertFalse(toggle.isSelected());
  }
}
