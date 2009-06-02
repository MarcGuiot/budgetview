package org.globsframework.gui.views;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import static org.globsframework.metamodel.DummyObject.*;
import org.globsframework.metamodel.DummyObject2;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.*;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.TestUtils;
import org.uispec4j.ListBox;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GlobListViewTest extends GuiComponentTestCase {
  private GlobListView view;

  public void testCreationWithEmptyRepository() throws Exception {
    GlobRepository repository = checker.getEmptyRepository();
    ListBox list = createList(repository);
    assertTrue(list.isEmpty());
    assertEquals("dummyObject", list.getName());
  }

  public void testCreationWithExistingGlobs() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);
    assertTrue(list.contentEquals("name1", "name2"));
  }

  public void testModificationEvents() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);
    repository.create(TYPE,
                      value(NAME, "name3"),
                      value(VALUE, 3.3));
    assertTrue(list.contentEquals("name1", "name2", "name3"));

    repository.update(key1, NAME, "newName1");
    assertTrue(list.contentEquals("name2", "name3", "newName1"));

    repository.delete(Key.create(TYPE, 2));
    assertTrue(list.contentEquals("name3", "newName1"));
  }

  public void testDeletionUpdatesSelection() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);
    list.selectIndices(0, 1);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    repository.delete(key2);
    assertTrue(list.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
  }

  public void testMultiDeletionUpdatesSelection() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>");
    ListBox list = createList(repository);
    list.selectIndices(1);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    repository.startChangeSet();
    repository.delete(key1);
    repository.delete(key2);
    repository.completeChangeSet();

    assertTrue(list.selectionIsEmpty());
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "</selection>" +
                          "</log>");
  }

  public void testSelectionAfterCreation() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);
    list.selectIndex(0);

    Glob glob3 = repository.create(TYPE,
                                   value(NAME, "name3"),
                                   value(VALUE, 3.3));
    selectionService.select(glob3);
    assertTrue(list.selectionEquals("name3"));
  }

  public void testListensOnlyToChangesForItsMainType() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>");
    ListBox list = createList(repository);
    Key keyForDummy2 = Key.create(DummyObject2.TYPE, 1);
    repository.create(DummyObject2.TYPE, keyForDummy2.toArray());
    repository.update(keyForDummy2, DummyObject2.LABEL, "label");
    assertTrue(list.contentEquals("name1"));

    repository.delete(keyForDummy2);
    assertTrue(list.contentEquals("name1"));
  }

  public void testModelIsProperlyUpdatedDuringDeleteCreateSequences() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);

    repository.startChangeSet();
    repository.delete(key1);
    repository.create(TYPE, value(NAME, "newName1"));
    repository.completeChangeSet();

    assertTrue(list.contentEquals("name2", "newName1"));
  }

  public void testManagesResetEvents() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    ListBox list = createList(repository);

    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE)
        .set(DummyObject.ID, 3)
        .set(DummyObject.NAME, "name3")
        .get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);
    assertTrue(list.contentEquals("name3"));
  }

  public void testUsingACustomRenderer() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='11' name='name1'/>" +
                    "<dummyObject id='10' name='name2'/>");
    ListBox list = new ListBox(GlobListView.init(TYPE, repository, directory)
      .setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object object, int i, boolean b, boolean b1) {
          Glob glob = (Glob)object;
          String value = "[" + glob.get(ID) + "] " + glob.get(NAME);
          return super.getListCellRendererComponent(list, value, i, b, b1);
        }
      }, new GlobFieldComparator(NAME))
      .getComponent());
    assertTrue(list.contentEquals("[11] name1", "[10] name2"));
  }

  public void testDefaultRendererUsesTheDescriptionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject2 id='1' label='name1'/>" +
                    "<dummyObject2 id='2' label='name2'/>");
    ListBox list = new ListBox(GlobListView.init(DummyObject2.TYPE, repository, directory).getComponent());
    assertTrue(list.contentEquals("dummyObject2[id=1]", "dummyObject2[id=2]"));
  }

  public void testDefaultRendererUsesSpaceForEmptyStringsToAvoidCollapsedRows() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject2 id='1' label='name1'/>" +
                    "<dummyObject2 id='2' label='name2'/>");
    ListBox list = new ListBox(GlobListView.init(DummyObject2.TYPE, repository, directory)
      .setRenderer(new AbstractGlobStringifier() {
        public String toString(Glob glob, GlobRepository repository) {
          return "";
        }
      })
      .getComponent());
    assertTrue(list.contentEquals(" ", " "));
  }

  public void testUserSelection() throws Exception {
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");

    ListBox listBox = createList(repository);
    listBox.select("name1");
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    TestUtils.assertEquals(view.getCurrentSelection(),
                           repository.get(key1));

    listBox.select("name1", "name2");
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
    TestUtils.assertEquals(view.getCurrentSelection(),
                           repository.get(key1),
                           repository.get(key2));

    listBox.clearSelection();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");
    assertTrue(view.getCurrentSelection().isEmpty());
  }

  public void testProgramSelection() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    ListBox listBox = createList(repository);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.select(glob1);
    assertTrue(listBox.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
    TestUtils.assertEquals(view.getCurrentSelection(),
                           repository.get(key1));

    view.select(glob1, glob2);
    assertTrue(listBox.selectionEquals("name1", "name2"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
    TestUtils.assertEquals(view.getCurrentSelection(),
                           repository.get(key1),
                           repository.get(key2));

    view.selectFirst();
    assertTrue(listBox.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    view.select();
    assertTrue(listBox.selectionIsEmpty());
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");
    assertTrue(view.getCurrentSelection().isEmpty());

    view.select(glob1);
    assertTrue(listBox.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    view.select(repository.create(DummyObject2.TYPE, value(DummyObject2.LABEL, "unknown object")));
  }

  public void testSelectFirstWithEmptyList() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.createEmpty();
    ListBox listBox = createList(repository);
    assertTrue(listBox.isEmpty());
    assertTrue(listBox.selectionIsEmpty());

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.selectFirst();

    assertTrue(listBox.selectionIsEmpty());
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "</selection>" +
                          "</log>");
  }

  public void testProgramSelectionWorksEvenWithDeletedGlobs() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    ListBox listBox = createList(repository);

    view.select(glob1);
    assertTrue(listBox.selectionEquals("name1"));

    repository.delete(glob2.getKey());

    Glob newGlob2 = repository.create(DummyObject.TYPE, value(DummyObject.NAME, "name2"));
    view.select(newGlob2);
    assertTrue(listBox.selectionEquals("name2"));
  }

  public void testSelectionThroughSelectionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    ListBox listBox = createList(repository);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    selectionService.select(glob1);
    assertTrue(listBox.selectionEquals("name1"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(listBox.selectionEquals("name1", "name2"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(listBox.selectionEquals("name1", "name2"));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
  }

  public void testDisablingSelectionThroughSelectionService() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    view = GlobListView.init(TYPE, repository, directory).setUpdateWithIncomingSelections(false);
    ListBox listBox = new ListBox(view.getComponent());

    selectionService.select(glob1);
    assertTrue(listBox.selectionIsEmpty());
  }

  public void testSingleSelection() throws Exception {
    GlobRepository repository = GlobRepositoryBuilder.init().get();
    view = GlobListView.init(TYPE, repository, directory).setSingleSelectionMode();
    JList list = view.getComponent();
    assertEquals(ListSelectionModel.SINGLE_SELECTION, list.getSelectionMode());
  }

  public void testSelectionIsPreservedWhenTheOrderOfItemsIsChanged() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");

    ListBox listBox = createList(repository);
    selectionService.select(repository.get(key1));
    assertThat(listBox.selectionEquals("name1"));

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    repository.startChangeSet();
    repository.update(key1, DummyObject.NAME, "newName1");
    repository.update(key2, DummyObject.NAME, "aNewName2");
    repository.completeChangeSet();
    assertTrue(listBox.contentEquals("aNewName2", "newName1"));
    assertTrue(listBox.selectionEquals("newName1"));
    listener.assertEmpty();

    repository.update(key2, DummyObject.NAME, "zeNewName2");
    assertTrue(listBox.contentEquals("newName1", "zeNewName2"));
    assertTrue(listBox.selectionEquals("newName1"));
    listener.assertEmpty();

    repository.update(key1, DummyObject.NAME, "zeRenamedName1");
    assertTrue(listBox.contentEquals("zeNewName2", "zeRenamedName1"));
    assertTrue(listBox.selectionEquals("zeRenamedName1"));
    listener.assertEmpty();

    repository.update(key1, DummyObject.NAME, "zeNewName1");
    assertTrue(listBox.contentEquals("zeNewName1", "zeNewName2"));
    assertTrue(listBox.selectionEquals("zeNewName1"));
    listener.assertEmpty();
  }

  public void testFiltering() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject name='name1'/>" +
                    "<dummyObject name='name2'/>" +
                    "<dummyObject name='name21'/>");
    ListBox listBox = createList(repository);
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

  public void testSetFilterSendsSelectionEvents() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject name='name1'/>" +
                    "<dummyObject name='name2'/>" +
                    "<dummyObject name='name21'/>");
    ListBox listBox = createList(repository);
    assertTrue(listBox.contentEquals("name1",
                                     "name2",
                                     "name21"));
    listBox.selectIndices(0, 1, 2);
    DummySelectionListener listener = DummySelectionListener.register(directory, DummyObject.TYPE);

    view.setFilter(createNameMatcher("1"));
    assertTrue(listBox.contentEquals("name1", "name21"));
    assertTrue(listBox.selectionEquals("name1", "name21"));
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "    <item key='dummyObject[id=100]'/>" +
                          "    <item key='dummyObject[id=102]'/>" +
                          "  </selection>" +
                          "</log>");

    view.setFilter(GlobMatchers.ALL);
    assertTrue(listBox.contentEquals("name1",
                                     "name2",
                                     "name21"));
    assertTrue(listBox.selectionEquals("name1", "name21"));
    listener.assertEmpty();

    view.setFilter(createNameMatcher("2"));
    assertTrue(listBox.selectionEquals("name21"));
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "    <item key='dummyObject[id=102]'/>" +
                          "  </selection>" +
                          "</log>");

    view.setFilter(createNameMatcher("3"));
    assertTrue(listBox.selectionIsEmpty());
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'>" +
                          "  </selection>" +
                          "</log>");
  }

  public void testSelectionIsClearedByNewFilter() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject name='name1'/>" +
                    "<dummyObject name='name2'/>" +
                    "<dummyObject name='name21'/>");
    ListBox listBox = createList(repository);
    assertTrue(listBox.contentEquals("name1",
                                     "name2",
                                     "name21"));
    listBox.selectIndices(0);
    view.setFilter(createNameMatcher("2"));
    assertTrue(listBox.selectionIsEmpty());
  }

  public void testFilteringIsTakenIntoAccountDuringUpdates() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    ListBox list = createList(repository);
    view.setFilter(createNameMatcher("3"));
    assertTrue(list.isEmpty());

    repository.create(TYPE, value(NAME, "name3"), value(VALUE, 3.3));
    assertTrue(list.contentEquals("name3"));

    repository.update(key1, NAME, "name1_3");
    assertTrue(list.contentEquals("name1_3", "name3"));

    repository.delete(Key.create(TYPE, 2));
    assertTrue(list.contentEquals("name1_3", "name3"));

    repository.delete(Key.create(TYPE, 1));
    assertTrue(list.contentEquals("name3"));
  }

  private ListBox createList(GlobRepository repository) {
    view = GlobListView.init(TYPE, repository, directory).setRenderer(NAME);
    return new ListBox(view.getComponent());
  }
}
