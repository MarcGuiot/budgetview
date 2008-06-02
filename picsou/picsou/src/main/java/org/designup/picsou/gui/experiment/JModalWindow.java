package org.designup.picsou.gui.experiment;

import org.crossbowlabs.splits.utils.GuiUtils;
import org.designup.picsou.gui.utils.PicsouDialogPainter;
import org.designup.picsou.gui.utils.Gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;

public class JModalWindow extends JDialog {
  private static Stack<Parent> windows = new Stack<Parent>();
  private Parent parent;

  private PicsouDialogPainter painter = new PicsouDialogPainter();

  public static JModalWindow create(JFrame owner) {
    if (windows.isEmpty()) {
      windows.push(new ParentFrame(owner));
    }
    JModalWindow jModalWindow = windows.peek().create();
    windows.push(new ParentJModalWindow(jModalWindow));
    return jModalWindow;
  }

  public static JModalWindow create(JFrame owner, String title) {
    JModalWindow modalWindow = create(owner);
    modalWindow.setTitle(title);
    return modalWindow;
  }

  private JModalWindow(JFrame parentFrame, Parent parent) {
    super(parentFrame);
    this.parent = parent;
    parentFrame.setGlassPane(new JDarkenGlass());
    setUndecorated(true);
  }

  private JModalWindow(JDialog parentDialog, Parent parent) {
    super(parentDialog);
    this.parent = parent;
    parentDialog.setGlassPane(new JDarkenGlass());
    setUndecorated(true);
  }

  public void setVisible(boolean visible) {
    if (!windows.isEmpty()) {
      if (!windows.peek().check(this)) {
        System.out.println("PicsouDialog.setVisible on " + getTitle() + " but " +
                           windows.peek().getName() + " is in frame stack");
      }
    }
    if (visible) {
      JPanel container = (JPanel)getContentPane();
      if (getTitle() != null) {
        Gui.installWindowTitle(container, painter, getTitle(), 0);
        Gui.installMovingWindowTitle(this);
        pack();
      }

      GuiUtils.opacify(container);

      showDialog();
    }
    else {
      hideDialog();
    }
    super.setVisible(visible);
  }

  public void paint(Graphics g) {
    Insets vInsets = getInsets();

    int width = getWidth() - (vInsets.left + vInsets.right);
    int height = getHeight() - (vInsets.top + vInsets.bottom);

    int x = vInsets.left;
    int y = vInsets.top;

//    checkOpacity(getContentPane());

//    painter.paint(g, width, height);
    super.paint(g);
//    checkOpacity(getContentPane());
  }

  private void checkOpacity(Container container) {
    System.out.println("container = " + container.getClass().getSimpleName());
    System.out.println("container.isOpaque() = " + container.isOpaque());
    Component[] components = container.getComponents();
    for (Component component : components) {
      System.out.println("component = " + component.getClass().getSimpleName());
      System.out.println("component.isOpaque() = " + component.isOpaque());
      if (component instanceof Container) {
        checkOpacity((Container)component);
      }
    }
  }

  private void showDialog() {
    parent.getGlassPane().setVisible(true);
  }

  private void hideDialog() {
    parent.getGlassPane().setVisible(false);
    if (!windows.isEmpty()) {
      windows.pop();
      if (windows.size() == 1) {
        windows.pop();
      }
    }
    else {
      System.out.println("ERROR IN PicsouDialog.hideDialog");
    }
  }

  private static class JGlass extends JPanel {
    private JGlass() {
      setOpaque(false);
      addMouseListener(new MouseAdapter() {
      });
      addMouseMotionListener(new MouseMotionAdapter() {
      });
      addKeyListener(new KeyAdapter() {
      });
      addComponentListener(new ComponentAdapter() {
        public void componentShown(ComponentEvent e) {
          requestFocusInWindow();
        }
      });
      setFocusTraversalKeysEnabled(false);
    }
  }

  private static class JDarkenGlass extends JGlass {
    public void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D)g;
      Rectangle clip = g.getClipBounds();

      g2.setColor(new Color(0, 0, 0, 0.3f));
      g2.fillRect(clip.x, clip.y, clip.width, clip.height);
    }
  }

  interface Parent {
    JModalWindow create();

    Component getGlassPane();

    boolean check(JModalWindow jModalWindow);

    String getName();
  }

  static class ParentFrame implements Parent {
    private JFrame frame;

    public ParentFrame(JFrame frame) {
      this.frame = frame;
    }

    public JModalWindow create() {
      return new JModalWindow(frame, this);
    }

    public Component getGlassPane() {
      return frame.getGlassPane();
    }

    public boolean check(JModalWindow jModalWindow) {
      return false;
    }

    public String getName() {
      return "root";
    }
  }

  static class ParentJModalWindow implements Parent {
    private JModalWindow modalWindow;

    public ParentJModalWindow(JModalWindow modalWindow) {
      this.modalWindow = modalWindow;
    }

    public JModalWindow create() {
      return new JModalWindow(modalWindow, this);
    }

    public Component getGlassPane() {
      return modalWindow.getGlassPane();
    }

    public boolean check(JModalWindow jModalWindow) {
      return jModalWindow == modalWindow;
    }

    public String getName() {
      return modalWindow.getTitle();
    }
  }

}