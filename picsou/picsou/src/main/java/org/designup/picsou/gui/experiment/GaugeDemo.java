package org.designup.picsou.gui.experiment;

import com.budgetview.shared.gui.GaugeModel;
import org.designup.picsou.gui.components.charts.Gauge;
import org.globsframework.gui.splits.layout.Anchor;
import org.globsframework.gui.splits.layout.Fill;
import org.globsframework.gui.splits.layout.GridBagBuilder;

import javax.swing.*;
import java.awt.*;

public class GaugeDemo {

  private static GridBagBuilder builder = GridBagBuilder.init();

  private static final boolean[] booleans = {true, false};

  private static final double[][] pairs = {
    {0, 0},
    {0, -20},
    {-12, -20},
    {-20, -20},
    {-25, -20},
    {-50, -20},
    {5, -20},
    {-5, 0},
    {12, 20},
    {20, 20},
    {25, 20},
    {-12, 20},
    {-20, 20},
    {-25, 20},
  };

  private static final double[][] triples = {
    {10, 20, 5}
  };

  public static void main(String[] args) {

    builder.setDefaultInsets(20, 20, 20, 20);

    int row = 0;
    int column = 0;

    addLabel(row, column++, "overrun");
    addLabel(row, column++, "warning");
    addLabel(row, column++, "inverted");
    for (double[] pair : pairs) {
      StringBuilder builder = new StringBuilder();
      builder.append(pair[0]);
      builder.append(" / ");
      builder.append(pair[1]);
      addLabel(row, column++, builder.toString());
    }

    row++;

    for (boolean overrunIsAnError : booleans) {
      for (boolean showWarningForErrors : booleans) {
        for (boolean invertedSignIsAnError : booleans) {

          column = 0;
          addLabel(row, column++, overrunIsAnError);
          addLabel(row, column++, showWarningForErrors);
          addLabel(row, column++, invertedSignIsAnError);

          for (double[] pair : pairs) {
            GaugeModel model = new GaugeModel();
            model.setInvertAll(overrunIsAnError);
            Gauge gauge = new Gauge(model);
            gauge.setLabel("Pair");
            gauge.setForeground(Color.WHITE);
            gauge.setPreferredSize(new Dimension(100, 28));
            gauge.setMaximumSize(new Dimension(100, 28));
            gauge.getModel().setValues(pair[0], pair[1]);
            builder.add(gauge, column++, row, 1, 1, 1, 1, Fill.HORIZONTAL, Anchor.CENTER,
                        new Insets(5, 5, 5, 5));
          }
          for (double[] triple : triples) {
            GaugeModel model = new GaugeModel();
            model.setInvertAll(overrunIsAnError);
            Gauge gauge = new Gauge(model);
            gauge.setLabel("Triple");
            gauge.setForeground(Color.WHITE);
            gauge.setPreferredSize(new Dimension(100, 28));
            gauge.setMaximumSize(new Dimension(100, 28));
            gauge.getModel().setValues(triple[0], triple[1], triple[2], (double)0, "", false);
            builder.add(gauge, column++, row, 1, 1, 1, 1, Fill.HORIZONTAL, Anchor.CENTER,
                        new Insets(5, 5, 5, 5));

          }

          row++;
        }
      }
    }

    JFrame frame = new JFrame();
    frame.setContentPane(new JScrollPane(builder.getPanel()));
    frame.setSize(new Dimension(1200, 500));
    frame.setVisible(true);
  }

  private static void addLabel(int row, int column, boolean value) {
    addLabel(row, column, Boolean.toString(value));
  }

  private static void addLabel(int row, int column, String value) {
    JLabel label = new JLabel();
    label.setText(value);
    builder.add(label, column, row, 1, 1, 1, 1, Fill.NONE, Anchor.WEST);
  }
}
