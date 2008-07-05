package org.globsframework.gui.splits;

import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.utils.Strings;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class SplitsRepeatTest extends SplitsTestCase {
  public void testRepeat() throws Exception {

    Repeat<String> futur =
      builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                        new RepeatComponentFactory<String>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                            cellBuilder.add("label", new JLabel(object));
                            cellBuilder.add("btn", new JButton(object));
                          }
                        });

    JPanel panel = parse(
      "<repeat ref='myRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    assertEquals("myRepeat", panel.getName());

    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    futur.insert("cc", 1);

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

    futur.remove(0);

    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
  }

  public void testRepeatAcceptsOnlyOneSubComponent() throws Exception {
    checkParsingError("<repeat ref='myRepeat'>" +
                      "  <label ref='label'/>" +
                      "  <button ref='btn'/>" +
                      "</repeat>",
                      "Repeat component 'myRepeat' must have exactly one subcomponent");
  }

  public void testRepeatNotFound() throws Exception {
    checkParsingError("<repeat ref='myRepeat'>" +
                      "  <label ref='label'/>" +
                      "</repeat>", "Repeat 'myRepeat' not declared");
  }

  public void testImbricatedRepeats() throws Exception {
    builder.addRepeat("parentRepeat", Arrays.asList("aa", "bb", "cc"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.addRepeat("childRepeat", new RepeatComponentFactory<String>() {
          public void registerComponents(RepeatCellBuilder cellBuilder, String item) {
            cellBuilder.add("button", new JButton(item));
          }
        }, getItems(object));
      }
    });

    JPanel panel = parse(
      "<repeat ref='parentRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <repeat ref='childRepeat'>" +
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

  public void testDisposeListener() throws Exception {
    final StringBuilder logger = new StringBuilder();
    Repeat<String> repeat = builder.addRepeat("parentRepeat", Arrays.asList("aa", "bb", "cc"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, final String object) {
        cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
          public void dispose() {
            logger.append(object).append('\n');
          }
        });
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.addRepeat("childRepeat", new RepeatComponentFactory<String>() {
          public void registerComponents(RepeatCellBuilder cellBuilder, final String item) {
            cellBuilder.add("button", new JButton(item));
            cellBuilder.addDisposeListener(new RepeatCellBuilder.DisposeListener() {
              public void dispose() {
                logger.append(item).append('\n');
              }
            });
          }
        }, getItems(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='parentRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <repeat ref='childRepeat'>" +
      "      <button ref='button'/>" +
      "    </repeat>" +
      "  </row>" +
      "</repeat>");

    repeat.remove(2);
    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  panel\n" +
               "    button:a1\n" +
               "    button:a2\n" +
               "panel\n" +
               "  label:bb\n" +
               "  panel\n");
    assertEquals("c1\n" +
                 "cc\n", logger.toString());
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
}
