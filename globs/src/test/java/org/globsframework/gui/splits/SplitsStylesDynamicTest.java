package org.globsframework.gui.splits;

import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SplitsStylesDynamicTest extends SplitsTestCase {

  public void testChangeStyle() throws Exception {
    SplitsNode<Component> splitsNode =
      getButton(
        "<styles>" +
        "  <style id='red' selector=' ' background='#FF0000'/>" +
        "  <style id='blue' selector=' ' background='#0000FF'/>" +
        "</styles>" +
        "<button ref='btn'/>");
    splitsNode.applyStyle("red");
    assertEquals(Color.RED, splitsNode.getComponent().getBackground());
    splitsNode.applyStyle("blue");
    assertEquals(Color.BLUE, splitsNode.getComponent().getBackground());
  }

  public void testDoNotUseSelector() throws Exception {
    SplitsNode<Component> splitsNode =
      getButton(
        "<styles>" +
        "  <style id='red' selector='#a' background='#FF0000'/>" +
        "</styles>" +
        "<button ref='btn' background='#000000'/>");
    splitsNode.applyStyle("red");
    assertEquals(Color.RED, splitsNode.getComponent().getBackground());
  }

  public void testOnRepeat() throws Exception {
    final Map<String, SplitsNode<JLabel>> splitsNodesMap = new HashMap<String, SplitsNode<JLabel>>();
    final Map<String, JLabel> labels = new HashMap<String, JLabel>();

    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {

                        public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                          JLabel jLabel = new JLabel(object);
                          SplitsNode<JLabel> labelSplitsNode = cellBuilder.add("label", jLabel);
                          splitsNodesMap.put(object, labelSplitsNode);
                          labels.put(object, jLabel);
                        }
                      });

    parse(
      "<styles>" +
      "  <style id='red' selector=' ' background='#FF0000'/>" +
      "  <style id='blue' selector=' ' background='#0000FF'/>" +
      "</styles>" +
      "<repeat ref='myRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "  </row>" +
      "</repeat>");

    checkColorApply(splitsNodesMap, labels, "aa", "blue", Color.BLUE);
    checkColorApply(splitsNodesMap, labels, "bb", "red", Color.RED);
    checkColorApply(splitsNodesMap, labels, "bb", "blue", Color.BLUE);
  }

  private void checkColorApply(Map<String, SplitsNode<JLabel>> splitHandlerMap, Map<String, JLabel> labels,
                               final String name, final String styleName, final Color color) {
    SplitsNode<JLabel> labelSplitsNode = splitHandlerMap.get(name);
    JLabel label = labels.get(name);
    labelSplitsNode.applyStyle(styleName);
    assertSame(label, labelSplitsNode.getComponent());
    assertEquals(color, label.getBackground());
  }

  private SplitsNode<Component> getButton(String xml) throws Exception {
    builder.add("btn", aButton);
    return parseWithHandler(xml);
  }
}
