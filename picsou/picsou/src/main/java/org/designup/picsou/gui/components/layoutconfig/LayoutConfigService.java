package org.designup.picsou.gui.components.layoutconfig;

import org.designup.picsou.gui.utils.FrameSize;
import org.designup.picsou.model.LayoutConfig;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.utils.directory.Directory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.globsframework.model.FieldValue.value;

public class LayoutConfigService {
  private List<LayoutConfigListener> listeners = new ArrayList<LayoutConfigListener>();
  private GlobRepository repository;
  private Directory directory;

  public LayoutConfigService(GlobRepository repository, Directory directory) {
    this.repository = repository;
    this.directory = directory;
    repository.addChangeListener(new ChangeListener());
  }

  private class ChangeListener implements ChangeSetListener {
    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(LayoutConfig.TYPE)) {
        updateComponents();
      }
    }
  }

  public void show(final JFrame frame) {
    updateComponents();
    frame.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        Dimension newSize = frame.getSize();
        storeFrameSize(newSize, frame, repository);
      }
    });
    GuiUtils.showCentered(frame);
  }

  private void updateComponents() {
    JFrame frame = directory.get(JFrame.class);
    FrameSize frameSize = FrameSize.init(directory.get(JFrame.class));
    Glob layoutConfig = LayoutConfig.find(frameSize.screenSize, frameSize.targetFrameSize, repository, false);
    if (layoutConfig == null) {
      return;
    }
    loadFrameSize(frame, layoutConfig);
    for (LayoutConfigListener listener : listeners) {
      listener.updateComponent(layoutConfig);
    }
  }

  public void updateFields(FieldValues values) {
    FrameSize frameSize = FrameSize.init(directory.get(JFrame.class));
    Glob layoutConfig = LayoutConfig.find(frameSize.screenSize, frameSize.targetFrameSize, repository, true);
    repository.update(layoutConfig.getKey(), values.toArray());
  }

  private void loadFrameSize(JFrame frame, Glob layoutConfig) {
    Integer width = layoutConfig.get(LayoutConfig.FRAME_WIDTH);
    Integer height = layoutConfig.get(LayoutConfig.FRAME_HEIGHT);
    if ((width != null) && (height != null)) {
      GuiUtils.setSizeWithinScreen(frame, width, height);
      GuiUtils.center(frame);
    }
  }

  private void storeFrameSize(Dimension newSize, JFrame frame, GlobRepository repository) {
    FrameSize frameSize = FrameSize.init(frame);
    Glob layoutConfig = LayoutConfig.find(frameSize.screenSize, frameSize.targetFrameSize, repository, true);
    if (layoutConfig == null) {
      return;
    }
    final Key key = layoutConfig.getKey();
    repository.update(key,
                      value(LayoutConfig.FRAME_WIDTH, newSize.width),
                      value(LayoutConfig.FRAME_HEIGHT, newSize.height));
  }

  public void addListener(LayoutConfigListener listener) {
    listeners.add(listener);
  }
}
