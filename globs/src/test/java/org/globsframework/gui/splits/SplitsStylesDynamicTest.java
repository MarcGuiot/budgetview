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
    SplitHandler<Component> splitHandler =
      getButton(
        "<styles>" +
        "  <style id='red' selector=' ' background='#FF0000'/>" +
        "  <style id='blue' selector=' ' background='#0000FF'/>" +
        "</styles>" +
        "<button ref='btn'/>");
    splitHandler.applyStyle("red");
    assertEquals(Color.RED, splitHandler.getComponent().getBackground());
    splitHandler.applyStyle("blue");
    assertEquals(Color.BLUE, splitHandler.getComponent().getBackground());
  }

  public void testDoNotUseSelector() throws Exception {
    SplitHandler<Component> splitHandler =
      getButton(
        "<styles>" +
        "  <style id='red' selector='#a' background='#FF0000'/>" +
        "</styles>" +
        "<button ref='btn' background='#000000'/>");
    splitHandler.applyStyle("red");
    assertEquals(Color.RED, splitHandler.getComponent().getBackground());
  }

  public void testOnRepeat() throws Exception {
    final Map<String, SplitHandler<JLabel>> splitHandlerMap = new HashMap<String, SplitHandler<JLabel>>();
    final Map<String, JLabel> labels = new HashMap<String, JLabel>();

    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {

                        public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                          JLabel jLabel = new JLabel(object);
                          SplitHandler<JLabel> labelSplitHandler = cellBuilder.add("label", jLabel);
                          splitHandlerMap.put(object, labelSplitHandler);
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

    checkColorApply(splitHandlerMap, labels, "aa", "blue", Color.BLUE);
    checkColorApply(splitHandlerMap, labels, "bb", "red", Color.RED);
    checkColorApply(splitHandlerMap, labels, "bb", "blue", Color.BLUE);
  }

  private void checkColorApply(Map<String, SplitHandler<JLabel>> splitHandlerMap, Map<String, JLabel> labels,
                               final String name, final String styleName, final Color color) {
    SplitHandler<JLabel> labelSplitHandler = splitHandlerMap.get(name);
    JLabel label = labels.get(name);
    labelSplitHandler.applyStyle(styleName);
    assertSame(label, labelSplitHandler.getComponent());
    assertEquals(color, label.getBackground());
  }

  private SplitHandler<Component> getButton(String xml) throws Exception {
    builder.add("btn", aButton);
    return parseWithHanler(xml);
  }
}
