package org.designup.picsou.gui.components.charts.histo.diff;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;

public class HistoDiffLegendPanel {

  private GlobRepository repository;
  private Directory directory;

  private JPanel panel = new JPanel();
  private JLabel lineLabel = new JLabel();
  private JLabel fillLabel = new JLabel();

  public HistoDiffLegendPanel(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    createPanel();
  }

  public void createPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/components/charts/histoDiffLegendPanel.splits",
                                                      repository, directory);

    builder.add("legendPanel", panel);

    builder.add("lineLabelIcon", new JLabel(new LineIcon()));
    builder.add("lineLabelText", lineLabel);

    builder.add("fillLabelIcon", new JLabel(new FillIcon()));
    builder.add("fillLabelText", fillLabel);

    panel = builder.load();
  }

  public JPanel getPanel() {
    return panel;
  }

  public void show(String lineText, String fillText) {
    panel.setVisible(true);
    lineLabel.setText(lineText);
    fillLabel.setText(fillText);
  }

  public void hide() {
    panel.setVisible(false);
  }

  private class LineIcon implements Icon {
    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g2 = (Graphics2D)graphics;
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(component.getForeground());
      g2.setStroke(new BasicStroke(1.5f));
      g2.drawLine(0, getIconHeight() / 2, getIconWidth(), getIconHeight() / 2);
      g2.fillOval(getIconWidth() / 2 - 3, getIconHeight() / 2 - 3, 6, 6);
    }

    public int getIconWidth() {
      return 15;
    }

    public int getIconHeight() {
      return 15;
    }
  }

  private class FillIcon implements Icon {

    private static final int MARGIN = 2;

    public void paintIcon(Component component, Graphics graphics, int x, int y) {
      Graphics2D g2 = (Graphics2D)graphics;
      g2.setColor(component.getForeground());
      int midX = getIconWidth() / 2;
      int width = getIconWidth() - 1;
      int barWidth = width / 2 - MARGIN;
      int height = getIconHeight() - 1;

      g2.fillRect(0, height - height / 2, barWidth, height / 2);
      g2.fillRect(midX + MARGIN, 0, barWidth, height);
    }

    public int getIconWidth() {
      return 15;
    }

    public int getIconHeight() {
      return 15;
    }
  }
}
