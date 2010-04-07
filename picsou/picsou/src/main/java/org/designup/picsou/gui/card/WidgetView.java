package org.designup.picsou.gui.card;

import org.designup.picsou.gui.View;
import org.designup.picsou.gui.card.widgets.NavigationWidgetPanel;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;
import org.globsframework.gui.GlobsPanelBuilder;

public abstract class WidgetView extends View {
  protected WidgetView(GlobRepository repository, Directory directory) {
    super(repository, directory);
  }

  protected void add(String name, GlobsPanelBuilder builder, NavigationWidget widget) {
    NavigationWidgetPanel panel = new NavigationWidgetPanel(widget, repository, directory);
    builder.add(name, panel.getPanel());
  }
}
