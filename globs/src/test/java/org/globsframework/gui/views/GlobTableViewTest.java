package org.globsframework.gui.views;

import org.globsframework.gui.DummySelectionListener;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.gui.views.impl.SortingIcon;
import org.globsframework.gui.views.utils.LabelCustomizers;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.metamodel.DummyObjectWithLinks;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.model.format.GlobStringifier;
import org.globsframework.model.format.GlobStringifiers;
import org.globsframework.model.format.utils.AbstractGlobStringifier;
import org.globsframework.model.utils.GlobBuilder;
import org.globsframework.model.utils.GlobFieldComparator;
import org.globsframework.model.utils.GlobMatcher;
import org.globsframework.model.utils.GlobMatchers;
import org.uispec4j.Clipboard;
import org.uispec4j.Table;
import org.uispec4j.Trigger;
import org.uispec4j.interception.PopupMenuInterceptor;
import org.uispec4j.interception.WindowHandler;
import org.uispec4j.interception.WindowInterceptor;
import org.uispec4j.utils.KeyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.Comparator;

import static org.globsframework.metamodel.DummyObject.*;
import static org.globsframework.model.FieldValue.value;
import static org.globsframework.model.KeyBuilder.newKey;

public class GlobTableViewTest extends GuiComponentTestCase {
  private GlobTableView view;

  public void testCreationWithEmptyRepository() throws Exception {
    repository = checker.getEmptyRepository();
    Table table = createTableWithNameAndValueColumns(repository);
    assertTrue(table.getHeader().contentEquals("name", "value"));
    assertTrue(table.isEmpty());
    assertEquals(DummyObject.TYPE.getName(), table.getName());
  }

  public void testCreationWithExistingGlobs() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    assertTrue(table.getHeader().contentEquals("name", "value"));

    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1"},
      {"name2", "2.2"},
    }));
  }

  public void testModificationEvents() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    repository.create(TYPE,
                      value(NAME, "name3"),
                      value(VALUE, 3.3));
    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1"},
      {"name2", "2.2"},
      {"name3", "3.3"},
    }));

    repository.update(key1, NAME, "newName1");
    assertTrue(table.contentEquals(new String[][]{
      {"name2", "2.2"},
      {"name3", "3.3"},
      {"newName1", "1.1"},
    }));

    repository.delete(newKey(TYPE, 2));
    assertTrue(table.contentEquals(new String[][]{
      {"name3", "3.3"},
      {"newName1", "1.1"},
    }));
  }

  public void testViewListensOnlyToChangesForItsMainType() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>");
    Table table = createTableWithNameAndValueColumns(repository);
    Key keyForDummy2 = newKey(DummyObject2.TYPE, 1);
    repository.create(DummyObject2.TYPE, keyForDummy2.toArray());
    repository.update(keyForDummy2, DummyObject2.LABEL, "label");
    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1"},
    }));

    repository.delete(keyForDummy2);
    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1"},
    }));
  }

  public void testSingleDeleteCreate() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    repository.startChangeSet();
    repository.delete(key1);
    repository.create(TYPE, value(NAME, "newName1"), value(VALUE, 1.0));
    repository.completeChangeSet();

    assertTrue(table.contentEquals(new String[][]{
      {"name2", "2.2"},
      {"newName1", "1"}
    }));
  }

  public void testMultipleDeleteCreate() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    repository.startChangeSet();
    repository.delete(key1);
    repository.create(TYPE, value(NAME, "newName1"), value(VALUE, 1.0));
    repository.delete(key2);
    repository.create(TYPE, value(NAME, "newName2"), value(VALUE, 2.0));
    repository.completeChangeSet();

    assertTrue(table.contentEquals(new String[][]{
      {"newName1", "1"},
      {"newName2", "2"}
    }));
  }

  public void testManagesResetEvents() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);
    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE)
        .set(DummyObject.ID, 3)
        .set(DummyObject.NAME, "name3")
        .set(DummyObject.VALUE, 3.3)
        .get();
    repository.reset(new GlobList(dummyObject3), DummyObject.TYPE);
    assertTrue(table.contentEquals(new String[][]{
      {"name3", "3.3"},
    }));
  }

  public void testInitialFilter() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    view = GlobTableView.init(DummyObject.TYPE, repository, new GlobFieldComparator(NAME), directory)
      .setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name1"))
      .addColumn(DummyObject.NAME)
      .addColumn(DummyObject.VALUE);
    Table table = new Table(view.getComponent());
    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1"},
    }));
  }

  public void testKeepsFilterWhenHandlingResetEvents() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);
    view.setFilter(new GlobMatcher() {
      public boolean matches(Glob item, GlobRepository repository) {
        return item.get(DummyObject.NAME).endsWith("3");
      }
    });
    Glob dummyObject3 =
      GlobBuilder.init(DummyObject.TYPE)
        .set(DummyObject.ID, 3)
        .set(DummyObject.NAME, "name3")
        .set(DummyObject.VALUE, 3.3)
        .get();
    Glob dummyObject4 =
      GlobBuilder.init(DummyObject.TYPE)
        .set(DummyObject.ID, 4)
        .set(DummyObject.NAME, "name4")
        .set(DummyObject.VALUE, 4.4)
        .get();
    repository.reset(new GlobList(dummyObject3, dummyObject4), DummyObject.TYPE);
    assertTrue(table.contentEquals(new String[][]{
      {"name3", "3.3"},
    }));
  }

  public void testUsingACustomRenderer() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table =
      createTable(
        GlobTableView
          .init(TYPE, repository, new GlobFieldComparator(NAME), directory)
          .addColumn("colName", new DefaultTableCellRenderer() {
                       public Component getTableCellRendererComponent(JTable table, Object object,
                                                                      boolean b, boolean b1, int i, int i1) {
                         Glob glob = (Glob)object;
                         String value = "[" + glob.get(ID) + "] " + glob.get(NAME);
                         return super.getTableCellRendererComponent(table, value, b, b1, i, i1);
                       }
                     }, GlobStringifiers.get(NAME)));

    assertTrue(table.getHeader().contentEquals("colName"));
    assertTrue(table.contentEquals(new String[][]{
      {"[1] name1"},
      {"[2] name2"},
    }));
  }

  public void testDeclaringAColumnWithAStringifier() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Table table =
      createTable(
        GlobTableView
          .init(TYPE, repository, new GlobFieldComparator(NAME), directory)
          .addColumn("theName",
                     new AbstractGlobStringifier() {
                       public String toString(Glob glob, GlobRepository repository) {
                         return "[" + glob.get(NAME) + "]";
                       }
                     },
                     LabelCustomizers.ALIGN_LEFT));
    assertTrue(table.contentEquals(new String[][]{
      {"[name1]"},
      {"[name2]"},
    }));

    checkColumnIsNotRightAligned(table, 0);
  }

  public void testDeclaringAColumnWithAStringifierAndAlignmentProperty() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Table table =
      createTable(
        GlobTableView
          .init(TYPE, repository, new GlobFieldComparator(NAME), directory)
          .addColumn("leftAligned", new DummyStringifier(), LabelCustomizers.ALIGN_LEFT)
          .addColumn("centerAligned", new DummyStringifier(), LabelCustomizers.ALIGN_CENTER)
          .addColumn("rightAligned", new DummyStringifier(), LabelCustomizers.ALIGN_RIGHT));

    checkColumnAlignment(table, 0, JLabel.LEFT);
    checkColumnAlignment(table, 1, JLabel.CENTER);
    checkColumnAlignment(table, 2, JLabel.RIGHT);
  }

  public void testLinkColumn() throws Exception {
    repository =
      checker.parse("<dummyObjectWithCompositeKey id1='1' id2='2' name='target'/>" +
                    "<dummyObjectWithLinks id='1' targetId1='1' targetId2='2'/>" +
                    "<dummyObjectWithLinks id='2'/>");
    Table table =
      createTable(GlobTableView
                    .init(DummyObjectWithLinks.TYPE, repository, new GlobFieldComparator(DummyObjectWithLinks.ID), directory)
                    .addColumn(DummyObjectWithLinks.COMPOSITE_LINK));
    assertTrue(table.getHeader().contentEquals(DummyObjectWithLinks.COMPOSITE_LINK.getName()));
    assertTrue(table.contentEquals(new Object[][]{
      {"target"},
      {""}
    }));
  }

  public void testNumbersAreRightAligned() throws Exception {
    final int ID_COLUMN = 0;
    final int NAME_COLUMN = 1;
    final int PRESENT_COLUMN = 2;
    final int VALUE_COLUMN = 3;
    final int DATE_COLUMN = 4;

    repository =
      checker.parse("<dummyObject id='1' name='name1' present='true' value='101.11' date='2006/08/26'/>");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(ID)
        .addColumn(NAME)
        .addColumn(PRESENT)
        .addColumn(VALUE)
        .addColumn(DATE);

    Table table = new Table(view.getComponent());

    checkColumnIsNotRightAligned(table, NAME_COLUMN);
    checkColumnIsNotRightAligned(table, PRESENT_COLUMN);
    checkColumnIsNotRightAligned(table, DATE_COLUMN);

    checkColumnIsRightAligned(table, ID_COLUMN);
    checkColumnIsRightAligned(table, VALUE_COLUMN);
  }

  public void testUserSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);
    table.selectRow(0);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    table.selectRows(0, 1);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    table.clearSelection();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");
  }

  public void testSelect() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    Table table = createTableWithNameAndValueColumns(repository);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.select(glob1);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    view.select(glob1, glob2);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    table.clearSelection();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");

    view.select(glob2);
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    view.clearSelection();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");

  }

  public void testSelectFirst() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");

    Table table = createTableWithNameAndValueColumns(repository);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.selectFirst();
    assertTrue(table.rowIsSelected(0));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    view.setFilter(GlobMatchers.NONE);
    listener.reset();

    view.selectFirst();
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'/>" +
                          "</log>");
  }

  public void testSelectionThroughSelectionService() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);
    Table table = createTableWithNameAndValueColumns(repository);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    selectionService.select(glob1);
    assertTrue(table.rowIsSelected(0));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(table.rowsAreSelected(0, 1));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(table.rowsAreSelected(0, 1));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");
  }

  public void testSelectionIsPreservedIfMatchedByNewFilter() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    Table table = createTableWithNameAndValueColumns(repository);
    selectionService.select(glob1);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);
    view.setFilter(GlobMatchers.fieldEquals(DummyObject.ID, 1));
    assertTrue(table.rowIsSelected(0));
    listener.assertEmpty();

    view.setFilter(GlobMatchers.ALL);
    assertTrue(table.rowIsSelected(0));
    listener.assertEmpty();

    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    assertTrue(table.rowsAreSelected(0, 1));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "<item key='dummyObject[id=2]'/>" +
                          "</selection>" +
                          "</log>");

    view.setFilter(GlobMatchers.fieldEquals(DummyObject.ID, 1));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
  }

  public void testSelectionIsAdjustedIfChangedByNewFilter() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    Table table = createTableWithNameAndValueColumns(repository);
    selectionService.select(Arrays.asList(glob1, glob2), DummyObject.TYPE);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.setFilter(GlobMatchers.fieldEquals(DummyObject.ID, 1));
    assertTrue(table.rowIsSelected(0));
    listener.assertEquals("<log>" +
                          "<selection types='dummyObject'>" +
                          "<item key='dummyObject[id=1]'/>" +
                          "</selection>" +
                          "</log>");
  }

  public void testEmptySelectionSentIfNewFilterDoesNotMatchCurrentSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    createTableWithNameAndValueColumns(repository);
    selectionService.select(repository.get(key1));
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    view.setFilter(GlobMatchers.fieldEquals(DummyObject.ID, 2));
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'/>" +
                          "</log>");
    assertTrue(view.getCurrentSelection().isEmpty());

    listener.reset();

    view.setFilter(GlobMatchers.ALL);
    listener.assertEmpty();
  }

  public void testEmptySelectionSentIfSelectedObjectIsChangedAndDoesNotMatchFilter() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name'/>" +
                    "<dummyObject id='2' name='name'/>");
    createTableWithNameAndValueColumns(repository);
    view.setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name"));
    selectionService.select(repository.get(key1));

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);
    repository.update(key1, DummyObject.NAME, "newName");
    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'/>" +
                          "</log>");
    assertTrue(view.getCurrentSelection().isEmpty());
  }

  public void testEmptySelectionSentIfMultipleSelectedObjectsAreChangedAndDoNotMatchFilter() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name'/>" +
                    "<dummyObject id='2' name='name'/>" +
                    "<dummyObject id='3' name='name'/>" +
                    "<dummyObject id='4' name='name'/>");
    createTableWithNameAndValueColumns(repository);
    view.setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name"));
    view.select(repository.get(key1));

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);
    repository.startChangeSet();
    repository.update(Key.create(DummyObject.TYPE, 1), DummyObject.NAME, "newName");
    repository.update(Key.create(DummyObject.TYPE, 2), DummyObject.NAME, "newName");
    repository.update(Key.create(DummyObject.TYPE, 3), DummyObject.NAME, "newName");
    repository.completeChangeSet();

    listener.assertEquals("<log>" +
                          "  <selection types='dummyObject'/>" +
                          "</log>");
    assertTrue(view.getCurrentSelection().isEmpty());
  }

  public void testModelUpdatePreservesSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Glob glob1 = repository.get(key1);
    Glob glob2 = repository.get(key2);

    Table table = createTableWithNameAndValueColumns(repository);
    selectionService.select(glob1);

    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    repository.startChangeSet();
    repository.update(glob1.getKey(), DummyObject.NAME, "newName1");
    repository.update(glob2.getKey(), DummyObject.NAME, "aNewName2");
    repository.completeChangeSet();

    listener.assertEmpty();
    assertTrue(table.contentEquals(new String[][]{
      {"aNewName2", "2.2"},
      {"newName1", "1.1"},
    }));
    table.rowIsSelected(0);

    repository.update(glob2.getKey(), DummyObject.NAME, "zeNewName2");

    listener.assertEmpty();
    assertTrue(table.contentEquals(new String[][]{
      {"newName1", "1.1"},
      {"zeNewName2", "2.2"},
    }));
    table.rowIsSelected(0);
  }

  public void testSorting() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='0.23' date='2006/08/14'/>" +
                    "<dummyObject id='2' name='aName' value='0.1' date='2006/08/01'/>" +
                    "<dummyObject id='3' name='yetAnotherName' value='2.2' date='2006/08/05'/>");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(ID)
        .addColumn(NAME)
        .addColumn(VALUE)
        .addColumn(DATE);

    Table table = new Table(view.getComponent());
    assertTrue(table.getHeader().contentEquals(
      "id", "name", "value", "date"));
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("value");
    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("value");
    assertTrue(table.contentEquals(new String[][]{
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
    }));

    table.getHeader().click("date");
    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
    }));

    table.getHeader().click("date");
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
      {"2", "aName", "0.1", "2006/08/01"},
    }));
  }

  public void testResetSort() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='0.23' date='2006/08/14'/>" +
                    "<dummyObject id='2' name='aName' value='0.1' date='2006/08/01'/>" +
                    "<dummyObject id='3' name='yetAnotherName' value='2.2' date='2006/08/05'/>");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(ID)
        .addColumn(NAME)
        .addColumn(VALUE)
        .addColumn(DATE);

    Table table = new Table(view.getComponent());
    assertTrue(table.getHeader().contentEquals(
      "id", "name", "value", "date"));
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));
    view.resetSort();
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));
  }

  public void testRefreshUpdatesTheSortingAndKeepsSelection() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='0.23' date='2006/08/14'/>" +
                    "<dummyObject id='2' name='aName' value='0.1' date='2006/08/01'/>" +
                    "<dummyObject id='3' name='yetAnotherName' value='2.2' date='2006/08/05'/>");

    DummyStringifier stringifier = new DummyStringifier();

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(ID)
        .addColumn("name", stringifier);

    Table table = new Table(view.getComponent());
    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"1", "a1"},
      {"2", "a2"},
      {"3", "a3"}
    }));
    table.selectRows(0, 1);
    DummySelectionListener listener = DummySelectionListener.register(directory, TYPE);

    stringifier.setReversed(true);
    assertTrue(table.contentEquals(new String[][]{
      {"1", "a3"},
      {"2", "a2"},
      {"3", "a1"}
    }));
    assertTrue(table.rowsAreSelected(0, 1));
    listener.assertEmpty();

    view.refresh(false);
    assertTrue(table.contentEquals(new String[][]{
      {"3", "a1"},
      {"2", "a2"},
      {"1", "a3"}
    }));
    assertTrue(table.rowsAreSelected(1, 2));
    listener.assertEmpty();
  }

  public void testSortingDisabled() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='0.23'/>" +
                    "<dummyObject id='2' name='aName' value='0.1'/>" +
                    "<dummyObject id='3' name='yetAnotherName' value='2.2'/>");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .setHeaderActionsDisabled()
        .addColumn(ID)
        .addColumn(NAME)
        .addColumn(VALUE);

    Table table = new Table(view.getComponent());
    assertTrue(table.getHeader().contentEquals(
      "id", "name", "value"));
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23"},
      {"2", "aName", "0.1"},
      {"3", "yetAnotherName", "2.2"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23"},
      {"2", "aName", "0.1"},
      {"3", "yetAnotherName", "2.2"},
    }));

  }

  public void testRefreshRemovesDeletedGlobs() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    repository.startChangeSet();
    repository.delete(key1);

    view.refresh(false);
    assertTrue(table.contentEquals(new String[][]{
      {"name2", "2.2"}
    }));
  }

  public void testRefreshUpdatesColumnNames() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");

    DummyTableColumn column1 = new DummyTableColumn("Col1");
    DummyTableColumn column2 = new DummyTableColumn("Col2");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(column1)
        .addColumn(column2);

    Table table = new Table(view.getComponent());
    assertThat(table.getHeader().contentEquals("Col1", "Col2"));

    column1.name = "Col A";
    column2.name = "Col B";

    view.refresh(false);
    assertThat(table.getHeader().contentEquals("Col A", "Col B"));
  }

  private static class DummyTableColumn implements GlobTableColumn {

    private String name;

    private DummyTableColumn(String name) {
      this.name = name;
    }

    public String getName() {
      return name;
    }

    public TableCellRenderer getRenderer() {
      return new DefaultTableCellRenderer();
    }

    public TableCellEditor getEditor() {
      return null;
    }

    public GlobStringifier getStringifier() {
      return null;
    }

    public Comparator<Glob> getComparator() {
      return null;
    }

    public boolean isEditable(int row, Glob glob) {
      return false;
    }

    public boolean isReSizable() {
      return false;
    }
  }

  public void testHeaderDisplaysIconsAccordingToColumnSorting() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);
    checkSortingIcons(table, SortingIcon.NONE, SortingIcon.NONE);

    table.getHeader().click(0);
    checkSortingIcons(table, SortingIcon.UP, SortingIcon.NONE);

    table.getHeader().click(1);
    checkSortingIcons(table, SortingIcon.NONE, SortingIcon.UP);

    table.getHeader().click(1);
    checkSortingIcons(table, SortingIcon.NONE, SortingIcon.DOWN);

    table.getHeader().click(0);
    checkSortingIcons(table, SortingIcon.UP, SortingIcon.NONE);

    table.getHeader().click(0);
    checkSortingIcons(table, SortingIcon.DOWN, SortingIcon.NONE);

    table.getHeader().click(0);
    checkSortingIcons(table, SortingIcon.NONE, SortingIcon.NONE);
  }

  public void testPopups() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>");
    final GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(NAME)
        .setPopupFactory(new PopupMenuFactory() {
          public JPopupMenu createPopup() {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem("item 1"));
            menu.add(new JMenuItem("item 2"));
            return menu;
          }
        });
    WindowInterceptor
      .init(new Trigger() {
        public void run() throws Exception {
          final JDialog dialog = new JDialog();
          dialog.add(view.getComponent());
          dialog.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent event) {
              dialog.setVisible(false);
            }
          }));
          dialog.setVisible(true);
        }
      })
      .process(new WindowHandler() {
        public Trigger process(org.uispec4j.Window window) throws Exception {
          PopupMenuInterceptor
            .run(window.getTable().triggerRightClick(0, 1))
            .contentEquals("item 1", "item 2")
            .check();
          return window.getButton("Close").triggerClick();
        }
      })
      .run();
  }

  public void testRightClickPreservesSelection() throws Exception {
    repository =
      checker.parse(
        "<dummyObject id='1' name='name1' value='1.1'/>" +
        "<dummyObject id='2' name='name2' value='1.2'/>" +
        "<dummyObject id='3' name='name3' value='1.3'/>"
      );

    final GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(NAME)
        .setPopupFactory(new PopupMenuFactory() {
          public JPopupMenu createPopup() {
            JPopupMenu menu = new JPopupMenu();
            menu.add(new JMenuItem("item 1"));
            menu.add(new JMenuItem("item 2"));
            return menu;
          }
        });

    final Table table = new Table(view.getComponent());

    WindowInterceptor
      .init(new Trigger() {
        public void run() throws Exception {
          final JDialog dialog = new JDialog();
          dialog.add(view.getComponent());
          dialog.add(new JButton(new AbstractAction("Close") {
            public void actionPerformed(ActionEvent event) {
              dialog.setVisible(false);
            }
          }));
          dialog.setVisible(true);
        }
      })
      .process(new WindowHandler() {
        public Trigger process(org.uispec4j.Window window) throws Exception {

          table.selectRows(0, 1);

          PopupMenuInterceptor
            .run(window.getTable().triggerRightClick(1, 1))
            .contentEquals("item 1", "item 2")
            .check();

          assertThat(table.rowsAreSelected(0, 1));

          PopupMenuInterceptor
            .run(window.getTable().triggerRightClick(2, 1))
            .contentEquals("item 1", "item 2")
            .check();

          assertThat(table.rowIsSelected(2));

          return window.getButton("Close").triggerClick();
        }
      })
      .run();
  }

  public void testEditableColumns() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='1.11'/>" +
                    "<dummyObject id='2' name='thisIsAnotherName' value='2.2'/>");

    view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(NAME), directory)
        .addColumn(NAME, new DefaultCellEditor(new JTextField()))
        .addColumn(VALUE);

    Table table = createTable(view);
    assertTrue(table.columnIsEditable(0, true));
    assertTrue(table.columnIsEditable(1, false));
  }

  public void testModifyingValuesWhileSorting() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='thisIsAName' value='0.23' date='2006/08/14'/>" +
                    "<dummyObject id='2' name='aName' value='0.1' date='2006/08/01'/>" +
                    "<dummyObject id='3' name='yetAnotherName' value='2.2' date='2006/08/05'/>");

    GlobTableView view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(ID), directory)
        .addColumn(ID)
        .addColumn(NAME)
        .addColumn(VALUE)
        .addColumn(DATE);

    Table table = new Table(view.getComponent());
    assertTrue(table.getHeader().contentEquals(
      "id", "name", "value", "date"));
    assertTrue(table.contentEquals(new String[][]{
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    table.getHeader().click("name");
    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"1", "thisIsAName", "0.23", "2006/08/14"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
    }));

    repository.update(newKey(DummyObject.TYPE, 1), DummyObject.NAME, "zorro");

    assertTrue(table.contentEquals(new String[][]{
      {"2", "aName", "0.1", "2006/08/01"},
      {"3", "yetAnotherName", "2.2", "2006/08/05"},
      {"1", "zorro", "0.23", "2006/08/14"},
    }));
  }

  public void testUpdatesCanInsertOrRemoveRow() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>" +
                    "<dummyObject id='3' name='name2' value='2.2'/>" +
                    "<dummyObject id='4' name='name2' value='2.2'/>" +
                    "");
    view = GlobTableView.init(DummyObject.TYPE, repository, new GlobFieldComparator(ID), directory)
      .setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name2"))
      .addColumn(DummyObject.ID)
      .addColumn(DummyObject.NAME);
    Table table = new Table(view.getComponent());
    assertTrue(table.contentEquals(new String[][]{
      {"2", "name2"},
      {"3", "name2"},
      {"4", "name2"},
    }));
    repository.startChangeSet();
    repository.update(key1, DummyObject.NAME, "name2");
    repository.update(key2, DummyObject.NAME, "name1");
    repository.update(key3, DummyObject.NAME, "name1");
    repository.completeChangeSet();
    assertTrue(table.contentEquals(new String[][]{
      {"1", "name2"},
      {"4", "name2"},
    }));
  }

  public void testChangeFilterWithSelectedDeletedGlob() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>" +
                    "<dummyObject id='3' name='name2' value='2.2'/>" +
                    "<dummyObject id='4' name='name2' value='2.2'/>" +
                    "");
    view = GlobTableView.init(DummyObject.TYPE, repository, new GlobFieldComparator(ID), directory)
      .setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name2"))
      .addColumn(DummyObject.ID)
      .addColumn(DummyObject.NAME);
    Table table = new Table(view.getComponent());
    selectionService.select(repository.get(key2));
    repository.startChangeSet();
    repository.delete(key2);
    view.setFilter(GlobMatchers.fieldEquals(DummyObject.NAME, "name1"));
    repository.completeChangeSet();
  }

  public void testCopySelectionToClipboard() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>" +
                    "<dummyObject id='3' name='name3' value='3.3'/>" +
                    "<dummyObject id='4' name='name4' value='4.4'/>" +
                    "");
    view = GlobTableView.init(DummyObject.TYPE, repository, new GlobFieldComparator(ID), directory)
      .addColumn(DummyObject.ID)
      .addColumn("Custom", new DummyStringifier("a"))
      .addColumn("Other", new DefaultTableCellRenderer(), new DummyStringifier("b"));
    JTable jTable = view.getComponent();
    Table table = new Table(jTable);

    table.selectRows(1, 3);
    KeyUtils.pressKey(jTable, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.C));
    assertEquals("id\tCustom\tOther\n" +
                 "2\ta2\tb2\n" +
                 "4\ta4\tb4\n",
                 Clipboard.getContentAsText());

    table.selectRows(2);
    Action action = view.getCopyAction("Copy rows");
    assertEquals("Copy rows", action.getValue(Action.NAME));
    action.actionPerformed(null);
    assertEquals("id\tCustom\tOther\n" +
                 "3\ta3\tb3\n",
                 Clipboard.getContentAsText());
  }

  public void testAddKeyBinding() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>" +
                    "");
    final StringBuilder logger = new StringBuilder();
    view = GlobTableView.init(DummyObject.TYPE, repository, new GlobFieldComparator(ID), directory)
      .addColumn(DummyObject.ID)
      .addKeyBinding(GuiUtils.ctrl(KeyEvent.VK_K), "Test", new AbstractAction() {
        public void actionPerformed(ActionEvent e) {
          logger.append("ok");
        }
      });

    JTable jTable = view.getComponent();
    Table table = new Table(jTable);

    KeyUtils.pressKey(jTable, org.uispec4j.Key.plaformSpecificCtrl(org.uispec4j.Key.K));
    assertEquals("ok", logger.toString());
  }

  public void testAddColumn() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1' present='false'/>" +
                    "<dummyObject id='2' name='name2' value='2.2' present='true'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    assertTrue(table.getHeader().contentEquals("name", "value"));

    view.addColumn(DummyObject.PRESENT);

    assertTrue(table.getHeader().contentEquals("name", "value", "present"));

    assertTrue(table.contentEquals(new String[][]{
      {"name1", "1.1", "no"},
      {"name2", "2.2", "yes"},
    }));

  }

  public void testRemoveColumn() throws Exception {
    repository =
      checker.parse("<dummyObject id='1' name='name1' value='1.1'/>" +
                    "<dummyObject id='2' name='name2' value='2.2'/>");
    Table table = createTableWithNameAndValueColumns(repository);

    assertTrue(table.getHeader().contentEquals("name", "value"));

    view.removeColumn(1);

    assertTrue(table.getHeader().contentEquals("name"));

    assertTrue(table.contentEquals(new String[][]{
      {"name1"},
      {"name2"},
    }));

    view.removeColumn(0);

    assertTrue(table.getHeader().contentEquals());
  }

  private void checkColumnIsNotRightAligned(Table table, int column) {
    JLabel label = (JLabel)TableUtils.getRenderedComponentAt(table.getJTable(), 0, column);
    assertFalse(label.getHorizontalAlignment() == JLabel.RIGHT);
  }

  private void checkColumnIsRightAligned(Table table, int column) {
    checkColumnAlignment(table, column, JLabel.RIGHT);
  }

  private void checkColumnAlignment(Table table, int column, int expectedAlignment) {
    JLabel label = (JLabel)TableUtils.getRenderedComponentAt(table.getJTable(), 0, column);
    assertTrue(label.getHorizontalAlignment() == expectedAlignment);
  }

  private void checkSortingIcons(Table table, int expectedColumn0Icon, int expectedColumn1Icon) {
    assertEquals(expectedColumn0Icon, getHeaderIconDirection(table, 0));
    assertEquals(expectedColumn1Icon, getHeaderIconDirection(table, 1));
  }

  private int getHeaderIconDirection(Table table, int columnIndex) {
    TableCellRenderer renderer = table.getJTable().getTableHeader().getDefaultRenderer();
    assertNotNull("No renderer for column " + columnIndex, renderer);
    JLabel label = (JLabel)renderer.getTableCellRendererComponent(table.getJTable(), "", false, false, 0, columnIndex);
    return ((SortingIcon)label.getIcon()).getDirection();
  }

  private Table createTableWithNameAndValueColumns(GlobRepository repository) {
    view =
      GlobTableView.init(TYPE, repository, new GlobFieldComparator(NAME), directory)
        .addColumn(NAME)
        .addColumn(VALUE);
    return createTable(view);
  }

  private Table createTable(GlobTableView view) {
    return new Table(view.getComponent());
  }

  private class DummyStringifier extends AbstractGlobStringifier {
    private boolean reversed;
    private String prefix = "a";

    private DummyStringifier() {
    }

    private DummyStringifier(String prefix) {
      this.prefix = prefix;
    }

    public void setReversed(boolean reversed) {
      this.reversed = reversed;
    }

    public String toString(Glob glob, GlobRepository repository) {
      int id = glob.get(DummyObject.ID);
      return prefix + String.valueOf(reversed ? 4 - id : id);
    }
  }
}
