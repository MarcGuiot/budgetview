package org.designup.picsou.gui.components;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.DoubleField;
import org.globsframework.model.Glob;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class BalanceGraph extends JPanel implements GlobSelectionListener {
  private Color receivedColorTop = Color.GREEN.darker();
  private Color receivedColorBottom = Color.GREEN.brighter();
  private Color spentColorTop = Color.RED.darker();
  private Color spentColorBottom = Color.RED.brighter();
  private Color borderColor = Color.GRAY;
  private GlobType type;
  private DoubleField receivedField;
  private DoubleField spentField;
  private double receivedPercent = 0.0;
  private double spentPercent = 0.0;

  public BalanceGraph(GlobType type, DoubleField receivedField, DoubleField spentField, Directory directory) {
    this.type = type;
    this.receivedField = receivedField;
    this.spentField = spentField;
    setOpaque(false);
    directory.get(SelectionService.class).addListener(this, type);
  }

  public void selectionUpdated(GlobSelection selection) {
    double received = 0;
    double spent = 0;
    for (Glob glob : selection.getAll(type)) {
      received += Math.abs(glob.get(receivedField));
      spent += Math.abs(glob.get(spentField));
    }

    double max = Math.max(received, spent);
    if (max == 0) {
      receivedPercent = 0;
      spentPercent = 0;
      return;
    }

    receivedPercent = received / max;
    spentPercent = spent / max;

    repaint();
  }

  public void paint(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;

    if ((receivedPercent == 0) && (spentPercent == 0)) {
      return;
    }

    int h = getHeight() - 1;
    int w = getWidth() - 1;

    int middle = w / 2;
    int incomeHeight = (int)(h * receivedPercent);
    int spentHeight = (int)(h * spentPercent);

    g2.setPaint(new GradientPaint(0, 0, receivedColorTop,
                                  0, incomeHeight, receivedColorBottom));
    g2.fillRect(0, h - incomeHeight, middle, incomeHeight);
    g2.setColor(borderColor);
    g2.drawRect(0, h - incomeHeight, middle, incomeHeight);

    g2.setColor(spentColorTop);
    g2.setPaint(new GradientPaint(0, 0, spentColorTop,
                                  0, spentHeight, spentColorBottom));
    g2.fillRect(middle, h - spentHeight, middle, spentHeight);
    g2.setColor(borderColor);
    g2.drawRect(middle, h - spentHeight, middle, spentHeight);
  }

  public void setSpentColorTop(Color spentColorTop) {
    this.spentColorTop = spentColorTop;
  }

  public void setSpentColorBottom(Color spentColorBottom) {
    this.spentColorBottom = spentColorBottom;
  }

  public void setReceivedColorTop(Color receivedColorTop) {
    this.receivedColorTop = receivedColorTop;
  }

  public void setReceivedColorBottom(Color receivedColorBottom) {
    this.receivedColorBottom = receivedColorBottom;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public double getReceivedPercent() {
    return receivedPercent;
  }

  public double getSpentPercent() {
    return spentPercent;
  }
}
