package org.globsframework.gui.components;

import org.globsframework.gui.GlobSelection;
import org.globsframework.gui.GlobSelectionListener;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.SplitsNode;
import org.globsframework.gui.splits.color.ColorChangeListener;
import org.globsframework.gui.splits.color.ColorLocator;
import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.gui.splits.utils.Disposable;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.Key;
import org.globsframework.utils.Utils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobSelectablePanel implements GlobSelectionListener, Disposable, ColorChangeListener {
  private SplitsNode<JPanel> node;
  private String selectedStyle;
  private String unselectedStyle;
  private String selectedRolloverStyle;
  private String unselectedRolloverStyle;
  private GlobRepository repository;
  private SelectionService selectionService;
  private ColorService colorService;
  private Key selectionKey;
  private List<Key> keys;
  private GlobType type;
  private boolean selected;
  private boolean rollover;
  private WindowMouseTracker windowTracker;
  private MouseTracker tracker;
  private GlobSelectablePanel.FrameDeactivatedListener frameDeactivatedListener;
  private boolean unselectEnabled = true;
  private boolean multiSelectionEnabled = true;

  public GlobSelectablePanel(SplitsNode<JPanel> panelNode,
                             String selectedStyle, String unselectedStyle,
                             String selectedRolloverStyle, String unselectedRolloverStyle,
                             GlobRepository repository, Directory directory,
                             Key key, Key... otherKeys) {
    this.node = panelNode;
    this.selectedStyle = selectedStyle;
    this.unselectedStyle = unselectedStyle;
    this.selectedRolloverStyle = selectedRolloverStyle;
    this.unselectedRolloverStyle = unselectedRolloverStyle;
    this.repository = repository;
    this.selectionService = directory.get(SelectionService.class);
    this.colorService = directory.get(ColorService.class);
    this.selectionKey = key;
    this.keys = Utils.joinedList(key, otherKeys);
    this.type = key.getGlobType();
    checkKeyTypes(otherKeys);

    selectionService.addListener(this, type);
    colorService.addListener(this);

    Component panel = node.getComponent();
    tracker = new MouseTracker();
    panel.addMouseListener(tracker);
    panel.addMouseMotionListener(tracker);
    frameDeactivatedListener = new FrameDeactivatedListener();
    panel.addPropertyChangeListener("Frame.active", frameDeactivatedListener);
  }

  public void setMultiSelectionEnabled(boolean multiSelectionEnabled) {
    this.multiSelectionEnabled = multiSelectionEnabled;
  }

  public void setUnselectEnabled(boolean unselectEnabled) {
    this.unselectEnabled = unselectEnabled;
  }

  private void checkKeyTypes(Key[] otherKeys) {
    for (Key otherKey : otherKeys) {
      if (!otherKey.getGlobType().equals(type)) {
        throw new InvalidParameter("Keys should be of type '" + type.getName() + "' - unexpected key: " + otherKey);
      }
    }
  }

  public void selectionUpdated(GlobSelection selection) {
    for (Glob glob : selection.getAll(type)) {
      if (keys.contains(glob.getKey())) {
        selected = true;
        update();
        return;
      }
    }
    selected = false;
    update();
  }

  public void update() {
    node.applyStyle(getCurrentStyle());
  }

  private String getCurrentStyle() {
    if (selected) {
      return rollover ? selectedRolloverStyle : selectedStyle;
    }
    else {
      return rollover ? unselectedRolloverStyle : unselectedStyle;
    }
  }

  public void dispose() {
    JPanel panel = node.getComponent();
    if (panel != null) {
      panel.removeMouseListener(tracker);
      panel.removeMouseMotionListener(tracker);
      panel.removePropertyChangeListener(frameDeactivatedListener);
    }
    selectionService.removeListener(this);
    colorService.removeListener(this);
    if (windowTracker != null) {
      windowTracker.dispose();
    }
  }

  public void colorsChanged(ColorLocator colorLocator) {
    node.reapplyStyle();
  }

  private class MouseTracker extends java.awt.event.MouseAdapter implements MouseMotionListener {
    public void mouseClicked(MouseEvent mouseEvent) {
      if (mouseEvent.isConsumed()) {
        return;
      }
      Glob glob = repository.find(selectionKey);
      if (glob != null) {
        GlobList currentSelection = selectionService.getSelection(selectionKey.getGlobType());
        if (currentSelection.contains(glob)) {
          if (unselectEnabled) {
            currentSelection.remove(glob);
            selectionService.select(currentSelection, selectionKey.getGlobType());
          }
        }
        else {
          if (multiSelectionEnabled && mouseEvent.isShiftDown()) {
            addToSelection();
          }
          else {
            selectionService.select(repository.get(selectionKey));
          }
        }
      }
    }

    public void mouseEntered(MouseEvent mouseEvent) {
      if (windowTracker == null) {
        windowTracker = new WindowMouseTracker();
      }

      setRollover();
      if (multiSelectionEnabled && mouseEvent.getButton() == MouseEvent.BUTTON1) {
        addToSelection();
      }
      update();
    }

    private void setRollover() {
      rollover = true;
    }

    public void mouseExited(MouseEvent mouseEvent) {
      JPanel panel = node.getComponent();
      if (panel.isShowing()) {
        Point point = mouseEvent.getPoint();
        Component component = panel.getComponentAt(point);
        if ((component != null) && GuiUtils.isChild(panel, component)) {
          return;
        }
      }
      rollover = false;
      update();
    }

    public void mouseDragged(MouseEvent mouseEvent) {
      if (multiSelectionEnabled)
        addToSelection();
    }

    public void mouseMoved(MouseEvent e) {
    }

    private void addToSelection() {
      GlobList selection = selectionService.getSelection(type);
      Glob glob = repository.get(selectionKey);
      Set<Glob> newSelection = new HashSet<Glob>();
      newSelection.addAll(selection);
      newSelection.add(glob);
      selectionService.select(newSelection, type);
    }
  }

  private class WindowMouseTracker extends MouseAdapter implements MouseMotionListener {

    private Window window;
    private Container parent;

    public WindowMouseTracker() {

      node.getComponent();

      this.window = GuiUtils.getEnclosingWindow(node.getComponent());
      if (window != null) {
        this.window.addMouseMotionListener(this);
      }

      this.parent = node.getComponent().getParent();
      if (parent != null) {
        this.parent.addMouseMotionListener(this);
      }
    }

    public void mouseDragged(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent mouseEvent) {
      if (rollover) {
        JPanel panel = node.getComponent();
        Point point = mouseEvent.getPoint();
        SwingUtilities.convertPointToScreen(point, panel.getParent());
        if (!panel.contains(point)) {
          rollover = false;
          update();
        }
      }
    }

    public void dispose() {
      if (window != null) {
        window.removeMouseMotionListener(this);
      }
      if (parent != null) {
        parent.removeMouseMotionListener(this);
      }
    }
  }

  private class FrameDeactivatedListener implements PropertyChangeListener {
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
      if (rollover && Boolean.FALSE.equals(propertyChangeEvent.getNewValue())) {
        rollover = false;
        update();
      }
    }
  }
}
