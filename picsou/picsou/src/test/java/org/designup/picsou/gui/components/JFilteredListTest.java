package org.designup.picsou.gui.components;

import junit.framework.TestCase;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.uispec4j.Key;
import org.uispec4j.ListBox;
import org.uispec4j.TextBox;
import static org.uispec4j.assertion.UISpecAssert.assertThat;

import javax.swing.*;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class JFilteredListTest extends TestCase {

  private ListBox listBox;
  private TextBox textBox;
  private JFilteredList filteredList;

  public void testContents() {
    JFilteredList listJ = new JFilteredList();
    assertTrue(listJ instanceof JComponent);
    assertEquals(2, listJ.getComponentCount());

    Component component = listJ.getComponent(0);
    assertTrue(component instanceof JScrollPane);
    assertTrue(((JScrollPane)component).getViewport().getComponent(0) instanceof JList);

    assertTrue(listJ.getComponent(1) instanceof JTextField);
  }

  public void testContentsWithSingleValue() {
    init(new String[]{"a"});
    assertThat(listBox.contentEquals("a"));
    assertThat(listBox.selectionEquals("a"));
    assertEquals("a", filteredList.getSelectedValue());
  }

  public void testMonoSelectionOnly() throws Exception {
    init(new String[]{"a", "b", "c"});
    listBox.selectIndices(0, 1);
    assertThat(listBox.selectionEquals("b"));
    assertEquals("b", filteredList.getSelectedValue());
  }

  public void testContentsWithThreeValues() throws Exception {
    init(new String[]{"a", "b", "c"});
    assertThat(listBox.contentEquals("a", "b", "c"));
    assertThat(listBox.selectionEquals("a"));
  }

  public void testSelection() throws Exception {
    init(new String[]{"a", "b", "c"});
    listBox.selectIndex(1);
    assertEquals("b", filteredList.getSelectedValue());
  }

  public void testFilter() throws Exception {
    init(new String[]{"a", "b", "c"});
    textBox.setText("b");
    assertThat(listBox.contentEquals("b"));
  }

  public void testFilter2() throws Exception {
    init(new String[]{"xxaaxx", "xxxabx", "xacxx"});
    textBox.pressKey(Key.A);
    assertThat(listBox.contentEquals("xxaaxx", "xxxabx", "xacxx"));
    textBox.pressKey(Key.C);
    assertThat(listBox.contentEquals("xacxx"));
    assertEquals("xacxx", filteredList.getSelectedValue());
  }

  public void testInvalidFilter() throws Exception {
    init(new String[]{"a", "b", "c"});
    textBox.pressKey(Key.D);
    assertThat(listBox.isEmpty());
    assertThat(listBox.selectionIsEmpty());
    assertNull(filteredList.getSelectedValue());
  }

  public void testFilterChange() throws Exception {
    init(new String[]{"xxaaxx", "xxxbbx", "xccxx"});
    textBox.pressKey(Key.C);
    assertEquals(1, filteredList.resultList.getModel().getSize());
    assertEquals("xccxx", filteredList.resultList.getSelectedValue());
  }

  public void testFilteringIsCaseInsensitive() throws Exception {
    init(new String[]{"One", "Two", "Three"});
    textBox.setText("on");
    assertThat(listBox.contentEquals("One"));
  }

  private class DummyListModelListener implements ListDataListener {
    boolean listenerCalled = false;
    ListDataEvent lastEvent;

    public void contentsChanged(ListDataEvent e) {
      listenerCalled = true;
      lastEvent = e;
    }

    public void intervalAdded(ListDataEvent e) {
    }

    public void intervalRemoved(ListDataEvent e) {
    }
  }

  public void testListUpdated() throws Exception {
    init(new String[]{"xxaaxx", "xxxbbx", "xccxx"});
    DummyListModelListener listener = new DummyListModelListener();
    filteredList.resultList.getModel().addListDataListener(listener);
    textBox.setText("c");
    assertTrue(listener.listenerCalled);
    assertEquals(0, listener.lastEvent.getIndex0());
    assertEquals(1, listener.lastEvent.getIndex1());
  }

  public void testFilterColor() throws Exception {
    init(new String[]{"xxaaxx", "xxxbbx", "xccxx"});
    textBox.setText("a");
    assertThat(textBox.foregroundEquals("black"));
    textBox.setText("d");
    assertThat(textBox.foregroundEquals("red"));
    textBox.setText("a");
    assertThat(textBox.foregroundEquals("black"));
  }

  public void testUpDownKeyPressed() throws Exception {
    init(new String[]{"a", "b", "c"});
    listBox.pressKey(Key.DOWN);
    assertEquals("b", filteredList.resultList.getSelectedValue());

    listBox.pressKey(Key.DOWN);
    assertEquals("c", filteredList.resultList.getSelectedValue());
    listBox.pressKey(Key.DOWN);
    assertEquals("c", filteredList.resultList.getSelectedValue());

    listBox.pressKey(Key.UP);
    assertEquals("b", filteredList.resultList.getSelectedValue());
    listBox.pressKey(Key.UP);
    assertEquals("a", filteredList.resultList.getSelectedValue());
    listBox.pressKey(Key.UP);
    assertEquals("a", filteredList.resultList.getSelectedValue());
  }

  public void testUpDownKeyPressedWithEmptyList() throws Exception {
    init(new String[]{});
    textBox.pressKey(Key.DOWN);
    assertEquals(null, filteredList.resultList.getSelectedValue());
    textBox.pressKey(Key.UP);
    assertEquals(null, filteredList.resultList.getSelectedValue());
  }

  public void testPageUpAndPageDown() throws Exception {
    init(new String[]{"a", "b", "c"});
    textBox.pressKey(Key.PAGE_DOWN);
    assertEquals("c", filteredList.resultList.getSelectedValue());
    textBox.pressKey(Key.PAGE_UP);
    assertEquals("a", filteredList.resultList.getSelectedValue());
  }

  public void testUsingTheListWhenAStringifierIsSet() throws Exception {
    init(new Integer[]{3, 13, 18},
         new JFilteredList.Stringifier() {
           public String toString(Object object) {
             Integer i = (Integer)object;
             return "v" + i;
           }
         });
    assertThat(listBox.contentEquals("v3", "v13", "v18"));
    assertEquals(3, filteredList.getSelectedValue());
    listBox.select("v13");
    assertThat(listBox.selectionEquals("v13"));
    assertEquals(13, filteredList.getSelectedValue());
    textBox.setText("1");
    assertThat(listBox.contentEquals("v13", "v18"));
  }

  public void testFilteringWhenAStringifierIsSet() throws Exception {
    init(new Integer[]{3, 13, 18},
         new JFilteredList.Stringifier() {
           public String toString(Object object) {
             Integer i = (Integer)object;
             return "v" + i;
           }
         });
    textBox.setText("v");
    assertThat(listBox.contentEquals("v3", "v13", "v18"));
    textBox.setText("1");
    assertThat(listBox.contentEquals("v13", "v18"));
    textBox.setText("13");
    assertThat(listBox.contentEquals("v13"));
    textBox.setText("");
    assertThat(listBox.contentEquals("v3", "v13", "v18"));
  }

  private void init(Object[] data, JFilteredList.Stringifier stringifier) {
    init(new JFilteredList(data, stringifier));
  }

  private void init(Object[] data) {
    init(new JFilteredList(data));
  }

  private void init(JFilteredList jFilteredList) {
    filteredList = jFilteredList;
    org.uispec4j.Panel panel = new org.uispec4j.Panel(jFilteredList);
    listBox = panel.getListBox();
    textBox = panel.getInputTextBox();
  }

  public static void main(String[] args) throws Exception {
    final JFilteredList filteredList =
      new JFilteredList(new String[]{"aix", "amiens", "angers", "angouleme", "annecy", "auxerre", "avignon",
                                     "bayonne", "besancon", "beziers", "bordeaux", "brest", "caen", "chateauthierry",
                                     "clermont ferrand", "dieppe", "dijon", "grenoble", "le mans", "lille",
                                     "limoges", "lyon", "marseille", "metz", "monaco", "montpellier", "muhouse",
                                     "nancy", "nantes", "nice", "nimes", "orleans", "paris", "pau", "perpignan",
                                     "poitiers", "quimper", "reims", "rennes", "rouen", "saint etienne", "strasbourg",
                                     "toulon", "toulouse", "tours", "troyes", "valence", "vichy", "vienne"},
                        new JFilteredList.Stringifier() {
                          public String toString(Object object) {
                            return "- " + object;
                          }
                        });
    filteredList.setActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        System.out.println("OldFilteredListTest.actionPerformed: " + filteredList.getSelectedValue());
      }
    });
    JPanel panel = new JPanel();
    panel.add(filteredList);
    GuiUtils.show(panel);
  }
}
