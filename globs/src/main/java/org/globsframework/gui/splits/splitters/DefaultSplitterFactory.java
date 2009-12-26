package org.globsframework.gui.splits.splitters;

import org.globsframework.gui.splits.SplitProperties;
import org.globsframework.gui.splits.Splitter;
import org.globsframework.gui.splits.SplitterFactory;
import org.globsframework.gui.splits.SplitsContext;
import org.globsframework.gui.splits.exceptions.SplitsException;

import javax.swing.*;
import java.awt.*;

public class DefaultSplitterFactory implements SplitterFactory {

  public Splitter getSplitter(String name,
                              Splitter[] subSplitters,
                              SplitProperties properties, SplitsContext context) {

    if (name.equals("row")) {
      return new GridBagSequence(subSplitters, Sequence.Direction.HORIZONTAL, properties);
    }
    if (name.equals("column")) {
      return new GridBagSequence(subSplitters, Sequence.Direction.VERTICAL, properties);
    }
    else if (name.equals("verticalSplit")) {
      return new MovableSplit(MovableSplit.Direction.VERTICAL, properties, subSplitters);
    }
    else if (name.equals("horizontalSplit")) {
      return new MovableSplit(MovableSplit.Direction.HORIZONTAL, properties, subSplitters);
    }
    else if (name.equals("grid")) {
      return new Grid(properties, subSplitters);
    }
    if (name.equals("horizontalBoxes")) {
      return new BoxSequence(subSplitters, Sequence.Direction.HORIZONTAL, properties);
    }
    if (name.equals("verticalBoxes")) {
      return new BoxSequence(subSplitters, Sequence.Direction.VERTICAL, properties);
    }
    else if (name.equals("borderLayout")) {
      return new BorderLayoutComponent(properties, subSplitters);
    }
    else if (name.equals("cards")) {
      return new CardLayoutComponent(properties, subSplitters);
    }
    else if (name.equals("card")) {
      return new CardSplitter(properties, subSplitters);
    }
    else if (name.equals("repeat")) {
      return new RepeatSplitter(properties, subSplitters);
    }
    else if (name.equals("label")) {
      return new LabelComponent(properties, subSplitters);
    }
    else if (name.equals("component")) {
      return new NamedComponent(properties, subSplitters);
    }
    else if (name.equals("filler")) {
      return new Filler(properties, subSplitters);
    }
    else if (name.equals("frame")) {
      return new FrameComponent(properties, subSplitters);
    }
    else if (name.equals("panel")) {
      return new PanelComponent(properties, subSplitters);
    }
    else if (name.equals("scrollPane")) {
      return new ScrollPaneComponent(properties, subSplitters);
    }
    else if (name.equals("table")) {
      return createDefaultComponent(JTable.class, "table", properties, subSplitters);
    }
    else if (name.equals("list")) {
      return createDefaultComponent(JList.class, "list", properties, subSplitters);
    }
    else if (name.equals("button")) {
      return createDefaultComponent(JButton.class, "button", properties, subSplitters);
    }
    else if (name.equals("toggleButton")) {
      return createDefaultComponent(JToggleButton.class, "toggleButton", properties, subSplitters);
    }
    else if (name.equals("radioButton")) {
      return createDefaultComponent(JRadioButton.class, "radioButton", properties, subSplitters);
    }
    else if (name.equals("checkBox")) {
      return createDefaultComponent(JCheckBox.class, "checkBox", properties, subSplitters);
    }
    else if (name.equals("comboBox")) {
      return createDefaultComponent(JComboBox.class, "comboBox", properties, subSplitters);
    }
    else if (name.equals("textField")) {
      return createDefaultComponent(JTextField.class, "textField", properties, subSplitters);
    }
    else if (name.equals("textArea")) {
      return createDefaultComponent(JTextArea.class, "textArea", properties, subSplitters);
    }
    else if (name.equals("editorPane")) {
      return createDefaultComponent(JEditorPane.class, "editorPane", properties, subSplitters);
    }
    else if (name.equals("htmlEditorPane")) {
      return new HtmlEditorPane(properties, subSplitters, false);
    }
    else if (name.equals("progressBar")) {
      return createDefaultComponent(JProgressBar.class, "progressBar", properties, subSplitters);
    }
    else if (name.equals("separator")) {
      return createDefaultComponent(JSeparator.class, "separator", properties, subSplitters);
    }
    else if (name.equals("tabs")) {
      return new TabGroupSplitter(properties, subSplitters);
    }
    else if (name.equals("tab")) {
      return new TabSplitter(properties, subSplitters, context);
    }
    throw new SplitsException("Unknown splitter name: " + name);
  }

  private <T extends Component> DefaultComponent<T> createDefaultComponent(Class<T> componentClass, String name, SplitProperties properties, Splitter[] subSplitters) {
    return new DefaultComponent<T>(componentClass, name, properties, subSplitters, false);
  }

}
