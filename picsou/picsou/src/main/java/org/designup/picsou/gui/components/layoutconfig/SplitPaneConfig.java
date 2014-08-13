package org.designup.picsou.gui.components.layoutconfig;

import com.jidesoft.swing.JideSplitPane;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class SplitPaneConfig implements LayoutConfigListener {

  private final Directory directory;
  private JideSplitPane splitPane = new JideSplitPane();
  private DoubleField[] fields;
  private boolean initCompleted;

  public static JideSplitPane create(Directory directory, DoubleField firstField, DoubleField... otherFields) {
    DoubleField[] fields = new DoubleField[otherFields.length + 1];
    fields[0] = firstField;
    System.arraycopy(otherFields, 0, fields, 1, otherFields.length);
    SplitPaneConfig splitPaneConfig = new SplitPaneConfig(directory, fields);
    return splitPaneConfig.splitPane;
  }

  private SplitPaneConfig(Directory directory, DoubleField[] fields) {
    this.directory = directory;
    this.fields = fields;
    directory.get(LayoutConfigService.class).addListener(this);
    splitPane.setProportionalLayout(true);
    splitPane.addPropertyChangeListener("proportions", new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent evt) {
        if (initCompleted) {
          storeValues();
        }
        else {
          initComponent();
          initCompleted = true;
        }
      }
    });
  }

  private void initComponent() {
    double[] proportions = new double[fields.length];
    for (int i = 0; i < fields.length; i++) {
      proportions[i] = (Double)fields[i].getDefaultValue();
    }
    splitPane.setProportions(proportions);
  }

  public void updateComponent(Glob layoutConfig) {
    double[] proportions = new double[fields.length];
    double total = 0;
    for (int i = 0; i < fields.length; i++) {
      proportions[i] = layoutConfig.get(fields[i]);
      total += proportions[i];
    }
    if (total > 1.0) {
      for (int i = 0; i < fields.length; i++) {
        proportions[i] = proportions[i] / total;
      }
    }
    splitPane.setProportions(proportions);
    initCompleted = true;
  }

  private void storeValues() {
    FieldValuesBuilder values = FieldValuesBuilder.init();
    double[] proportions = splitPane.getProportions();
    for (int i = 0; i < Math.min(fields.length, proportions.length); i++) {
      values.set(fields[i], proportions[i]);
    }
    directory.get(LayoutConfigService.class).updateFields(values.get());
  }
}
