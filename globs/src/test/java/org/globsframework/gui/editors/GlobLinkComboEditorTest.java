package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.model.Glob;
import org.uispec4j.ComboBox;

import java.util.Arrays;

public class GlobLinkComboEditorTest extends GuiComponentTestCase {

  public void testStandardUsage() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals(null));
    assertFalse(combo.isEnabled());

    selectionService.select(glob1);

    assertTrue(combo.isEnabled());
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name2"));

    combo.select("name3");
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertEquals(3, glob1.get(DummyObject.LINK).intValue());

    selectionService.clear(DummyObject.TYPE);
    assertFalse(combo.isEnabled());

    selectionService.select(glob2);

    assertTrue(combo.isEnabled());
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name1"));
  }

  public void testMultiEditionIsNotSupported() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);

    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals(null));
    assertFalse(combo.isEnabled());
  }
}
