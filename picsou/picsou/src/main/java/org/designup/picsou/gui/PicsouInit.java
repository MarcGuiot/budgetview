package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.config.RegistrationTrigger;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.gui.utils.ExceptionHandler;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.PicsouModel;
import org.designup.picsou.model.User;
import org.designup.picsou.triggers.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.CachedGlobIdGenerator;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.xml.XmlGlobParser;

import javax.swing.*;
import java.io.*;

public class PicsouInit {

  private GlobRepository repository;
  private ServerAccess serverAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;

  public static PicsouInit init(ServerAccess serverAccess, String user, boolean validUser,
                                boolean newUser, Directory directory) throws IOException {
    return new PicsouInit(serverAccess, user, validUser, newUser, directory);
  }

  private PicsouInit(ServerAccess serverAccess, String user, boolean validUser,
                     boolean newUser, final Directory directory) throws IOException {
    this.serverAccess = serverAccess;
    this.directory = directory;

    idGenerator = new DefaultGlobIdGenerator();
    this.repository =
      GlobRepositoryBuilder.init(new CachedGlobIdGenerator(idGenerator))
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    ExceptionHandler.setRepository(repository);

    this.repository.addChangeListener(new ServerChangeSetListener(serverAccess));

    upgradeTrigger = new UpgradeTrigger(directory, user, validUser);
    this.repository.addTrigger(upgradeTrigger);
    this.repository.addTrigger(new CurrentMonthTrigger());
    this.repository.addTrigger(new SeriesRenameTrigger());
    this.repository.addTrigger(new SeriesDeletionTrigger());
    this.repository.addTrigger(new RegistrationTrigger(directory));
    this.repository.addTrigger(new RegisterLicenseTrigger(serverAccess));
    this.repository.addTrigger(new FutureMonthTrigger(directory));
    this.repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    this.repository.addTrigger(new IrregularSeriesBudgetCreationTrigger());
    this.repository.addTrigger(new NotImportedTransactionAccountTrigger());
    this.repository.addTrigger(new ObservedSeriesStatTrigger());
    this.repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    this.repository.addTrigger(new TransactionPlannedTrigger());
    this.repository.addTrigger(new ImportedToNotImportedAccountTransactionTrigger());
    this.repository.addTrigger(new UpdateAccountOnTransactionDelete());
    this.repository.addTrigger(new PositionTrigger());
    this.repository.addTrigger(new PlannedSeriesStatTrigger());
    this.repository.addTrigger(new BalanceStatTrigger());
    this.repository.addTrigger(new SavingsBalanceStatTrigger());

    initDirectory(this.repository);

    if (!newUser) {
      this.repository.create(User.KEY,
                             value(User.NAME, user),
                             value(User.IS_REGISTERED_USER, validUser));
    }

    try {
      this.repository.startChangeSet();
      MutableChangeSet changeSet = new DefaultChangeSet();
      GlobList userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
          idGenerator.update(field, lastAllocatedId);
        }
      });
      this.repository.reset(userData, GlobUtils.toArray(userData.getTypes()));

      serverAccess.applyChanges(changeSet, this.repository);

      this.repository.completeChangeSet();
    }
    catch (Exception e) {
      throw new InvalidData(Lang.get("login.data.load.fail"), e);
    }
    repository.removeTrigger(upgradeTrigger);

    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION);
    }

    LicenseCheckerThread.launch(directory, this.repository);
  }

  private void initDirectory(GlobRepository repository) {
    directory.add(BrowsingService.class, BrowsingService.createService());

    TransactionAnalyzerFactory factory = new TransactionAnalyzerFactory(PicsouGuiModel.get(), repository);
    directory.add(TransactionAnalyzerFactory.class, factory);
    ImportService importService = new ImportService();
    directory.add(ImportService.class, importService);

    directory.add(new BackupService(serverAccess, repository, idGenerator, upgradeTrigger));
  }

  private static class ServerChangeSetListener extends DefaultChangeSetListener {
    private ServerAccess serverAccess;

    public ServerChangeSetListener(ServerAccess serverAccess) {
      this.serverAccess = serverAccess;
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      serverAccess.applyChanges(changeSet, repository);
    }
  }

  public Directory getDirectory() {
    return directory;
  }

  public GlobRepository getRepository() {
    return repository;
  }

  private void loadGlobs(String fileName) {
    Reader reader;
    try {
      InputStream stream = PicsouInit.class.getResourceAsStream(fileName);
      if (stream == null) {
        throw new ResourceAccessFailed("Resource file not found:" + fileName);
      }
      reader = new InputStreamReader(stream, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new UnexpectedApplicationState(e);
    }
    XmlGlobParser.parse(PicsouModel.get(), repository, reader, "globs");
  }

  private static class LicenseCheckerThread extends Thread {
    private Directory directory;
    private GlobRepository repository;

    public static void launch(Directory directory, GlobRepository repository) {
      LicenseCheckerThread thread = new LicenseCheckerThread(directory, repository);
      thread.setDaemon(true);
      thread.start();
    }

    private LicenseCheckerThread(Directory directory, GlobRepository repository) {
      this.directory = directory;
      this.repository = repository;
    }

    public void run() {
      ConfigService.waitEndOfConfigRequest(directory);
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          ConfigService.check(directory, repository);
          repository.update(User.KEY, User.CONNECTED, true);
        }
      });
    }
  }

}
