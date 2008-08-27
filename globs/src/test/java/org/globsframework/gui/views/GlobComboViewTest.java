package org.globsframework.gui.views;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import static org.globsframework.metamodel.DummyObject.TYPE;
import org.globsframework.metamodel.DummyObject2;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import static org.globsframework.model.KeyBuilder.newKey;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.uispec4j.ComboBox;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GlobComboViewTest extends GuiComponentTestCase {
  private GlobComboView view;

  public void testCreationWithEmptyRepository() throws Exception {
    GlobRepository repository = checker.getEmptyRepository();
    ComboBox combo = createCombo(repository);
    assertThat(combo.isEmpty(""));
    assertEquals("dummyObject", combo.getName());
  }

  public void testCreationWithExistingGlobs() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox combo = createCombo(repository);
    assertThat(combo.selectionEquals("name1"));
    assertThat(combo.contentEquals("name1", "name2"));
  }

  public void testModificationEvents() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox combo = createCombo(repository);

    repository.create(DummyObject.TYPE,
                      value(DummyObject.NAME, "name3"),
                      value(DummyObject.VALUE, 3.3));
    assertThat(combo.contentEquals("name1", "name2", "name3"));

    repository.update(key1, DummyObject.NAME, "newName1");
    assertThat(combo.contentEquals("name2", "name3", "newName1"));

    repository.delete(newKey(DummyObject.TYPE, 2));
    assertThat(combo.contentEquals("name3", "newName1"));
  }

  public void testTableListensOnlyToChangesForItsMainType() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>");
    ComboBox combo = createCombo(repository);
    Key keyForDummy2 = newKey(DummyObject2.TYPE, 1);
    repository.create(DummyObject2.TYPE, keyForDummy2.toArray());
    repository.update(keyForDummy2, DummyObject2.LABEL, "label");
    assertThat(combo.contentEquals("name1"));

    repository.delete(keyForDummy2);
    assertThat(combo.contentEquals("name1"));
  }

  public void testModelIsProperlyUpdatedDuringDeleteCreateSequences() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox combo = createCombo(repository);

    repository.enterBulkDispatchingMode();
    repository.delete(key1);
    repository.create(DummyObject.TYPE, value(DummyObject.NAME, "newName1"));
    repository.completeBulkDispatchingMode();

    assertThat(combo.contentEquals("name2", "newName1"));
  }

  public void testUsingACustomRenderer() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='11' name='name1'/>" +
                    "<dummyObject id='10' name='name2'/>ll;k…");
    GlobComboView view =
      GlobComboView
        .init(DummyObject.TYPE, repository, directory)
        .setRenderer(new DefaultListCellRenderer() {
          public Component getListCellRendererComponent(JList combo, Object object, int i, boolean b, boolean b1) {
            Glob glob = (Glob)object;
            String value = "[" + glob.get(DummyObject.ID) + "] " + glob.get(DummyObject.NAME);
            return super.getListCellRendererComponent(combo, value, i, b, b1);
          }
        }, new GlobFieldComparator(DummyObject.NAME));
    ComboBox combo = new ComboBox(view.getComponent());
    assertThat(combo.selectionEquals("[11] name1"));
    assertThat(combo.contentEquals("[11] name1", "[10] name2"));
  }

  public void testUsingACustomComparator() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='11' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    view =
      GlobComboView.init(DummyObject.TYPE, repository, directory)
        .setComparator(new GlobFieldComparator(DummyObject.ID));
    ComboBox combo = new ComboBox(view.getComponent());
    assertThat(combo.contentEquals("name2", "name1"));
  }

  public void testDefaultRendererUsesTheDescriptionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject2 id='1' label='name1'/>" +
                    "<dummyObject2 id='2' label='name2'/>");
    ComboBox combo = new ComboBox(GlobComboView.init(DummyObject2.TYPE, repository, directory).getComponent());
    assertThat(combo.contentEquals("dummyObject2[id=1]", "dummyObject2[id=2]"));
  }

  public void testUserSelection() throws Exception {
    DummySelectionListener listener = DummySelectionListener.register(directory, DummyObject.TYPE);
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox listBox = createCombo(repository);

    listBox.select("name1");
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "    <item key='dummyObject[id=1]'/>" +
                          "  </selection>" +
                          "</log>");
    assertEquals(repository.get(key1), view.getCurrentSelection());

    listBox.select("name2");
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "    <item key='dummyObject[id=2]'/>" +
                          "  </selection>" +
                          "</log>");
    assertEquals(repository.get(key2), view.getCurrentSelection());
  }

  public void testProgramSelection() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox listBox = createCombo(repository);

    Glob glob2 = repository.get(key2);
    view.select(glob2);
    assertTrue(listBox.selectionEquals("name2"));
    assertEquals(glob2, view.getCurrentSelection());

    view.select(repository.create(DummyObject2.TYPE, value(DummyObject2.LABEL, "unknown object")));
    assertTrue(listBox.selectionEquals("name2"));
  }

  public void testSelectionThroughSelectionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    view = GlobComboView.init(DummyObject.TYPE, repository, directory).setShowEmptyOption(true);
    ComboBox comboBox = new ComboBox(view.getComponent());

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    selectionService.select(glob1);
    assertThat(comboBox.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
    assertEquals(glob1, view.getCurrentSelection());

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertThat(comboBox.selectionEquals(null));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
    assertNull(view.getCurrentSelection());

    selectionService.select(glob2);
    assertThat(comboBox.selectionEquals("name2"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
    assertEquals(glob2, view.getCurrentSelection());
  }

  public void testDisablingSelectionThroughSelectionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    view = GlobComboView.init(TYPE, repository, directory)
      .setUpdateWithIncomingSelections(false)
      .setShowEmptyOption(true);
    ComboBox listBox = new ComboBox(view.getComponent());

    selectionService.select(glob1);
    assertTrue(listBox.selectionEquals(null));
  }

  public void testProgramSelectionWorksWithDeletedGlobs() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    ComboBox listBox = createCombo(repository);

    view.select(glob1);
    assertTrue(listBox.selectionEquals("name1"));

    repository.delete(glob2.getKey());

    Glob newGlob2 = repository.create(DummyObject.TYPE, value(DummyObject.NAME, "name2"));
    view.select(newGlob2);
    assertTrue(listBox.selectionEquals("name2"));
  }

  public void testFiltering() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='21' name='name21'/>");
    ComboBox listBox = createCombo(repository);
    view.setFilter(createNameMatcher("1"));
    assertTrue(listBox.contentEquals("name1",
                                     "name21"));

    view.setFilter(createNameMatcher("21"));
    assertTrue(listBox.contentEquals("name21"));

    view.setFilter(GlobMatchers.ALL);
    assertTrue(listBox.contentEquals("name1",
                                     "name2",
                                     "name21"));
  }

  public void testFilteringIsTakenIntoAccountDuringUpdates() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ComboBox combo = createCombo(repository);
    view.setFilter(createNameMatcher("3"));
    assertThat(combo.isEmpty(""));

    repository.create(DummyObject.TYPE,
                      value(DummyObject.NAME, "name3"),
                      value(DummyObject.VALUE, 3.3));
    assertThat(combo.contentEquals("name3"));

    repository.update(key1, DummyObject.NAME, "name1_3");
    assertThat(combo.contentEquals("name1_3", "name3"));

    repository.delete(newKey(DummyObject.TYPE, 2));
    assertThat(combo.contentEquals("name1_3", "name3"));

    repository.delete(newKey(DummyObject.TYPE, 1));
    assertThat(combo.contentEquals("name3"));
  }

  public void testSelectionIsReinitializedAfterAFilterChange() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='10' name='name10'/>" +
                    "<dummyObject id='11' name='name11'/>" +
                    "<dummyObject id='12' name='name12'/>" +
                    "<dummyObject id='20' name='name20'/>");
    ComboBox combo = createCombo(repository);
    view.setFilter(createNameMatcher("1"));
    assertThat(combo.selectionEquals("name10"));

    view.setFilter(createNameMatcher("2"));
    assertThat(combo.selectionEquals("name12"));

    view.setFilter(createNameMatcher("0"));
    assertThat(combo.selectionEquals("name10"));

    view.setFilter(createNameMatcher("x"));
    assertThat(combo.isEmpty(""));

    view.setFilter(createNameMatcher("1"));
    assertThat(combo.selectionEquals("name10"));
  }

  public void testAddingNullOption() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    view =
      GlobComboView.init(DummyObject.TYPE, repository, directory)
        .setShowEmptyOption(true);
    ComboBox combo = new ComboBox(view.getComponent());
    assertThat(combo.selectionEquals(null));
    assertThat(combo.contentEquals("", "name1", "name2"));

    combo.select("name1");
    assertThat(combo.selectionEquals("name1"));

    combo.select("");
    assertThat(combo.selectionEquals(null));
  }

  public void testSettingANullOptionLabel() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    view =
      GlobComboView.init(DummyObject.TYPE, repository, directory)
        .setShowEmptyOption(true)
        .setEmptyOptionLabel("empty");

    ComboBox combo = new ComboBox(view.getComponent());
    assertThat(combo.selectionEquals(null));
    assertThat(combo.contentEquals("empty", "name1", "name2"));

    combo.select("name1");
    assertThat(combo.selectionEquals("name1"));

    combo.select("empty");
    assertThat(combo.selectionEquals(null));
  }

  public void testReset() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    view =
      GlobComboView.init(DummyObject.TYPE, repository, directory);
    ComboBox combo = new ComboBox(view.getComponent());
    combo.select("name2");
    DummyGlobSelectionHandler globSelectionHandler = new DummyGlobSelectionHandler();
    view.setSelectionHandler(globSelectionHandler);
    DummySelectionListener dummySelectionListener = DummySelectionListener.register(directory, DummyObject.TYPE);
    repository.reset(GlobList.EMPTY, DummyObject2.TYPE);
    assertThat(combo.contentEquals("name1", "name2"));
    assertThat(combo.selectionEquals("name2"));
    dummySelectionListener.assertEmpty();
    assertTrue(globSelectionHandler.isEmpty());
  }

  private ComboBox createCombo(GlobRepository repository) {
    view = GlobComboView.init(DummyObject.TYPE, repository, directory).setRenderer(DummyObject.NAME);
    return new ComboBox(view.getComponent());
  }

  private static class DummyGlobSelectionHandler implements GlobComboView.GlobSelectionHandler {
    private boolean empty = true;

    public void processSelection(Glob glob) {
      empty = false;
    }

    public boolean isEmpty() {
      return empty;
    }
  }
}
