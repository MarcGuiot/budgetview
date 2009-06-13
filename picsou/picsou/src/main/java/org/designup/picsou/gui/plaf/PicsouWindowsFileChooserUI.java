package org.designup.picsou.gui.plaf;

import org.designup.picsou.gui.components.dialogs.PicsouDialogPainter;
import org.designup.picsou.gui.components.dialogs.MovingDialog;
import org.globsframework.gui.splits.utils.GuiUtils;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import java.awt.*;
import java.io.File;

public class PicsouWindowsFileChooserUI extends MetalFileChooserUI {
  private final BasicFileView fileView = new SystemIconFileView();
  private PicsouDialogPainter painter;
  private JDialog dialog;

  public PicsouWindowsFileChooserUI(JFileChooser fileChooser) {
    super(fileChooser);
  }

  public static ComponentUI createUI(JComponent c) {
    return new PicsouWindowsFileChooserUI((JFileChooser)c);
  }

  public void clearIconCache() {
    fileView.clearIconCache();
  }

  public FileView getFileView(JFileChooser fc) {
    return fileView;
  }

  private final class SystemIconFileView extends BasicFileView {
    public Icon getIcon(File f) {
      Icon icon = getCachedIcon(f);
      if (icon != null) {
        return icon;
      }
      if ((f != null) && UIManager.getBoolean("FileChooser.useSystemIcons")) {
        icon = getFileChooser().getFileSystemView().getSystemIcon(f);
      }
      if (icon == null) {
        return super.getIcon(f);
      }
      cacheIcon(f, icon);
      return icon;
    }
  }

  public void installUI(JComponent c) {
    super.installUI(c);
    dialog = null;
    if (painter == null) {
      painter = new PicsouDialogPainter();
    }
    MovingDialog.installWindowTitle(c, painter, getDialogTitle((JFileChooser)c), 10);
  }

  public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    installMovingWindowTitle(c);
  }

  private void installMovingWindowTitle(JComponent c) {
    if (dialog != null) {
      return;
    }

    dialog = (JDialog)GuiUtils.getEnclosingComponent(c, new GuiUtils.ComponentMatcher() {
      public boolean matches(Component component) {
        return JDialog.class.isInstance(component);
      }
    });

    MovingDialog.installMovingWindowTitle(dialog);
  }
}

