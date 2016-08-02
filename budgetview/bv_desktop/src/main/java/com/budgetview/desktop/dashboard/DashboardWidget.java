package com.budgetview.desktop.dashboard;

import com.budgetview.desktop.model.DashboardStat;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.OnLoadListener;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.utils.TypeChangeSetListener;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public abstract class DashboardWidget {

  protected final GlobRepository repository;
  protected final Directory directory;
  protected JButton widgetButton = new JButton();
  protected JEditorPane legend = GuiUtils.createReadOnlyHtmlComponent();
  private SplitsNode<JButton> widgetNode;
  private TypeChangeSetListener listener;

  public DashboardWidget(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    this.listener = new TypeChangeSetListener(DashboardStat.TYPE) {
      public void update(GlobRepository repository) {
        doUpdate(repository.findOrCreate(DashboardStat.KEY), repository);
      }
    };
    repository.addChangeListener(listener);
  }

  protected abstract void doUpdate(Glob dashboardStat, GlobRepository repository);

  public void register(GlobsPanelBuilder builder, String widgetButtonName, String legendName) {
    widgetNode = builder.add(widgetButtonName, widgetButton);
    builder.add(legendName, legend);
    builder.addOnLoadListener(new OnLoadListener() {
                                public void processLoad() {
                                  listener.update(repository);
                                }
                              });
  }

  protected void setWidgetStyle(String style) {
    widgetNode.applyStyle(style);
  }
}
