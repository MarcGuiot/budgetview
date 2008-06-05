package org.crossbowlabs.splits.splitters;

import org.crossbowlabs.splits.SplitProperties;
import org.crossbowlabs.splits.SplitsContext;
import org.crossbowlabs.splits.Splitter;
import org.crossbowlabs.splits.SplitterFactory;
import org.crossbowlabs.splits.exceptions.SplitsException;

import javax.swing.*;
import java.awt.*;

public class DefaultSplitterFactory implements SplitterFactory {

  public Splitter getSplitter(String name,
                              Splitter[] subSplitters,
                              SplitProperties properties,
                              SplitsContext context) {

    if (name.equals("row")) {
      return new Sequence(subSplitters, Sequence.Direction.HORIZONTAL, properties, context);
    }
    if (name.equals("column")) {
      return new Sequence(subSplitters, Sequence.Direction.VERTICAL, properties, context);
    }
    else if (name.equals("verticalSplit")) {
      return new MovableSplit(MovableSplit.Direction.VERTICAL, properties, subSplitters, context);
    }
    else if (name.equals("horizontalSplit")) {
      return new MovableSplit(MovableSplit.Direction.HORIZONTAL, properties, subSplitters, context);
    }
    else if (name.equals("grid")) {
      return new Grid(properties, subSplitters, context);
    }
    else if (name.equals("borderLayout")) {
      return new BorderLayoutComponent(properties, subSplitters, context);
    }
    else if (name.equals("cards")) {
      return new CardLayoutComponent(properties, subSplitters, context);
    }
    else if (name.equals("card")) {
      return new CardSplitter(context, properties, subSplitters);
    }
    else if (name.equals("label")) {
      return new LabelComponent(properties, subSplitters, context);
    }
    else if (name.equals("component")) {
      return NamedComponent.get(properties, subSplitters, context);
    }
    else if (name.equals("filler")) {
      return new Filler(properties, subSplitters, context);
    }
    else if (name.equals("frame")) {
      return new FrameComponent(properties, subSplitters, context);
    }
    else if (name.equals("panel")) {
      return new PanelComponent(properties, subSplitters, context);
    }
    else if (name.equals("styledPanel")) {
      return new StyledPanelComponent(properties, subSplitters, context);
    }
    else if (name.equals("scrollPane")) {
      return new ScrollPaneComponent(properties, subSplitters, context);
    }
    else if (name.equals("table")) {
      return createDefaultComponent(JTable.class, "table", context, properties, subSplitters);
    }
    else if (name.equals("list")) {
      return createDefaultComponent(JList.class, "list", context, properties, subSplitters);
    }
    else if (name.equals("button")) {
      return createDefaultComponent(JButton.class, "button", context, properties, subSplitters);
    }
    else if (name.equals("toggleButton")) {
      return createDefaultComponent(JToggleButton.class, "toggleButton", context, properties, subSplitters);
    }
    else if (name.equals("textField")) {
      return createDefaultComponent(JTextField.class, "textField", context, properties, subSplitters);
    }
    else if (name.equals("textArea")) {
      return createDefaultComponent(JTextArea.class, "textArea", context, properties, subSplitters);
    }
    else if (name.equals("editorPane")) {
      return createDefaultComponent(JEditorPane.class, "editorPane", context, properties, subSplitters);
    }
    else if (name.equals("htmlEditorPane")) {
      return new HtmlEditorPane(context, properties, subSplitters, false);
    }
    else if (name.equals("comboBox")) {
      return createDefaultComponent(JComboBox.class, "comboBox", context, properties, subSplitters);
    }
    else if (name.equals("checkBox")) {
      return createDefaultComponent(JCheckBox.class, "checkBox", context, properties, subSplitters);
    }
    else if (name.equals("tabs")) {
      return new TabGroupSplitter(context, properties, subSplitters);
    }
    else if (name.equals("tab")) {
      return new TabSplitter(context, properties, subSplitters);
    }
    throw new SplitsException("Unknown splitter name: " + name);
  }

  private <T extends Component> DefaultComponent<T> createDefaultComponent(Class<T> componentClass, String name, SplitsContext context, SplitProperties properties, Splitter[] subSplitters) {
    return new DefaultComponent<T>(componentClass, name, context, properties, subSplitters, false);
  }

}
