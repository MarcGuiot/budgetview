package org.designup.picsou.bank.importer.webcomponents.utils;

import org.designup.picsou.bank.BankConnectorDisplay;
import org.designup.picsou.gui.description.PicsouDescriptionService;
import org.designup.picsou.gui.startup.components.OpenRequestManager;
import org.designup.picsou.gui.utils.ApplicationColors;
import org.designup.picsou.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class WebConnectorLauncher {
  public static void show(BankConnectorDisplay factory) throws IOException {
    DefaultDirectory directory = new DefaultDirectory();
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    directory.add(SelectionService.class, new SelectionService());
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    OpenRequestManager openRequestManager = new OpenRequestManager();
    directory.add(OpenRequestManager.class, openRequestManager);
    directory.add(new UIService());
    ApplicationColors.registerColorService(directory);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        return true;
      }

      public void openFiles(List<File> files) {
        System.out.println("read " + files.size());
      }
    });

    JFrame frame = new JFrame("test SG");
    directory.add(JFrame.class, frame);
    frame.setSize(100, 100);
    frame.setVisible(true);

    GlobRepository repository = new DefaultGlobRepository(new DefaultGlobIdGenerator());
    factory.show(frame, directory, repository);
  }
}
