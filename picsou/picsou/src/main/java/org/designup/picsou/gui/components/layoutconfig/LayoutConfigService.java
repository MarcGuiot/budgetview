package org.designup.picsou.gui.components.layoutconfig;

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
  private Directory directory;

  public LayoutConfigService(GlobRepository repository, Directory directory) {
    this.directory = directory;
    Listener listener = new Listener();
    repository.addChangeListener(listener);
  }

  private class Listener implements ChangeSetListener {

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
    }

    public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
      if (changedTypes.contains(LayoutConfig.TYPE)) {
        loadFrameSize(directory.get(JFrame.class), repository);
      }
    }

  }

  public void show(final JFrame frame, final GlobRepository repository) {
    loadFrameSize(frame, repository);
    frame.addComponentListener(new ComponentAdapter() {
      public void componentResized(ComponentEvent e) {
        Dimension newSize = frame.getSize();
        storeFrameSize(newSize, frame, repository);
      }
    });
    GuiUtils.showCentered(frame);
  }

  private void loadFrameSize(JFrame frame, GlobRepository repository) {
    Glob layoutConfig = LayoutConfig.find(GuiUtils.getMaxSize(frame), repository);
    if (layoutConfig == null) {
      return;
    }
    Integer width = layoutConfig.get(LayoutConfig.FRAME_WIDTH);
    Integer height = layoutConfig.get(LayoutConfig.FRAME_HEIGHT);
    if ((width != null) && (height != null)) {
      GuiUtils.setSizeWithinScreen(frame, width, height);
      GuiUtils.center(frame);
    }
  }

  private void storeFrameSize(Dimension newSize, JFrame frame, GlobRepository repository) {
    Glob layoutConfig = LayoutConfig.find(GuiUtils.getMaxSize(frame), repository);
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
