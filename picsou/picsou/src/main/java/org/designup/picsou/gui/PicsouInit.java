package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.config.RegistrationTrigger;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.upgrade.UpgradeService;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.model.initial.InitialCategories;
import org.designup.picsou.model.initial.InitialSeries;
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
  private Directory directory;

  public static PicsouInit init(ServerAccess serverAccess, String user, boolean newUser, Directory directory) throws IOException {
    return new PicsouInit(serverAccess, user, newUser, directory);
  }

  private PicsouInit(ServerAccess serverAccess, String user, boolean newUser, final Directory directory) throws IOException {
    this.directory = directory;

    final DefaultGlobIdGenerator generator = new DefaultGlobIdGenerator();
    repository =
      GlobRepositoryBuilder.init(new CachedGlobIdGenerator(generator))
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    generator.setRepository(repository);

    repository.addChangeListener(new ServerChangeSetListener(serverAccess));

    repository.addTrigger(new SavingsSeriesCreationTrigger());
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new SeriesRenameTrigger());
    repository.addTrigger(new RegistrationTrigger(directory));
    repository.addTrigger(new RegisterLicenseTrigger(serverAccess));
    repository.addTrigger(new FutureMonthTrigger(directory));
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new IrregularSeriesBudgetCreationTrigger());
    repository.addTrigger(new ObservedSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new SeriesBudgetUpdateTransactionTrigger());
    repository.addTrigger(new TransactionPlannedTrigger());
    repository.addTrigger(new BalanceTrigger());
    repository.addTrigger(new MonthStatTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());
    repository.addTrigger(new OccasionalSeriesStatTrigger());
    repository.addTrigger(new BalanceStatTrigger());

    if (!newUser) {
      repository.create(User.KEY, value(User.NAME, user));
    }
    MutableChangeSet changeSet = new DefaultChangeSet();
    try {
      GlobList userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdate() {

        public void update(IntegerField field, Integer lastAllocatedId) {
          generator.update(field, lastAllocatedId);
        }
      });
      Collection<GlobType> serverTypes = userData.getTypes();
      repository.reset(userData, serverTypes.toArray(new GlobType[serverTypes.size()]));
    }
    catch (Exception e) {
      throw new InvalidData(Lang.get("login.data.load.fail"), e);
    }
    serverAccess.applyChanges(changeSet, repository);
    Glob versionInfo;
    try {
      repository.enterBulkDispatchingMode();
      versionInfo = repository.find(VersionInformation.KEY);
      createDataForNewUser(user, repository);
    }
    finally {
      repository.completeBulkDispatchingMode();
    }

    initDirectory(repository);
    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION);
    }

    checkForUpgrade(repository, versionInfo == null && !newUser);

    LicenseCheckerThread licenseCheckerThread = new LicenseCheckerThread(directory, repository);
    licenseCheckerThread.setDaemon(true);
    licenseCheckerThread.start();
  }

  private void checkForUpgrade(GlobRepository repository, boolean forceUpgrade) {
    Glob version = repository.get(VersionInformation.KEY);
    if (!version.get(VersionInformation.CURRENT_JAR_VERSION).equals(PicsouApplication.JAR_VERSION)
        || forceUpgrade) {
      directory.get(UpgradeService.class).upgrade(repository, version);
    }

    if (!version.get(VersionInformation.CURRENT_BANK_CONFIG_VERSION)
      .equals(version.get(VersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION))
        || forceUpgrade) {
      directory.get(UpgradeService.class).applyFilter(repository, version);
    }

    Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
    if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
      repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                        Month.addOneMonth(TimeService.getToday()));
    }
  }

  public static void createDataForNewUser(String user, GlobRepository repository) {
    repository.findOrCreate(User.KEY,
                            value(User.NAME, user));
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
                            FieldValue.value(CurrentMonth.MONTH_ID, 0),
                            FieldValue.value(CurrentMonth.DAY, 0));
    repository.findOrCreate(Account.SUMMARY_KEY);
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
