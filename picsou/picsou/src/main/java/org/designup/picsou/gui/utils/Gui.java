package org.designup.picsou.gui.utils;

import org.crossbowlabs.splits.IconLocator;
import org.crossbowlabs.splits.utils.GuiUtils;
import org.crossbowlabs.splits.utils.JarIconLocator;
import sun.security.action.GetPropertyAction;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.security.AccessController;

public class Gui {
  private static final int TITLE_BAR_HEIGHT = 19;

  public static final Font DEFAULT_TABLE_FONT;
  public static final Font DEFAULT_TABLE_FONT_BOLD;
  private static final String MAC_PLATFORM_ID = "Mac OS X";
  private static final String LINUX_PLATFORM_ID = "Linux";

  public static char EURO = '\u20ac';

  public static final IconLocator ICON_LOCATOR = new JarIconLocator(Gui.class, "/images");

  private static Font font;

  static {
    Font labelFont = new JLabel().getFont();

    if (isMacOSX()) {
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN, ((float)labelFont.getSize() - 2));
    }
    else if (isLinux()){
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN);
    }
    else {
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN, labelFont.getSize() - 1);
    }

    DEFAULT_TABLE_FONT_BOLD = DEFAULT_TABLE_FONT.deriveFont(Font.BOLD);
  }

  private Gui() {
  }

  public static boolean isMacOSX() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(MAC_PLATFORM_ID);
  }

  public static boolean isLinux() {
    return ((String)AccessController.doPrivileged(new GetPropertyAction("os.name"))).contains(LINUX_PLATFORM_ID);
  }

  public static Font getDefaultFont() {
    if (font == null) {
      JLabel label = new JLabel();
      font = label.getFont();
    }
    return font;
  }

  public static void configureIconButton(AbstractButton button, String name, Dimension dimension) {
    button.setName(name);
    button.setSize(dimension);
    button.setOpaque(false);
    button.setText(null);
    button.setBorder(null);
    button.setBorderPainted(false);
    button.setContentAreaFilled(false);
    button.setFocusable(false);
    Gui.setRolloverCursor(button);
  }

  public static void setIcons(AbstractButton button, Icon mainIcon, Icon rolloverIcon, Icon pressedIcon) {
    button.setIcon(mainIcon);
    Gui.setRolloverIcon(button, rolloverIcon);
    button.setPressedIcon(pressedIcon);
  }

  public static void setRolloverIcon(AbstractButton button, Icon icon) {
    button.setRolloverEnabled(true);
    button.setRolloverIcon(icon);
  }

  public static void setRolloverIcon(final JLabel label, final Icon rolloverIcon) {
    label.addMouseListener(new RolloverIconListener(label, rolloverIcon));
  }

  public static void setRolloverColor(final JComponent component, final Color rolloverColor) {
    setRolloverCursor(component);
    component.addMouseListener(new RolloverColorListener(component, rolloverColor));
  }

  public static void setRolloverCursor(JComponent component) {
    component.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  public static void installRolloverOnButtons(final JTable table, final int[] editorColumns) {
    final TableColumnModel columnModel = table.getColumnModel();
    table.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        int graphicalColumnIndex = columnModel.getColumnIndexAtX(e.getX());
        int modelColumnIndex = columnModel.getColumn(graphicalColumnIndex).getModelIndex();
        if (modelColumnIndex < 0) {
          return;
        }
        for (int column : editorColumns) {
          if (modelColumnIndex == column) {
            int rowIndex = e.getY() / table.getRowHeight();
            if (table.isEditing() &&
                (table.getEditingColumn() == modelColumnIndex) &&
                (table.getEditingRow() == rowIndex)) {
              return;
            }
            table.editCellAt(rowIndex, column);
            return;
          }
        }
      }
    });
  }

  public static JPanel createHorizontalBoxLayoutPanel() {
    final JPanel panel = new JPanel();
    panel.setOpaque(false);
    panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
    return panel;
  }

  public static void installWindowTitle(JComponent mainComponent, final PicsouDialogPainter painter,
                                        final String title, int insets) {
    MatteBorder titleBorder = new MatteBorder(TITLE_BAR_HEIGHT, 0, 0, 0, Color.gray) {
      public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        drawTitle((Graphics2D)g, title, x, y, width, painter);
      }
    };

    Border emptyBorder = BorderFactory.createEmptyBorder(insets, insets, insets, insets);
    Border lineBorder = BorderFactory.createLineBorder(painter.getBorderColor(), 1);
    mainComponent.setBorder(BorderFactory.createCompoundBorder(lineBorder,
                                                               BorderFactory.createCompoundBorder(titleBorder, emptyBorder)));

    mainComponent.invalidate();
    mainComponent.revalidate();
  }

  public static void installMovingWindowTitle(Window movingWindow) {
    DialogMovingListener dialogMovingListener = new DialogMovingListener(movingWindow, TITLE_BAR_HEIGHT);
    movingWindow.addMouseListener(dialogMovingListener);
    movingWindow.addMouseMotionListener(dialogMovingListener);
  }

  private static void drawTitle(Graphics2D g2d, String title, int x, int y, int width, PicsouDialogPainter painter) {
    g2d.setFont(GuiUtils.getDefaultLabelFont());
    FontMetrics metrics = g2d.getFontMetrics();
    int fontHeight = (metrics.getMaxAscent() - metrics.getMaxDescent());
    int stringWidth = SwingUtilities.computeStringWidth(metrics, title);

    int titleX = x + (width / 2) - (stringWidth / 2);
    int titleY = y + (TITLE_BAR_HEIGHT / 2) + (fontHeight / 2);
    int titleHeight = TITLE_BAR_HEIGHT;

    g2d.setPaint(new GradientPaint(x, y, Color.WHITE, x, y + titleHeight, Color.GRAY));
    g2d.fillRect(x, y, width, titleHeight);

    g2d.setColor(Color.BLACK);
    g2d.drawString(title, titleX, titleY);

    g2d.setColor(painter.getBorderColor());
    g2d.drawLine(x, titleHeight, x + width, titleHeight);
  }

  public static class RolloverColorListener extends MouseAdapter {
    private final JComponent component;
    private final Color rolloverColor;
    private Color originalColor;

    public RolloverColorListener(JComponent component, Color rolloverColor) {
      this.component = component;
      this.rolloverColor = rolloverColor;
    }

    public void mouseEntered(MouseEvent e) {
      this.originalColor = component.getForeground();
      component.setForeground(rolloverColor);
    }

    public void mouseExited(MouseEvent e) {
      component.setForeground(originalColor);
    }
  }

  public static class RolloverIconListener extends MouseAdapter {
    private final JLabel label;
    private final Icon rolloverIcon;
    private Icon originalIcon;

    public RolloverIconListener(JLabel label, Icon rolloverIcon) {
      this.label = label;
      this.rolloverIcon = rolloverIcon;
    }

    public void mouseEntered(MouseEvent e) {
      this.originalIcon = label.getIcon();
      label.setIcon(rolloverIcon);
    }

    public void mouseExited(MouseEvent e) {
      label.setIcon(originalIcon);
    }
  }
}
