package com.budgetview.bank.connectors.webcomponents.utils;

import com.budgetview.bank.BankConnector;
import com.budgetview.bank.BankConnectorFactory;
import com.budgetview.bank.BankPluginService;
import com.budgetview.desktop.Application;
import com.budgetview.desktop.browsing.BrowsingService;
import com.budgetview.desktop.browsing.DummyBrowsingService;
import com.budgetview.desktop.description.PicsouDescriptionService;
import com.budgetview.desktop.startup.components.OpenRequestManager;
import com.budgetview.desktop.utils.ApplicationColors;
import com.budgetview.bank.connectors.SynchroMonitor;
import com.budgetview.desktop.model.PicsouGuiModel;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.model.Synchro;
import com.budgetview.utils.Lang;
import org.globsframework.gui.SelectionService;
import org.globsframework.gui.splits.TextLocator;
import org.globsframework.gui.splits.layout.LayoutService;
import org.globsframework.gui.splits.ui.UIService;
import org.globsframework.gui.splits.utils.GuiUtils;
import org.globsframework.model.FieldValue;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.format.DescriptionService;
import org.globsframework.utils.directory.DefaultDirectory;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WebConnectorLauncher {
  public static void show(Integer bankId, BankConnectorFactory factory) throws IOException {

    GlobRepository repository = GlobRepositoryBuilder.init()
      .add(PicsouGuiModel.get().getConstants())
      .get();

    DefaultDirectory directory = createDirectoryWithDefaultServices(repository);

    BankConnector connector = factory.create(repository, directory, false, repository.create(Synchro.TYPE,
                                                                                             FieldValue.value(Synchro.BANK, bankId)));

    JFrame frame = new JFrame("Test: " + connector.getClass().getSimpleName());
    directory.add(JFrame.class, frame);

    JPanel connectorPanel = connector.getPanel();
    connectorPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    frame.setContentPane(connectorPanel);

    connector.init(new TextMonitor());

    frame.setSize(350, 300);
    GuiUtils.showCentered(frame);
  }

  private static DefaultDirectory createDirectoryWithDefaultServices(final GlobRepository repository) throws IOException {
    DefaultDirectory directory = new DefaultDirectory();
    directory.add(TextLocator.class, Lang.TEXT_LOCATOR);
    directory.add(SelectionService.class, new SelectionService());
    directory.add(BrowsingService.class, new DummyBrowsingService());
    directory.add(DescriptionService.class, new PicsouDescriptionService());
    OpenRequestManager openRequestManager = new OpenRequestManager();
    directory.add(OpenRequestManager.class, openRequestManager);
    ExecutorService executorService = Executors.newCachedThreadPool();
    directory.add(ExecutorService.class, executorService);
    directory.add(new UIService());
    directory.add(new LayoutService());
    ApplicationColors.registerColorService(directory);
    openRequestManager.pushCallback(new OpenRequestManager.Callback() {
      public boolean accept() {
        return true;
      }

      public void openFiles(List<File> files) {
        System.out.println("read: " + files.size());
      }
    });
    directory.add(new BankPluginService());
    directory.add(new TransactionAnalyzerFactory(PicsouGuiModel.get()));
    directory.get(TransactionAnalyzerFactory.class)
      .load(WebConnectorLauncher.class.getClassLoader(), Application.BANK_CONFIG_VERSION, repository, directory);

    return directory;
  }

  private static class TextMonitor implements SynchroMonitor {
    public void initialConnection() {
      System.out.println("Initial connection...");
    }

    public void waitingForUser() {
      System.out.println("Waiting for user...");
    }

    public void identificationInProgress() {
      System.out.println("Identification in progress...");
    }

    public void identificationFailed(String page) {
      System.out.println("Identification failed...");
    }

    public void preparingAccount(String accountName) {
      System.out.println("preparing account: " + accountName);
    }

    public void downloadInProgress() {
      System.out.println("Download in progress...");
    }

    public void downloadingAccount(String accountName) {
      System.out.println("downloading account: " + accountName);
    }

    public void errorFound(String errorMessage) {
      System.out.println("Error: " + errorMessage);
    }

    public void errorFound(Throwable exception) {
      System.out.println("Exception: " + exception.getMessage());
      System.out.flush();
      exception.printStackTrace();
      System.err.flush();
    }

    public void info(String message) {
      System.out.println("Info : " + message);
    }

    public void importCompleted(GlobList realAccounts) {
      System.out.println("Import completed: " + realAccounts);
    }
  }
}