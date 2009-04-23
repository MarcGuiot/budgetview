package org.globsframework.gui.editors;

import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.gui.DummySelectionListener;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObjectWithLinks;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.Key;
import org.globsframework.model.utils.GlobBuilder;
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

  public void testChangeSelectionDoesNotCallUpdate() throws Exception {
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

  public void testListensToChanges() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);
    assertTrue(combo.selectionEquals("name2"));

    repository.update(key1, DummyObject.LINK, 3);
    assertTrue(combo.selectionEquals("name3"));
    assertThat(combo.isEnabled());

    repository.update(key1, DummyObject.LINK, null);
    assertTrue(combo.selectionEquals(null));
    assertThat(combo.isEnabled());

    repository.update(key1, DummyObject.LINK, 3);
    assertTrue(combo.selectionEquals("name3"));
    assertThat(combo.isEnabled());

    repository.delete(key1);
    assertTrue(combo.selectionEquals(null));
    assertFalse(combo.isEnabled());
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

  public void testResetClearsTheCurrentSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name2"));

    DummySelectionListener selectionListener = DummySelectionListener.register(selectionService, DummyObject.TYPE);

    repository.reset(new GlobList(GlobBuilder.create(DummyObject.TYPE,
                                                     value(DummyObject.ID, 1),
                                                     value(DummyObject.LINK, 3),
                                                     value(DummyObject.NAME, "newName1")),
                                  GlobBuilder.create(DummyObject.TYPE,
                                                     value(DummyObject.ID, 3),
                                                     value(DummyObject.NAME, "newName3"))),
                     DummyObject.TYPE);

    changeListener.assertNoChanges();
    assertFalse(combo.isEnabled());
    assertThat(combo.selectionEquals(null));
    selectionListener.assertEmpty();
  }

  public void testForbiddingEmptyValues() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory)
      .setShowEmptyOption(false);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);
    assertThat(combo.contentEquals("name1", "name2", "name3"));
    
    editor.setShowEmptyOption(true);
    assertThat(combo.contentEquals("", "name1", "name2", "name3"));

    editor.setShowEmptyOption(false);
    assertThat(combo.contentEquals("name1", "name2", "name3"));
  }

  public void testForcedSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' link='2'/>" +
                    "<dummyObject id='2' name='name2' link='1'/>" +
                    "<dummyObject id='3' name='name3'/>");
    repository.addChangeListener(changeListener);
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    GlobLinkComboEditor editor = new GlobLinkComboEditor(DummyObject.LINK, repository, directory).forceSelection(glob2);
    ComboBox combo = new ComboBox(editor.getComponent());

    selectionService.select(glob1);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name1"));

    combo.select("name3");
    assertEquals(2, glob1.get(DummyObject.LINK).intValue());
    assertEquals(3, glob2.get(DummyObject.LINK).intValue());

    selectionService.select(glob1);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name3"));

    editor.forceSelection(glob1);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name2"));
    combo.select("name1");

    selectionService.select(glob2);
    assertTrue(combo.selectionEquals("name1"));

    editor.forceSelection(glob2);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name3"));

    editor.forceSelection(glob1);
    assertTrue(combo.contentEquals("", "name1", "name2", "name3"));
    assertTrue(combo.selectionEquals("name1"));
  }
}
