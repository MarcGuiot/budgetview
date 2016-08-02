package com.budgetview.desktop.categorization.components;

import com.budgetview.desktop.components.charts.Gauge;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.Updatable;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class CategorizationGaugePanel implements Updatable {
  private CategorizationLevel categorizationLevel;
  private GlobRepository repository;
  private Directory directory;

  private Gauge gauge;
  private JPanel panel;

  public CategorizationGaugePanel(CategorizationLevel categorizationLevel,
                                  GlobRepository repository,
                                  Directory directory) {
    this.categorizationLevel = categorizationLevel;
    this.categorizationLevel.addListener(this);
    this.repository = repository;
    this.directory = directory;
    createPanel();
    update();
  }

  private void createPanel() {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/categorization/categorizationGaugePanel.splits",
                            repository, directory);

    gauge = builder.add("gauge", new Gauge()).getComponent();
    panel = builder.add("panel", new JPanel()).getComponent();
    builder.load();

    gauge.setVisible(false);
  }

  public JPanel getPanel() {
    return panel;
  }

  public void update() {
    double total = categorizationLevel.getTotal();
    double percentage = categorizationLevel.getPercentage();

    if ((total == 0) || (percentage == 0)) {
      gauge.setVisible(false);
    }
    else {
      gauge.setVisible(true);
      gauge.getModel().setValues(percentage, 1.0);
    }
  }

}
