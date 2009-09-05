package org.designup.picsou.gui.components.filtering;

import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.designup.picsou.utils.Lang;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class CustomFilterMessagePanel implements FilterSetListener {
  private FilterSet filterSet;
  private JPanel panel;
  public static final String CUSTOM = "custom";

  public CustomFilterMessagePanel(FilterSet filterSet, GlobRepository repository, Directory directory) {
    this.filterSet = filterSet;
    this.filterSet.addListener(this);
    createPanel(repository, directory);
  }

  private void createPanel(GlobRepository repository, Directory directory) {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/customFilterMessagePanel.splits",
                                                      repository, directory);

    panel = builder.add("panel", new JPanel()).getComponent();
    panel.setVisible(false);

    builder.add("cancel", new AbstractAction(Lang.get("filter.custom")) {
      public void actionPerformed(ActionEvent e) {
        filterSet.remove(CUSTOM);
      }
    });

    builder.load();
  }

  public void filterUpdated(String name, boolean enabled) {
    if (CUSTOM.equals(name)) {
      panel.setVisible(enabled);
    }
  }

  public JPanel getPanel() {
    return panel;
  }
}
