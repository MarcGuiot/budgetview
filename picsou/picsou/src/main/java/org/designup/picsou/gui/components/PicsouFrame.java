package org.designup.picsou.gui.components;

import org.designup.picsou.gui.utils.Gui;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.GlobRepository;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

public class PicsouFrame extends JFrame {
  private GlobRepository repository;
  private boolean isIconified;

  public PicsouFrame(String title) {
    super(title);
    addWindowListener(new WindowAdapter() {
      public void windowIconified(WindowEvent e) {
        isIconified = true;
      }

      public void windowDeiconified(WindowEvent e) {
        isIconified = false;
      }
    });

    if (GuiUtils.isMacOSX()) {
      addWindowFocusListener(new WindowFocusListener() {
        public void windowLostFocus(WindowEvent e) {
          ToolTipManager.sharedInstance().setEnabled(false);
        }

        public void windowGainedFocus(WindowEvent e) {
          ToolTipManager.sharedInstance().setEnabled(true);
        }
      });
    }
    
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setIconImage(Gui.IMAGE_LOCATOR.get("app_icon_128.png").getImage());
  }

  public boolean isIconified() {
    return isIconified;
  }

  public GlobRepository getRepository() {
    return repository;
  }

  public void setRepository(GlobRepository repository) {
    this.repository = repository;
  }
}
