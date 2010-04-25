package org.designup.picsou.gui.card.widgets;

import org.designup.picsou.gui.utils.Gui;
import org.designup.picsou.gui.card.NavigationWidget;
import org.designup.picsou.gui.card.utils.NavigationIcons;
import org.globsframework.gui.GlobsPanelBuilder;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;

public class NavigationWidgetPanel {

  private NavigationWidget widget;
  private SplitsNode<JPanel> panelNode;
  private JPanel contentPanel = new JPanel();
  private GlobRepository repository;
  private Directory directory;

  public NavigationWidgetPanel(NavigationWidget widget,
                               GlobRepository repository,
                               Directory directory) {
    this.widget = widget;
    this.repository = repository;
    this.directory = directory;
  }

  public JPanel getPanel() {
    GlobsPanelBuilder builder = new GlobsPanelBuilder(getClass(), "/layout/home/navigationWidgetPanel.splits",
                                                      repository, directory);

    Action action = widget.getAction();
    panelNode = builder.add("panel", new JPanel());
    builder.add("icon", createIconButton(action));

    SplitsNode<JButton> actionButtonNode = builder.add("widgetAction", new JButton(action));
    builder.add("contentPanel", contentPanel);
    builder.add("content", widget.getComponent());

    JPanel panel = builder.load();

    actionButtonNode.applyStyle(widget.isNavigation() ? "navigation" : "action");

    setHighlighted(widget.isHighlighted());
    widget.addListener(new NavigationWidgetListener() {
      public void highlightingChanged(boolean highlighted) {
        setHighlighted(highlighted);
      }
    });

    return panel;
  }

  private void setHighlighted(boolean highlighted) {
    panelNode.applyStyle(highlighted ? "highlightedWidget" : "normalWidget");
    GuiUtils.revalidate(panelNode.getComponent());
  }

  private JButton createIconButton(Action action) {
    JButton iconButton = new JButton(action);
    Gui.configureIconButton(iconButton, widget.getName(), NavigationIcons.DIMENSION);
    iconButton.setIcon(widget.getIcon());
    iconButton.setRolloverIcon(widget.getRolloverIcon());
    return iconButton;
  }
}
