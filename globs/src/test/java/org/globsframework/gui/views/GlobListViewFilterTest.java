package org.globsframework.gui.views;

import org.globsframework.gui.splits.utils.DummyAction;
import org.globsframework.gui.utils.GuiComponentTestCase;
import org.globsframework.metamodel.DummyObject;
import org.globsframework.metamodel.DummyObject2;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.exceptions.InvalidParameter;
import org.uispec4j.Key;
import org.uispec4j.ListBox;
import org.uispec4j.TextBox;

import javax.swing.*;

import static org.globsframework.metamodel.DummyObject.NAME;

public class GlobListViewFilterTest extends GuiComponentTestCase {
  private ListBox list;
  private TextBox filter;
  private GlobListView listView;
  private GlobListViewFilter viewFilter;

  public void testCreationWithEmptyRepository() throws Exception {
    GlobRepository repository = checker.getEmptyRepository();
    init(repository);
    assertThat(list.isEmpty());
    assertEquals("dummyObject", list.getName());
  }

  public void testFiltering() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    init(repository);

    assertThat(list.contentEquals("name1", "name2"));
    assertThat(list.selectionIsEmpty());

    filter.setText("1");
    assertThat(list.contentEquals("name1"));
    assertThat(list.selectionEquals("name1"));
    assertThat(filter.foregroundNear("000000"));

    list.clearSelection();

    filter.setText("NA");
    assertThat(list.contentEquals("name1", "name2"));
    assertThat(list.selectionIsEmpty());
    assertThat(filter.foregroundNear("000000"));

    filter.setText("xx");
    assertThat(list.isEmpty());
    assertThat(filter.foregroundNear("FF0000"));

    filter.clear();
    assertThat(list.contentEquals("name1", "name2"));
    assertThat(list.selectionIsEmpty());
    assertThat(filter.foregroundNear("000000"));

    filter.pressKey(Key.E);
    assertThat(list.contentEquals("name1", "name2"));
    assertThat(filter.foregroundNear("000000"));
    filter.pressKey(Key.d1);
    assertThat(list.contentEquals("name1"));
    assertThat(filter.foregroundNear("000000"));
    assertThat(list.selectionEquals("name1"));
  }

  public void testDefaultFilter() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='12' name='name12'/>" +
                    "<dummyObject id='20' name='name20'/>");
    init(repository);

    assertThat(list.contentEquals("name1", "name12", "name2", "name20"));
    assertThat(list.selectionIsEmpty());

    viewFilter.setDefaultMatcher(GlobMatchers.fieldContainsIgnoreCaseAndAccents(DummyObject.NAME, "2"));

    assertThat(list.contentEquals("name12", "name2", "name20"));
    assertThat(list.selectionIsEmpty());

    filter.setText("1");
    assertThat(list.contentEquals("name12"));
    assertThat(list.selectionEquals("name12"));
    assertThat(filter.foregroundNear("000000"));

    list.clearSelection();

    filter.clear();
    assertThat(list.contentEquals("name12", "name2", "name20"));

    viewFilter.setDefaultMatcher(GlobMatchers.fieldContainsIgnoreCaseAndAccents(DummyObject.NAME, "1"));

    assertThat(list.contentEquals("name1", "name12"));
    assertThat(list.selectionIsEmpty());
  }

  public void testUpDownKeyPressed() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    init(repository);

    assertThat(list.selectionIsEmpty());

    list.pressKey(Key.DOWN);
    assertThat(list.selectionEquals("name1"));

    list.pressKey(Key.DOWN);
    assertThat(list.selectionEquals("name2"));

    list.pressKey(Key.DOWN);
    assertThat(list.selectionEquals("name2"));

    list.pressKey(Key.UP);
    assertThat(list.selectionEquals("name1"));

    list.pressKey(Key.UP);
    assertThat(list.selectionEquals("name1"));
  }

  public void testPageUpAndPageDown() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>");
    init(repository);

    filter.pressKey(Key.PAGE_DOWN);
    assertThat(list.selectionEquals("name3"));

    filter.pressKey(Key.PAGE_UP);
    assertThat(list.selectionEquals("name1"));
  }

  public void testDefaultValue() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>" +
                    "<dummyObject id='0' name='default'/>");
    initWithDefault(0, repository);

    filter.setText("name");
    assertThat(list.contentEquals("default", "name1", "name2", "name3"));
    assertThat(list.selectionIsEmpty());

    filter.setText("XXX");
    assertThat(list.contentEquals("default"));
    assertThat(list.selectionEquals("default"));

    filter.setText("1");
    assertThat(list.contentEquals("default", "name1"));
    assertThat(list.selectionEquals("name1"));
  }

  public void testEnterTriggersDoubleClickAction() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>");
    init(repository);

    DummyAction action = new DummyAction();
    listView.addDoubleClickAction(action);

    ((JTextField)filter.getAwtComponent()).postActionEvent();
    assertTrue(action.wasClicked());
  }

  public void testDefaultValueMustUseSameTypeAsList() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='name1'/>" +
                    "<dummyObject id='2' name='name2'/>" +
                    "<dummyObject id='3' name='name3'/>");
    GlobListView view = createList(repository);
    try {
      GlobListViewFilter.init(view).setDefaultValue(org.globsframework.model.Key.create(DummyObject2.TYPE, 2));
      fail();
    }
    catch (InvalidParameter e) {
      assertEquals("Key must be of type 'dummyObject' instead of 'dummyObject2'", e.getMessage());
    }
  }

  public void testIgnoringAccents() throws Exception {
    GlobRepository repository =
      checker.parse("<dummyObject id='1' name='Crédit'/>" +
                    "<dummyObject id='2' name='Crête'/>" +
                    "<dummyObject id='3' name='Sucre'/>");
    init(repository);

    assertThat(list.contentEquals("Crédit","Crête","Sucre"));
    assertThat(list.selectionIsEmpty());

    filter.setText("cré");
    assertThat(list.contentEquals("Crédit"));

    viewFilter.setIgnoreAccents(true);
    assertThat(list.contentEquals("Crédit", "Crête", "Sucre"));

    viewFilter.setIgnoreAccents(false);
    assertThat(list.contentEquals("Crédit"));
  }

  private void init(GlobRepository repository) {
    listView = createList(repository);
    viewFilter = GlobListViewFilter.init(listView);
    filter = new TextBox(viewFilter.getComponent());
  }

  private void initWithDefault(int id, GlobRepository repository) {
    GlobListView view = createList(repository);

    viewFilter = GlobListViewFilter.init(view)
      .setDefaultValue(org.globsframework.model.Key.create(DummyObject.TYPE, id));
    filter = new TextBox(viewFilter.getComponent());
  }

  private GlobListView createList(GlobRepository repository) {
    GlobListView view = GlobListView.init(DummyObject.TYPE, repository, directory).setRenderer(NAME);
    list = new ListBox(view.getComponent());
    return view;
  }
}
