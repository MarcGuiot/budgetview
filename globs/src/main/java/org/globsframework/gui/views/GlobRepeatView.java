package org.globsframework.gui.views;

import org.globsframework.gui.ComponentHolder;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GlobRepeatView implements ComponentHolder {
  private JPanel jPanel = new JPanel();
  private GlobViewModel model;
  private GlobRepository repository;
  private Directory directory;
  private Factory factory;
  private List<ComponentHolder> panels = new ArrayList<ComponentHolder>();

  public static GlobRepeatView init(GlobType type, GlobRepository repository, Directory directory, Comparator<Glob> comparator, Factory factory) {
    return new GlobRepeatView(type, repository, directory, comparator, factory);
  }

  public GlobRepeatView(GlobType type, final GlobRepository repository, final Directory directory,
                        Comparator<Glob> comparator, final Factory factory) {
    this.repository = repository;
    this.directory = directory;
    this.factory = factory;
    this.jPanel.setLayout(new BoxLayout(jPanel, BoxLayout.Y_AXIS));
    this.model = new GlobViewModel(type, repository, comparator, new GlobViewModel.Listener() {
      public void globInserted(int index) {
        Glob glob = model.get(index);
        ComponentHolder panel = factory.getComponent(glob, repository, directory);
        panels.add(index, panel);
        jPanel.add(panel.getComponent(), index);
        revalidate();
      }

      public void globUpdated(int index) {
        revalidate();
      }

      public void globRemoved(int index) {
        jPanel.remove(index);
        ComponentHolder panel = panels.remove(index);
        panel.dispose();
        revalidate();
      }

      public void globListPreReset() {
      }

      public void globListReset() {
        initPanel();
        revalidate();
      }
    });
    initPanel();
  }

  private void revalidate() {
    Container parent = jPanel.getParent();
    if (parent != null) {
      parent.validate();
    }
  }

  private void initPanel() {
    jPanel.removeAll();
    jPanel.repaint();
    for (ComponentHolder panel : panels) {
      panel.dispose();
    }
    GlobList globs = model.getAll();
    for (Glob glob : globs) {
      ComponentHolder component = factory.getComponent(glob, repository, directory);
      panels.add(component);
      jPanel.add(component.getComponent());
    }
  }

  public JPanel getComponent() {
    return jPanel;
  }

  public void dispose() {
    model.dispose();
  }

  public interface Factory {
    ComponentHolder getComponent(Glob glob, GlobRepository repository, Directory directory);
  }
}
