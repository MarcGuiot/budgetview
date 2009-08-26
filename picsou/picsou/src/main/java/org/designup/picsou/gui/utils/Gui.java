package org.designup.picsou.gui.utils;

import org.globsframework.gui.splits.ImageLocator;
import org.globsframework.gui.splits.font.FontLocator;
import org.globsframework.gui.splits.font.FontService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.gui.splits.utils.JarImageLocator;
import org.globsframework.gui.utils.TableUtils;
import org.globsframework.utils.Utils;
import org.designup.picsou.gui.plaf.PicsouMacLookAndFeel;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;

import com.jgoodies.looks.Options;
import com.jgoodies.looks.plastic.PicsouWindowsLookAndFeel;

public class Gui {

  public static final Font DEFAULT_TABLE_FONT;
  public static final Font DEFAULT_TABLE_FONT_BOLD;

  public static char EURO = '\u20ac';

  public static final ImageLocator IMAGE_LOCATOR = new JarImageLocator(Gui.class, "/images");
  public static final FontLocator FONT_LOCATOR = new FontService(Gui.class, "/fonts.properties");

  private static Font font;
  public static final int DEFAULT_COLUMN_CHAR_WIDTH = 7;

  private static Color selectionBackground;
  public static final Insets NO_INSETS = new Insets(0, 0, 0, 0);

  static {

    Font labelFont = new JLabel().getFont();

    if (GuiUtils.isMacOSX()) {
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN, ((float)labelFont.getSize() - 2));
    }
    else if (GuiUtils.isLinux()) {
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN);
    }
    else {
      DEFAULT_TABLE_FONT = labelFont.deriveFont(Font.PLAIN, labelFont.getSize() - 1);
    }

    DEFAULT_TABLE_FONT_BOLD = DEFAULT_TABLE_FONT.deriveFont(Font.BOLD);

    JTable table = new JTable();
    selectionBackground = table.getSelectionBackground();
  }

  private Gui() {
  }

  public static void init() {

    try {
      if (GuiUtils.isMacOSX()) {
        UIManager.setLookAndFeel(new PicsouMacLookAndFeel());
      }
      else {
        Options.setUseSystemFonts(true);
        Options.setUseNarrowButtons(false);

        PicsouWindowsLookAndFeel.set3DEnabled(true);
        PicsouWindowsLookAndFeel.setHighContrastFocusColorsEnabled(false);
        PicsouWindowsLookAndFeel.setSelectTextOnKeyboardFocusGained(false);

        UIManager.put("FileChooser.useSystemIcons", Boolean.TRUE);
        UIManager.setLookAndFeel(new PicsouWindowsLookAndFeel());
      }
      JDialog.setDefaultLookAndFeelDecorated(false);
      ToolTipManager.sharedInstance().setInitialDelay(500);
      ToolTipManager.sharedInstance().setDismissDelay(100000);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }


  public static Font getDefaultFont() {
    if (font == null) {
      JLabel label = new JLabel();
      font = label.getFont();
    }
    return font;
  }

  public static Color getDefaultTableSelectionBackground() {
    return selectionBackground;
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

  public static void installRolloverOnButtons(final JTable table, final int... editorColumns) {
    table.addMouseMotionListener(new MouseMotionAdapter() {
      public void mouseMoved(MouseEvent e) {
        final TableColumnModel columnModel = table.getColumnModel();
        int graphicalColumnIndex = columnModel.getColumnIndexAtX(e.getX());
        int modelColumnIndex = columnModel.getColumn(graphicalColumnIndex).getModelIndex();
        if (modelColumnIndex < 0) {
          return;
        }
        for (int column : editorColumns) {
          if (modelColumnIndex == column) {
            int row = e.getY() / table.getRowHeight();
            if (table.isEditing() &&
                (table.getEditingColumn() == modelColumnIndex) &&
                (table.getEditingRow() == row)) {
              return;
            }
            table.editCellAt(row, column);
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

  public static Dimension getWindowSize(int width, int height) {
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    return new Dimension(Math.min(width, screenSize.width),
                         Math.min(height, screenSize.height));
  }

  public static void setColumnSizes(JTable targetTable, int[] columnSizes) {
    int columns = Math.min(columnSizes.length, targetTable.getColumnCount() - 1);
    for (int column = 0; column < columns; column++) {
      final int width = columnSizes[column] * DEFAULT_COLUMN_CHAR_WIDTH;
      TableUtils.setSize(targetTable, column, width);
    }
  }

  public static JEditorPane createHtmlEditor(String text) {
    JEditorPane editor = new JEditorPane();
    editor.setContentType("text/html");
    editor.setText(text);
    editor.setCaretPosition(0);
    return editor;
  }

  public static void setWaitCursor(JFrame frame) {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
  }

  public static void setDefaultCursor(JFrame frame) {
    frame.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
  }

  public static boolean useMacOSMenu() {
    boolean result = GuiUtils.isMacOSX();
    Utils.beginRemove();
    result = false;
    Utils.endRemove();
    return result;
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

  public static boolean hasModifiers(ActionEvent e) {
    return ((e.getModifiers() & InputEvent.CTRL_MASK) == InputEvent.CTRL_MASK) ||
           ((e.getModifiers() & InputEvent.SHIFT_MASK) == InputEvent.SHIFT_MASK) ||
           ((e.getModifiers() & InputEvent.ALT_MASK) == InputEvent.ALT_MASK) ||
           ((e.getModifiers() & InputEvent.META_MASK) == InputEvent.META_MASK);
  }

  public static JLabel createInvisibleLabel() {
    JLabel empty = new JLabel();
    empty.setVisible(false);
    return empty;
  }
}
