package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObjectWithLinks;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.uispec4j.ComboBox;

import java.util.Arrays;
import java.util.Comparator;

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

  public void testWithDifferentGlobType() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>" +
                    "<dummyObjectWithLinks id='1' parentLink='name1'/>" +
                    "<dummyObjectWithLinks id='2' parentLink='name2'/>" +
                    "<dummyObjectWithLinks id='3' parentLink='name2'/>" +
                    "");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(Key.create(DummyObjectWithLinks.TYPE, 1));
    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObjectWithLinks.PARENT_LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);

    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name1"));
    combo.select("name2");
    changeListener.assertLastChangesEqual("<update type='dummyObjectWithLinks' id='1'" +
                                          "          parentId='2' _parentId='1'/>");
  }

  public void testChangeSelectionDoNotCallUpdate() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>" +
                    "<dummyObjectWithLinks id='1' parentLink='name1'/>" +
                    "<dummyObjectWithLinks id='2' parentLink='name2'/>" +
                    "<dummyObjectWithLinks id='3' parentLink='name2'/>" +
                    "");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(Key.create(DummyObjectWithLinks.TYPE, 1));
    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObjectWithLinks.PARENT_LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);
    changeListener.assertNoChanges();

  }

  public void testComparator() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory);
    editor.setShowEmptyOption(false);
    editor.setComparator(new Comparator<Glob>() {
      public int compare(Glob o1, Glob o2) {
        return o2.get(DummyObject.NAME).compareTo(o1.get(DummyObject.NAME));
      }
    });
    ComboBox combo = new ComboBox(editor.getComponent());

    assertTrue(combo.contentEquals("name3", "name2", "name1"));
  }
}
