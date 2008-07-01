package org.globsframework.gui.splits;

import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatFactory;
import org.globsframework.gui.splits.repeat.RepeatHandler;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;
import java.awt.*;

public class SplitsRepeatTest extends SplitsTestCase {
  public void testRepeat() throws Exception {

    RepeatHandler<String> handler = builder.addRepeat("myRepeat", new RepeatFactory<String>() {
      public void register(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("btn", new JButton(object));
      }
    }, Arrays.asList("aa", "bb"));

    JPanel panel = parse(
      "<repeat name='myRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
    
    handler.insert("cc", 1);

    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    handler.remove(0);

    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
  }

  public void testRepeatAcceptsOnlyOneSubComponent() throws Exception {
    try {
      parse(
        "<repeat name='myRepeat'>" +
        "  <label ref='label'/>" +
        "  <button ref='btn'/>" +
        "</repeat>");
    }
    catch (Exception e) {
      assertTrue(e.getMessage().contains("Repeat component 'myRepeat' must have exactly one subcomponent"));
    }
  }

  public void testRepeatNotFound() throws Exception {
    try {
      parse(
        "<repeat name='myRepeat'>" +
        "  <label ref='label'/>" +
        "</repeat>");
    }
    catch (Exception e) {
      assertEquals("Repeat 'myRepeat' not declared", e.getMessage());
    }
  }

  public void testImbricatedRepeats() throws Exception {
    builder.addRepeat("parentRepeat", new RepeatFactory<String>() {
      public void register(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.addRepeat("childRepeat", new RepeatFactory<String>() {
          public void register(RepeatCellBuilder cellBuilder, String item) {
            cellBuilder.add("button", new JButton(item));
          }
        }, getItems(object));
      }
    }, Arrays.asList("aa", "bb", "cc"));

    JPanel panel = parse(
      "<repeat name='parentRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <repeat name='childRepeat'>" +
      "      <button ref='button'/>" +
      "    </repeat>" +
      "  </row>" +
      "</repeat>");

    checkPanel(panel,
               "panel\n" +
                 "  label:aa\n" +
                 "  panel\n" +
                 "    button:a1\n" +
                 "    button:a2\n" +
                 "panel\n" +
                 "  label:bb\n" +
                 "  panel\n" +
                 "panel\n" +
                 "  label:cc\n" +
                 "  panel\n" +
                 "    button:c1\n");
  }

  private void checkPanel(JPanel panel, String expected) {
    StringBuilder builder = new StringBuilder();
    dump(panel, builder, 0);
    assertEquals(expected,
                 builder.toString());
  }

  private void dump(Container panel, StringBuilder builder, int level) {
    for (Component component : panel.getComponents()) {
      builder.append(Strings.repeat(" ", level * 2));
      if (component instanceof JLabel) {
        builder.append("label:").append(((JLabel)component).getText());
        builder.append('\n');
      }
      else if (component instanceof JButton) {
        builder.append("button:").append(((JButton)component).getText());
        builder.append('\n');
      }
      else if (component instanceof JPanel) {
        builder.append("panel");
        builder.append('\n');
        dump((JPanel)component, builder, level + 1);
      }
      else {
        throw new RuntimeException(component.toString());
      }
    }
  }

  private static java.util.List<String> getItems(String item) {
    if ("aa".equals(item)) {
      return Arrays.asList("a1", "a2");
    }
    if ("cc".equals(item)) {
      return Arrays.asList("c1");
    }
    return Collections.emptyList();
  }

  private void checkSubpanel(JPanel panel, int row, String text) {
    JPanel subPanel = (JPanel)panel.getComponent(row);
    JLabel label = (JLabel)subPanel.getComponent(0);
    assertEquals("label-" + text, label.getText());
    JButton button = (JButton)subPanel.getComponent(1);
    assertEquals("button-" + text, button.getText());
  }
}
