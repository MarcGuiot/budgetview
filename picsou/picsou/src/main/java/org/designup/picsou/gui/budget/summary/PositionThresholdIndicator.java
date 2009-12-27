package org.designup.picsou.gui.budget.summary;

import org.designup.picsou.gui.model.BudgetStat;
import org.designup.picsou.gui.utils.AmountColors;
import org.designup.picsou.model.AccountPositionThreshold;
import org.designup.picsou.model.util.Amounts;
import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.ChangeSet;
import org.globsframework.model.ChangeSetListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.Set;

public class PositionThresholdIndicator extends JPanel implements ColorChangeListener, GlobSelectionListener {

  private final String topColorKey;
  private final String bottomColorKey;
  private String borderColorKey;
  private Color topColor;
  private Color bottomColor;
  private Color borderColor;
  private AmountColors colors;

  private Double threshold;
  private Double value;
  private double diff = 0;

  private GlobRepository repository;

  private static int INDICATOR_WIDTH = 20;
  private static int INDICATOR_HEIGHT = 10;

  public PositionThresholdIndicator(GlobRepository repository, Directory directory,
                                    String topColorKey,
                                    String bottomColorKey,
                                    String borderColorKey) {
    this.repository = repository;
    this.topColorKey = topColorKey;
    this.bottomColorKey = bottomColorKey;
    this.borderColorKey = borderColorKey;
    this.colors = new AmountColors(directory);

    setOpaque(true);
    directory.get(ColorService.class).addListener(this);
    this.setMinimumSize(new Dimension(50, 50));
    this.setPreferredSize(new Dimension(50, 50));

    updateThreshold();
    repository.addChangeListener(new ChangeSetListener() {
      public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
        updateThreshold();
      }

      public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
        updateThreshold();
      }
    });
    directory.get(SelectionService.class).addListener(this, BudgetStat.TYPE);
  }

  public void colorsChanged(ColorLocator colorLocator) {
    this.topColor = colorLocator.get(topColorKey);
    this.bottomColor = colorLocator.get(bottomColorKey);
    this.borderColor = colorLocator.get(borderColorKey);
  }

  private void updateThreshold() {
    this.threshold = AccountPositionThreshold.getValue(repository);
    updateDiff();
  }

  public void setValue(Double value) {
    this.value = value;
    updateDiff();
  }

  public double getDiff() {
    return diff;
  }

  private void updateDiff() {
    this.diff = Amounts.diff(value, threshold);
    repaint();
  }

  public void selectionUpdated(GlobSelection selection) {
    Glob lastStat = selection.getAll(BudgetStat.TYPE).sort(BudgetStat.MONTH).getLast();
    setValue(lastStat.get(BudgetStat.END_OF_MONTH_ACCOUNT_POSITION));
  }

  protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D)g;
    int width = getWidth() - 1;
    int height = getHeight() - 1;

    g2.setPaint(new GradientPaint(0, 0, topColor, 0, height, bottomColor));
    g2.fillRect(0, 0, width, height);

    g2.setPaint(colors.getIndicatorColor(diff));
    g2.fillRect(width / 2 - INDICATOR_WIDTH / 2,
                height / 2 - INDICATOR_HEIGHT / 2,
                INDICATOR_WIDTH, INDICATOR_HEIGHT);

    g2.setPaint(borderColor);
    g2.drawRect(0, 0, width, height);

  }
}
