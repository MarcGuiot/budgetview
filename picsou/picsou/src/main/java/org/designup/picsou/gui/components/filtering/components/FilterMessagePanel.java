package org.designup.picsou.gui.components.filtering.components;

import org.designup.picsou.gui.components.filtering.FilterListener;
import org.designup.picsou.gui.components.filtering.FilterManager;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Collection;

public class FilterMessagePanel implements FilterListener {
  private FilterManager filterManager;
  private JPanel panel;
  private JLabel filterLabel = new JLabel();

  public FilterMessagePanel(FilterManager filterManager, GlobRepository repository, Directory directory) {
    this.filterManager = filterManager;
    this.filterManager.addListener(this);
    createPanel(repository, directory);
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder =
      new GlobsPanelBuilder(getClass(), "/layout/utils/filterMessagePanel.splits",
                            repository, directory);

    builder.add("filterLabel", filterLabel);

    panel = builder.add("panel", new JPanel()).getComponent();
    panel.setVisible(false);

    builder.add("cancel", new AbstractAction(Lang.get("filter.custom")) {
      public void actionPerformed(ActionEvent e) {
        filterManager.removeAll();
      }
    });

    builder.load();
  }

  public void filterUpdated(Collection<String> changedFilters) {
    filterLabel.setText(filterManager.getLabel());
    panel.setVisible(filterManager.hasClearableFilters());
  }

  public JPanel getPanel() {
    return panel;
  }
}
