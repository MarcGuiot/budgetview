package org.designup.picsou.gui.components.filtering.components;

import org.designup.picsou.gui.components.filtering.FilterListener;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class FilterClearingPanel implements FilterListener {
  private FilterManager filterManager;
  private JPanel panel;

  public FilterClearingPanel(FilterManager filterManager, GlobRepository repository, Directory directory) {
    this.filterManager = filterManager;
    this.filterManager.addListener(this);
    createPanel(repository, directory);
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/utils/filterClearingPanel.splits",
                            repository, directory);

    panel = builder.add("panel", new JPanel()).getComponent();
    panel.setVisible(false);

    builder.add("cancel", new AbstractAction(Lang.get("filter.custom")) {
      public void actionPerformed(ActionEvent e) {
        filterManager.clear();
      }
    });

    builder.load();
  }

  public void filterUpdated(List<String> changedFilters) {
    panel.setVisible(filterManager.hasClearableFilters());
  }

  public JPanel getPanel() {
    return panel;
  }
}
