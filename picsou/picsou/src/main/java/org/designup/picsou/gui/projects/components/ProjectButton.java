package org.designup.picsou.gui.projects.components;

import org.designup.picsou.gui.components.charts.Gauge;
import org.designup.picsou.gui.components.charts.SimpleGaugeView;
import org.designup.picsou.gui.components.images.GlobImageActions;
import org.designup.picsou.gui.description.Formatting;
import org.designup.picsou.gui.model.ProjectStat;
import org.designup.picsou.gui.projects.ProjectView;
import org.designup.picsou.model.Month;
import org.designup.picsou.model.Picture;
import org.designup.picsou.model.Project;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.utils.PopupMenuFactory;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Set;

public class ProjectButton extends JButton implements ChangeSetListener, Disposable {

  private final Key projectKey;
  private final Key projectStatKey;
  private final GlobRepository repository;
  private final Directory directory;

  private boolean active;
  private ImageIcon icon;
  private Gauge gauge;
  private String planned = "";
  private Integer monthId;

  private Color disabledBackgroundColor;
  private Color borderColor;
  private Color disabledBorderColor;
  private Color rolloverBorderColor;

  private DefaultPictureIcon defaultIcon;

  public ProjectButton(final Key projectKey, final PopupMenuFactory factory, final GlobRepository repository, final Directory directory) {
    this.projectKey = projectKey;
    this.directory = directory;
    this.projectStatKey = Key.create(ProjectStat.TYPE, projectKey.get(Project.ID));
    this.repository = repository;
    repository.addChangeListener(this);

    this.gauge = SimpleGaugeView.init(ProjectStat.ACTUAL_AMOUNT, ProjectStat.PLANNED_AMOUNT, repository, directory)
      .setKey(projectStatKey)
      .getComponent();
    this.gauge.setOpaque(false);

    setUI(new ProjectButtonUI());
    setRolloverEnabled(true);
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

    addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        updateComponents();
        GuiUtils.revalidate(ProjectButton.this);
      }
    });

    addActionListener(new AbstractAction() {
      public void actionPerformed(ActionEvent e) {
        Glob project = repository.find(projectKey);
        if (project != null) {
          directory.get(SelectionService.class).select(project);
        }
      }
    });

    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        if (!GuiUtils.isRightClick(e)) {
          return;
        }
        JPopupMenu menu = factory.createPopup();
        if (!menu.isShowing()) {
          menu.show(ProjectButton.this, e.getX(), e.getY());
        }
      }
    });

    setPreferredSize(new Dimension(110,125));
    updateComponents();
  }

  public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    if (changeSet.containsChanges(projectKey) || changeSet.containsChanges(projectStatKey)) {
      updateComponents();
    }
  }

  public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
    if (changedTypes.contains(Project.TYPE) || changedTypes.contains(ProjectStat.TYPE)) {
      updateComponents();
    }
  }

  public void dispose() {
    repository.removeChangeListener(this);
    if (defaultIcon != null) {
      defaultIcon.dispose();
    }
  }

  private void updateComponents() {
    Glob project = repository.find(projectKey);
    active = project != null && project.isTrue(Project.ACTIVE);

    updateIcon();
    updatePlanned();
    updateMonth();
    updateGauge();

    repaint();
  }

  private void updateMonth() {
    Glob stat = repository.find(projectStatKey);
    monthId = stat != null ? stat.get(ProjectStat.FIRST_MONTH) : null;
  }

  private void updateGauge() {
    Dimension preferredSize = getPreferredSize();
    gauge.setSize(preferredSize.width - 30, 20);
  }

  private void updateIcon() {
    Dimension preferredSize = getPreferredSize();
    int iconWidth = preferredSize.width - 1 - 10;
    int iconHeight = (int)(0.75 * (float)iconWidth);
    Glob project = repository.find(projectKey);
    Dimension iconSize = new Dimension(iconWidth, iconHeight);
    icon = Picture.getIcon(project, Project.PICTURE, repository, iconSize);
    if (icon == null) {
      if (defaultIcon == null) {
        defaultIcon = new DefaultPictureIcon(iconSize, directory);
      }
      icon = Picture.toImageIcon(defaultIcon);
    }
    if ((project != null) && !project.isTrue(Project.ACTIVE)) {
      icon = Picture.toGrayscale(icon);
    }
  }

  private void updatePlanned() {
    Glob stat = repository.find(projectStatKey);
    planned = stat == null ? "" : Formatting.toString(stat.get(ProjectStat.PLANNED_AMOUNT));
  }

  public Icon getIcon() {
    return icon;
  }

  public Gauge getGauge() {
    return gauge;
  }

  public String getPlanned() {
    return planned;
  }

  public boolean isActive() {
    return active;
  }

  public String getMonth() {
    return Month.getShortMonthLabel(monthId);
  }

  public Color getBackground() {
    return active ? super.getBackground() : disabledBackgroundColor;
  }

  public Color getBorderColor() {
    if (getModel().isRollover()) {
      return rolloverBorderColor;
    }
    return active ? borderColor : disabledBorderColor;
  }

  public void setDisabledBackground(Color color) {
    this.disabledBackgroundColor = color;
  }

  public void setBorderColor(Color color) {
    this.borderColor = color;
  }

  public void setDisabledBorderColor(Color color) {
    this.disabledBorderColor = color;
  }

  public void setRolloverBorderColor(Color rolloverBorderColor) {
    this.rolloverBorderColor = rolloverBorderColor;
  }

  public void setGaugeBorderColor(Color color) {
    gauge.setBorderColor(color);
  }

  public void setGaugeEmptyColorTop(Color color) {
    gauge.setEmptyColorTop(color);
  }

  public void setGaugeEmptyColorBottom(Color color) {
    gauge.setEmptyColorBottom(color);
  }

  public void setGaugeFilledColorTop(Color color) {
    gauge.setFilledColorTop(color);
  }

  public void setGaugeFilledColorBottom(Color color) {
    gauge.setFilledColorBottom(color);
  }

  public void setGaugeOverrunColorTop(Color color) {
    gauge.setOverrunColorTop(color);
  }

  public void setGaugeOverrunColorBottom(Color color) {
    gauge.setOverrunColorBottom(color);
  }

  public void setGaugeOverrunErrorColorTop(Color color) {
    gauge.setOverrunErrorColorTop(color);
  }

  public void setGaugeOverrunErrorColorBottom(Color color) {
    gauge.setOverrunErrorColorBottom(color);
  }

  public String toString() {
    return "ProjectButton(" + projectKey + " - month:" + monthId + " - planned:" + planned + ")";
  }
}
