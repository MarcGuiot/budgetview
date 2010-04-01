package org.globsframework.gui.splits;

import org.globsframework.gui.splits.layout.*;
import org.globsframework.gui.splits.repeat.Repeat;
import org.globsframework.gui.splits.repeat.RepeatCellBuilder;
import org.globsframework.gui.splits.repeat.RepeatComponentFactory;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.utils.Strings;
import org.uispec4j.finder.ComponentMatchers;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.ArrayList;

public class SplitsRepeatTest extends SplitsTestCase {

  public void testRepeat() throws Exception {
    Repeat<String> repeat =
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
    assertSame(panel, builder.getComponent("myRepeat"));

    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    repeat.insert("cc", 1);
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
    assertTrue(panel.isVisible());

    repeat.remove(0);
    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
    assertTrue(panel.isVisible());

    repeat.remove(0);
    checkPanel(panel,
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
    assertTrue(panel.isVisible());

    repeat.remove(0);
    checkPanel(panel, "");
    assertTrue(panel.isVisible());
  }

  public void testRepeatAutoHidesWhenNothingIsShown() throws Exception {
    Repeat<String> repeat =
      builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                        new RepeatComponentFactory<String>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                            cellBuilder.add("label", new JLabel(object));
                            cellBuilder.add("btn", new JButton(object));
                          }
                        });

    JPanel panel = parse(
      "<repeat ref='myRepeat' autoHideIfEmpty='true'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    assertEquals("myRepeat", panel.getName());
    assertSame(panel, builder.getComponent("myRepeat"));

    assertTrue(panel.isVisible());
    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    repeat.remove(0);
    assertTrue(panel.isVisible());
    checkPanel(panel,
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    repeat.remove(0);
    assertFalse(panel.isVisible());
    checkPanel(panel, "");

    repeat.insert("cc", 0);
    assertTrue(panel.isVisible());
    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n");

    repeat.remove(0);
    assertFalse(panel.isVisible());
    checkPanel(panel, "");
  }

  public void testAutoHideRepeatWithNoInitialItemsIsHidden() throws Exception {
    Repeat<String> repeat =
      builder.addRepeat("myRepeat", new ArrayList<String>(),
                        new RepeatComponentFactory<String>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                            cellBuilder.add("label", new JLabel(object));
                            cellBuilder.add("btn", new JButton(object));
                          }
                        });

    JPanel panel = parse(
      "<repeat ref='myRepeat' autoHideIfEmpty='true'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");
    assertFalse(panel.isVisible());
  }

  public void testRepeatWithDefaultLayoutAcceptsOnlyOneSubComponent() throws Exception {
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
        cellBuilder.addRepeat("childRepeat", getItems(object), new RepeatComponentFactory<String>() {
          public void registerComponents(RepeatCellBuilder cellBuilder, String item) {
            cellBuilder.add("button", new JButton(item));
          }
        });
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
    Repeat<String> parentRepeat = builder.addRepeat("parentRepeat", Arrays.asList("aa", "bb", "cc"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, final String object) {
        cellBuilder.addDisposeListener(new Disposable() {
          public void dispose() {
            logger.append(object).append('\n');
          }
        });
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.addRepeat("childRepeat", getItems(object), new RepeatComponentFactory<String>() {
          public void registerComponents(RepeatCellBuilder cellBuilder, final String item) {
            cellBuilder.add("button", new JButton(item));
            cellBuilder.addDisposeListener(new Disposable() {
              public void dispose() {
                logger.append(item).append('\n');
              }
            });
          }
        });
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

    parentRepeat.remove(2);
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
    logger.setLength(0);

    parentRepeat.set(Arrays.asList("dd"));
    checkPanel(panel,
               "panel\n" +
               "  label:dd\n" +
               "  panel\n");
    assertEquals("a1\n" +
                 "a2\n" +
                 "aa\n" +
                 "bb\n",
                 logger.toString());
  }

  public void testVerticalGridLayout() throws Exception {
    Repeat<String> repeat = builder.addRepeat("repeat", Arrays.asList("aa", "bb"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("button", new JButton(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='repeat' layout='verticalGrid'>" +
      "  <label ref='label' fill='horizontal' anchor='south' marginTop='10' marginBottom='5'/>" +
      "  <button ref='button' marginLeft='5' marginRight='5'/>" +
      "</repeat>");

    checkPanel(panel,
               "label:aa\n" +
               "button:aa\n" +
               "label:bb\n" +
               "button:bb\n");

    checkLabel(panel, 0, "aa", 0, 0);
    checkButton(panel, 1, "aa", 1, 0);
    checkLabel(panel, 2, "bb", 0, 1);
    checkButton(panel, 3, "bb", 1, 1);

    repeat.insert("cc", 1);

    checkPanel(panel,
               "label:aa\n" +
               "button:aa\n" +
               "label:cc\n" +
               "button:cc\n" +
               "label:bb\n" +
               "button:bb\n");
    checkLabel(panel, 0, "aa", 0, 0);
    checkButton(panel, 1, "aa", 1, 0);
    checkLabel(panel, 2, "cc", 0, 1);
    checkButton(panel, 3, "cc", 1, 1);
    checkLabel(panel, 4, "bb", 0, 2);
    checkButton(panel, 5, "bb", 1, 2);

    repeat.remove(0);

    checkPanel(panel,
               "label:cc\n" +
               "button:cc\n" +
               "label:bb\n" +
               "button:bb\n");
    checkLabel(panel, 0, "cc", 0, 0);
    checkButton(panel, 1, "cc", 1, 0);
    checkLabel(panel, 2, "bb", 0, 1);
    checkButton(panel, 3, "bb", 1, 1);
  }

  public void testSwapInRepeat() throws Exception {
    Repeat<String> repeat =
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
    assertSame(panel, builder.getComponent("myRepeat"));

    checkPanel(panel,
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

    repeat.insert("cc", 1);
    repeat.move(0, 1);
    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:aa\n" +
               "  button:aa\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");

  }

  public void testGridSwap() throws Exception {
    Repeat<String> repeat = builder.addRepeat("repeat", Arrays.asList("aa", "bb"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("button", new JButton(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='repeat' layout='horizontalGrid'>" +
      "  <label ref='label' fill='horizontal' anchor='south' marginTop='10' marginBottom='5'/>" +
      "  <button ref='button' marginLeft='5' marginRight='5'/>" +
      "</repeat>");

    checkPanel(panel,
               "label:aa\n" +
               "button:aa\n" +
               "label:bb\n" +
               "button:bb\n");
    repeat.move(0, 1);
    checkPanel(panel,
               "label:bb\n" +
               "button:bb\n" +
               "label:aa\n" +
               "button:aa\n" +
               "");

  }

  public void testHorizontalGridLayout() throws Exception {
    builder.addRepeat("repeat", Arrays.asList("aa", "bb"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("button", new JButton(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='repeat' layout='horizontalGrid'>" +
      "  <label ref='label' fill='horizontal' anchor='south' marginTop='10' marginBottom='5'/>" +
      "  <button ref='button' marginLeft='5' marginRight='5'/>" +
      "</repeat>");

    checkPanel(panel,
               "label:aa\n" +
               "button:aa\n" +
               "label:bb\n" +
               "button:bb\n");

    checkLabel(panel, 0, "aa", 0, 0);
    checkButton(panel, 1, "aa", 0, 1);
    checkLabel(panel, 2, "bb", 1, 0);
    checkButton(panel, 3, "bb", 1, 1);
  }

  public void testForceWrapInHorizontalGridLayouts() throws Exception {
    builder.addRepeat("repeat", Arrays.asList("a", "b", "c", "d"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("button", new JButton(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='repeat' layout='horizontalGrid' gridWrapLimit='3'>" +
      "  <label ref='label' fill='horizontal' anchor='south' marginTop='10' marginBottom='5'/>" +
      "  <button ref='button' marginLeft='5' marginRight='5'/>" +
      "</repeat>");

    checkLabel(panel, 0, "a", 0, 0);
    checkButton(panel, 1, "a", 0, 1);
    checkLabel(panel, 2, "b", 1, 0);
    checkButton(panel, 3, "b", 1, 1);
    checkLabel(panel, 4, "c", 2, 0);
    checkButton(panel, 5, "c", 2, 1);
    checkLabel(panel, 6, "d", 0, 2);
    checkButton(panel, 7, "d", 0, 3);
  }

  public void testForceWrapInVerticalGridLayouts() throws Exception {
    builder.addRepeat("repeat", Arrays.asList("a", "b", "c", "d"), new RepeatComponentFactory<String>() {
      public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
        cellBuilder.add("label", new JLabel(object));
        cellBuilder.add("button", new JButton(object));
      }
    });
    JPanel panel = parse(
      "<repeat ref='repeat' layout='verticalGrid' gridWrapLimit='3'>" +
      "  <label ref='label' fill='horizontal' anchor='south' marginTop='10' marginBottom='5'/>" +
      "  <button ref='button' marginLeft='5' marginRight='5'/>" +
      "</repeat>");

    checkLabel(panel, 0, "a", 0, 0);
    checkButton(panel, 1, "a", 1, 0);
    checkLabel(panel, 2, "b", 0, 1);
    checkButton(panel, 3, "b", 1, 1);
    checkLabel(panel, 4, "c", 0, 2);
    checkButton(panel, 5, "c", 1, 2);
    checkLabel(panel, 6, "d", 2, 0);
    checkButton(panel, 7, "d", 3, 0);
  }

  public void testRowLayout() throws Exception {
    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                          cellBuilder.add("label", new JLabel(object));
                        }
                      });

    JPanel panel = parse(
      "<repeat ref='myRepeat' layout='row'>" +
      "  <label ref='label'/>" +
      "</repeat>");

    assertTrue(panel.getLayout() instanceof BoxLayout);
  }

  public void testWrappedRowLayout() throws Exception {
    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                          cellBuilder.add("label", new JLabel(object));
                        }
                      });

    JPanel panel = parse(
      "<repeat ref='myRepeat' layout='wrappedRow'>" +
      "  <label ref='label'/>" +
      "</repeat>");

    FlowLayout layout = (FlowLayout)panel.getLayout();
    assertEquals(FlowLayout.LEFT, layout.getAlignment());
  }

  public void testWrappedColumnLayout() throws Exception {
    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                          cellBuilder.add("label", new JLabel(object));
                          cellBuilder.add("btn", new JButton(object));
                        }
                      });

    JPanel panel = parse(
      "<repeat ref='myRepeat' layout='wrappedColumn'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    assertEquals(WrappedColumnLayout.class, panel.getLayout().getClass());
  }

  public void testLabelForInRepeat() throws Exception {
    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String text) {
                          cellBuilder.add("label", new JLabel(text));
                          cellBuilder.add("btn", new JButton(text + "Button"));
                        }
                      });

    JPanel jPanel = parse(
      "<repeat ref='myRepeat'>" +
      "  <row>" +
      "    <label ref='label' labelFor='btn'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    org.uispec4j.Panel panel = new org.uispec4j.Panel(jPanel);
    assertThat(panel.getButton(ComponentMatchers.componentLabelFor("aa")).textEquals("aaButton"));
    assertThat(panel.getButton(ComponentMatchers.componentLabelFor("bb")).textEquals("bbButton"));
  }

  public void testAutoHideInRepeat() throws Exception {
    builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                      new RepeatComponentFactory<String>() {
                        public void registerComponents(RepeatCellBuilder cellBuilder, String text) {
                          cellBuilder.add("label", new JLabel(text));
                          cellBuilder.add("btn", new JButton(text + "Button"));
                        }
                      });

    JPanel jPanel = parse(
      "<repeat ref='myRepeat'>" +
      "  <row>" +
      "    <label ref='label' autoHideSource='btn'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    org.uispec4j.Panel panel = new org.uispec4j.Panel(jPanel);
    panel.getButton("aaButton").getAwtComponent().setVisible(false);
    assertFalse(panel.getTextBox("aa").isVisible());

    assertThat(panel.getButton("bbButton").isVisible());
    assertThat(panel.getTextBox("bb").isVisible());
  }

  public void testUpdateNotCreatedPanelRepeat() throws Exception {
    Repeat<String> repeat =
      builder.addRepeat("myRepeat", Arrays.asList("aa"),
                        new RepeatComponentFactory<String>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                            cellBuilder.add("label", new JLabel(object));
                            cellBuilder.add("btn", new JButton(object));
                          }
                        });

    repeat.insert("bb", 1);
    repeat.insert("cc", 2);
    repeat.remove(0);
    repeat.move(1, 0);

    JPanel panel = parse(
      "<repeat ref='myRepeat'>" +
      "  <row>" +
      "    <label ref='label'/>" +
      "    <button ref='btn'/>" +
      "  </row>" +
      "</repeat>");

    checkPanel(panel,
               "panel\n" +
               "  label:cc\n" +
               "  button:cc\n" +
               "panel\n" +
               "  label:bb\n" +
               "  button:bb\n");
  }

  public void testCardLayoutInRepeat() throws Exception {

    final java.util.List<CardHandler> handlers = new ArrayList<CardHandler>();

    Repeat<String> repeat =
      builder.addRepeat("myRepeat", Arrays.asList("aa", "bb"),
                        new RepeatComponentFactory<String>() {
                          public void registerComponents(RepeatCellBuilder cellBuilder, String object) {
                            JPanel panel = new JPanel();
                            panel.setName(object + "Panel");
                            cellBuilder.add("panel", panel);

                            cellBuilder.add("label", new JLabel(object));

                            cellBuilder.add("button", new JButton(object));
                            cellBuilder.add("radio", new JRadioButton(object));

                            handlers.add(cellBuilder.addCardHandler("cards"));
                          }
                        });

    org.uispec4j.Panel panel = new org.uispec4j.Panel((JPanel)parse(
      "<repeat ref='myRepeat'>" +
      "  <row ref='panel'>" +
      "    <label ref='label'/>" +
      "    <cards ref='cards'>" +
      "      <card name='aa'>" +
      "        <button ref='button'/>" +
      "      </card>" +
      "      <card name='bb'>" +
      "        <radioButton ref='radio'/>" +
      "      </card>" +
      "    </cards>" +
      "  </row>" +
      "</repeat>"));

    handlers.get(0).show("aa");
    handlers.get(1).show("bb");

    org.uispec4j.Panel aaPanel = panel.getPanel("aaPanel");
    assertThat(aaPanel.getButton("aa").isVisible());
    assertFalse(aaPanel.containsSwingComponent(JRadioButton.class));

    org.uispec4j.Panel bbPanel = panel.getPanel("bbPanel");
    assertThat(bbPanel.getRadioButton("bb").isVisible());
    assertFalse(bbPanel.containsSwingComponent(JButton.class));
  }

  private void checkButton(JPanel panel, int componentIndex, String label, int x, int y) {
    JButton button = (JButton)panel.getComponent(componentIndex);
    assertEquals(label, button.getText());
    checkGridPos(panel, button,
                 x, y, 1, 1,
                 SwingStretches.NORMAL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.HORIZONTAL, Anchor.CENTER,
                 new Insets(0, 5, 0, 5));
  }

  private void checkLabel(JPanel panel, int componentIndex, String label, int x, int y) {
    JLabel jLabel = (JLabel)panel.getComponent(componentIndex);
    assertEquals(label, jLabel.getText());
    checkGridPos(panel, jLabel,
                 x, y, 1, 1,
                 SwingStretches.NULL_WEIGHT, SwingStretches.NULL_WEIGHT,
                 Fill.HORIZONTAL, Anchor.SOUTH,
                 new Insets(10, 0, 5, 0));
  }

  public static void checkPanel(JPanel panel, String expected) {
    StringBuilder builder = new StringBuilder();
    dump(panel, builder, 0);
    assertEquals(expected, builder.toString());
  }

  private static void dump(Container panel, StringBuilder builder, int level) {
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
        throw new RuntimeException("Unexpected component type: " + component.toString());
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
