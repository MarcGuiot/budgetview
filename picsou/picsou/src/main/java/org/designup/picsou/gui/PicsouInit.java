package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.config.RegistrationTrigger;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.upgrade.UpgradeService;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.model.initial.InitialCategories;
import org.designup.picsou.model.initial.InitialSeries;
import org.designup.picsou.server.model.SerializableGlobType;
import org.designup.picsou.server.persistence.direct.ReadOnlyAccountDataManager;
import org.designup.picsou.triggers.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.CachedGlobIdGenerator;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.MapOfMaps;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import org.globsframework.utils.exceptions.ResourceAccessFailed;
import org.globsframework.utils.exceptions.UnexpectedApplicationState;
import org.globsframework.xml.XmlGlobParser;

import javax.swing.*;
import java.io.*;
import java.util.Collection;

public class PicsouInit {

  private GlobRepository repository;
  private ServerAccess serverAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;

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

    idGenerator.setRepository(this.repository);

    this.repository.addChangeListener(new ServerChangeSetListener(serverAccess));

    UpgradeTrigger upgradeTrigger = new UpgradeTrigger();
    this.repository.addTrigger(upgradeTrigger);
    this.repository.addTrigger(new UncategorizeOnAccountChangeTrigger());
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
    this.repository.addTrigger(new BalanceTrigger());
    this.repository.addTrigger(new MonthStatTrigger());
    this.repository.addTrigger(new PlannedSeriesStatTrigger());
    this.repository.addTrigger(new OccasionalSeriesStatTrigger());
    this.repository.addTrigger(new BalanceStatTrigger());
    this.repository.addTrigger(new SavingsBalanceStatTrigger());

    if (!newUser) {
      this.repository.create(User.KEY,
                             value(User.NAME, user),
                             value(User.IS_REGISTERED_USER, validUser));
    }

    MutableChangeSet changeSet = new DefaultChangeSet();
    try {
      GlobList userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
          idGenerator.update(field, lastAllocatedId);
        }
      });
      this.repository.reset(userData, GlobUtils.toArray(userData.getTypes()));
    }
    catch (Exception e) {
      throw new InvalidData(Lang.get("login.data.load.fail"), e);
    }
    repository.removeTrigger(upgradeTrigger);

    serverAccess.applyChanges(changeSet, this.repository);

    Glob versionInfo;
    try {
      this.repository.startChangeSet();
      versionInfo = this.repository.find(VersionInformation.KEY);
      createDataForNewUser(user, this.repository, validUser);
    }
    finally {
      this.repository.completeChangeSet();
    }

    initDirectory(this.repository);
    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION);
    }

    boolean forceUpgrade = versionInfo == null && !newUser;

    final UpgradeService upgradeService = directory.get(UpgradeService.class);

    Glob version = repository.get(VersionInformation.KEY);
    if (forceUpgrade || !version.get(VersionInformation.CURRENT_BANK_CONFIG_VERSION).equals(version.get(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))) {
      upgradeService.upgradeBankData(repository, version);
    }

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }

    try {
      this.repository.startChangeSet();
      this.repository.update(CurrentMonth.KEY,
                             value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                             value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
    }
    finally {
      this.repository.completeChangeSet();
    }

    LicenseCheckerThread.launch(directory, this.repository);
  }

  public static void createDataForNewUser(String user, GlobRepository repository, boolean validUser) {
    repository.findOrCreate(User.KEY,
                            value(User.NAME, user),
                            value(User.IS_REGISTERED_USER, validUser));
    repository.findOrCreate(VersionInformation.KEY,
                            value(VersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(VersionInformation.CURRENT_BANK_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(VersionInformation.CURRENT_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION),
                            value(VersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(VersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));
    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }

    repository.findOrCreate(CurrentMonth.KEY,
                            value(CurrentMonth.LAST_TRANSACTION_MONTH, 0),
                            value(CurrentMonth.LAST_TRANSACTION_DAY, 0),
                            value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                            value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
    repository.findOrCreate(Account.MAIN_SUMMARY_KEY,
                            value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
                            value(Account.IS_IMPORTED_ACCOUNT, true));
    repository.findOrCreate(Account.SAVINGS_SUMMARY_KEY,
                            FieldValue.value(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()));
    repository.findOrCreate(Account.ALL_SUMMARY_KEY);
    InitialCategories.run(repository);
    InitialSeries.run(repository);
  }

  private void initDirectory(GlobRepository repository) {
    directory.add(BrowsingService.class, BrowsingService.createService());

    TransactionAnalyzerFactory factory = new TransactionAnalyzerFactory(PicsouGuiModel.get(), repository);
    directory.add(TransactionAnalyzerFactory.class, factory);
    ImportService importService = new ImportService();
    directory.add(ImportService.class, importService);
  }

  public void restore(InputStream stream) {
    MapOfMaps<String, Integer, SerializableGlobType> serverData =
      new MapOfMaps<String, Integer, SerializableGlobType>();
    ReadOnlyAccountDataManager.readSnapshot(serverData, stream);
    serverAccess.replaceData(serverData);

    MutableChangeSet changeSet = new DefaultChangeSet();
    GlobList userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {

      public void update(IntegerField field, Integer lastAllocatedId) {
        idGenerator.update(field, lastAllocatedId);
      }
    });
    Collection<GlobType> serverTypes = userData.getTypes();
    repository.reset(userData, serverTypes.toArray(new GlobType[serverTypes.size()]));
  }

  public void generateBackupIn(String file) throws IOException {
    MapOfMaps<String, Integer, SerializableGlobType> serverData = serverAccess.getServerData();
    ReadOnlyAccountDataManager.writeSnapshot_V2(serverData, new File(file));
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
