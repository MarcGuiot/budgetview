package com.budgetview.desktop.plaf;

import javax.swing.*;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalFileChooserUI;
import java.io.File;

public class PicsouWindowsFileChooserUI extends MetalFileChooserUI {
  private final BasicFileView fileView = new SystemIconFileView();

  public PicsouWindowsFileChooserUI(JFileChooser fileChooser) {
    super(fileChooser);
  }

  public static ComponentUI createUI(JComponent c) {
    return new PicsouWindowsFileChooserUI((JFileChooser) c);
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
}

