package org.designup.picsou.gui;

import org.designup.picsou.bank.SpecificBankLoader;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.accounts.utils.MonthDay;
import org.designup.picsou.gui.backup.BackupService;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.config.RegistrationTrigger;
import org.designup.picsou.gui.license.LicenseService;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.preferences.components.ColorThemeUpdater;
import org.designup.picsou.gui.series.view.SeriesWrapperUpdateTrigger;
import org.designup.picsou.gui.time.TimeService;
import org.designup.picsou.gui.upgrade.ConfigUpgradeTrigger;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.gui.utils.FrameSize;
import org.designup.picsou.gui.utils.ShowDialogAndExitExceptionHandler;
import org.designup.picsou.gui.utils.datacheck.DataCheckingService;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.model.initial.DefaultSeriesFactory;
import org.designup.picsou.triggers.*;
import org.designup.picsou.triggers.projects.*;
import org.designup.picsou.triggers.savings.SavingsUpdateSeriesMirrorTrigger;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.repository.DefaultGlobIdGenerator;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobMatchers;
import org.globsframework.utils.Log;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import picsou.AwtExceptionHandler;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.globsframework.model.FieldValue.value;

public class PicsouInit {

  private GlobRepository repository;
  private ServerAccess serverAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;
  private ServerChangeSetListener changeSetListenerToDb;
  private ShowDialogAndExitExceptionHandler exceptionHandler;

  public static PicsouInit init(ServerAccess serverAccess, Directory directory, boolean registeredUser, boolean badJarVersion) {
    return new PicsouInit(serverAccess, directory, registeredUser, badJarVersion);
  }

  private PicsouInit(ServerAccess serverAccess, final Directory directory, boolean registeredUser, boolean badJarVersion) {
    this.serverAccess = serverAccess;
    this.directory = directory;

    this.idGenerator = new DefaultGlobIdGenerator();
    this.exceptionHandler = new ShowDialogAndExitExceptionHandler(directory);
    this.repository =
      GlobRepositoryBuilder.init(idGenerator, exceptionHandler)
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    repository.findOrCreate(User.KEY,
                            value(User.ACTIVATION_STATE, badJarVersion ? User.STARTUP_CHECK_JAR_VERSION : null),
                            value(User.IS_REGISTERED_USER, registeredUser));
    repository.findOrCreate(AppVersionInformation.KEY,
                            value(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(AppVersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));

    AwtExceptionHandler.setRepository(repository, directory);

    changeSetListenerToDb = new ServerChangeSetListener(serverAccess);
    this.repository.addChangeListener(changeSetListenerToDb);

    upgradeTrigger = new UpgradeTrigger(directory);
    initTriggers(serverAccess, directory, this.repository);
    repository.addTrigger(new SeriesWrapperUpdateTrigger());

    initDirectory(this.repository);

    initBank(directory);

    ColorThemeUpdater.register(repository, directory);
  }

  private void initBank(Directory directory) {
    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION, repository, directory);
    }

    SpecificBankLoader bankLoader = new SpecificBankLoader();
    bankLoader.load(repository, directory);
  }

  public static void initTriggers(ServerAccess serverAccess, Directory directory, final GlobRepository repository) {
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new MonthTrigger(directory));
    repository.addTrigger(new AccountInitialPositionTrigger());
    repository.addTrigger(new DeleteInitialSeriesTrigger());
    repository.addTrigger(new DeleteUnusedSeriesGroupTrigger());
    repository.addTrigger(new ProjectTransferAccountChangeTrigger());
    repository.addTrigger(new ProjectToItemTrigger());
    repository.addTrigger(new ProjectItemToAmountGlobalTrigger());
    repository.addTrigger(new ProjectItemToSeriesTrigger());
    repository.addTrigger(new ProjectTransferToSeriesTrigger());
    repository.addTrigger(new ProjectTransferDeletionTrigger());
    repository.addTrigger(new ProjectToSeriesGroupTrigger());
    repository.addTrigger(new ProjectToStatTrigger());
    repository.addTrigger(new ProjectItemToStatTrigger());
    repository.addTrigger(new ProjectItemToSeriesBudgetTrigger());
    repository.addTrigger(new ProjectItemToProjectStatTrigger());
    repository.addTrigger(new HideProjectDetailsTrigger());
    repository.addTrigger(new SavingsAccountUpdateSeriesTrigger());
    repository.addTrigger(new SavingsUpdateSeriesMirrorTrigger());
    repository.addTrigger(new SavingsDateActiveBudgetTrigger());
    repository.addTrigger(new UpdateActiveBudgetTrigger());
    repository.addTrigger(new ConfigUpgradeTrigger(directory));
    repository.addTrigger(new SeriesRenameTrigger());
    repository.addTrigger(new AccountDeleteTrigger());
    repository.addTrigger(new SeriesDeletionTrigger());
    repository.addTrigger(new SubSeriesDeletionTrigger());
    repository.addTrigger(new RegistrationTrigger(directory));
    repository.addTrigger(new RegisterLicenseTrigger(serverAccess));
    repository.addTrigger(new DayTrigger());
    repository.addTrigger(new DeferredAccountTrigger());
    repository.addTrigger(new DeferredCardDayTrigger());
    repository.addTrigger(new DeferredOperationTrigger());
    repository.addTrigger(new DeferredDayChangeTrigger());
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new IrregularSeriesBudgetCreationTrigger());
    repository.addTrigger(new NotImportedTransactionAccountTrigger());
    repository.addTrigger(new ImportedToNotImportedAccountTransactionTrigger());
    repository.addTrigger(new ObservedSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new SeriesShapeTrigger());
    repository.addTrigger(new TransactionPlannedTrigger());
    repository.addTrigger(new UpdateAccountOnTransactionDelete());
    repository.addTrigger(new PositionTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());
    repository.addTrigger(new SeriesStatSummaryTrigger());
    repository.addTrigger(new BudgetStatTrigger());
    repository.addTrigger(new SavingsBudgetStatTrigger());
    repository.addTrigger(new SubSeriesStatTrigger());
    repository.addTrigger(new SeriesStatToProjectStatTrigger());
    repository.addTrigger(new SeriesStatToProjectItemStatTrigger());
    repository.addTrigger(new ProjectCategorizationWarningTrigger());
    repository.addTrigger(new SeriesStatForGroupsTrigger());
    repository.addTrigger(new DateFormatTrigger(directory));
    repository.addTrigger(new ProjectPicturesDelectionTrigger());
  }

  public PreLoadData loadUserData(String user, boolean useDemoAccount, boolean autoLogin) {
    return new PreLoadData(user, useDemoAccount, autoLogin);
  }

  public void partialReset() {
    GlobList additionalGlobToInsert = additionalGlobToAdd(PicsouInit.this.repository);
    repository.reset(GlobList.EMPTY, PicsouGuiModel.getUserSpecificTypes());
    Set<GlobType> types = additionalGlobToInsert.getTypes();
    repository.reset(additionalGlobToInsert, types.toArray(new GlobType[types.size()]));
  }

  class PreLoadData {
    MutableChangeSet changeSet;
    GlobList userData;
    private String user;
    private boolean useDemoAccount;
    private GlobType[] typesToReplace;
    boolean autoLogin;

    PreLoadData(String user, boolean useDemoAccount, boolean autoLogin) {
      this.user = user;
      this.useDemoAccount = useDemoAccount;
      this.autoLogin = autoLogin;
      changeSet = new DefaultChangeSet();
      typesToReplace = PicsouGuiModel.getUserSpecificTypes();
      idGenerator.reset(Arrays.asList(typesToReplace));

      userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
          idGenerator.update(field, lastAllocatedId);
        }
      });
    }

    public void load() {
      GlobList additionalGlobToInsert = additionalGlobToAdd(PicsouInit.this.repository);

      repository.reset(GlobList.EMPTY, typesToReplace);
      repository.addTriggerAtFirst(upgradeTrigger);

      try {
        repository.startChangeSet();
        repository.update(User.KEY,
                          value(User.NAME, user),
                          value(User.AUTO_LOGIN, autoLogin),
                          value(User.IS_DEMO_USER, useDemoAccount));
        if (userData.isEmpty()) {
          createTransientDataForNewUser(repository);
          createPersistentDataForNewUser(repository, directory);
          repository.completeChangeSet();
          Set<GlobType> types = additionalGlobToInsert.getTypes();
          repository.reset(additionalGlobToInsert, types.toArray(new GlobType[types.size()]));
        }
        else {
          repository.completeChangeSet();
          exceptionHandler.setFirstReset(true);
          userData.addAll(additionalGlobToInsert);
          try {
            repository.reset(userData, typesToReplace);
          }
          catch (Exception e) {
            e.printStackTrace();
            GlobRepository repository =
              GlobRepositoryBuilder.init(idGenerator)
                .add(directory.get(GlobModel.class).getConstants())
                .get();
            repository.addChangeListener(changeSetListenerToDb);
            // reload data to lauch the check on saved data
            userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
              public void update(IntegerField field, Integer lastAllocatedId) {
                idGenerator.update(field, lastAllocatedId);
              }
            });
            userData.addAll(additionalGlobToInsert);
            repository.reset(userData, typesToReplace);
            DataCheckingService dataChecker = new DataCheckingService(repository, directory);
            dataChecker.check(e);

            userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
              public void update(IntegerField field, Integer lastAllocatedId) {
                idGenerator.update(field, lastAllocatedId);
              }
            });
            PicsouInit.this.repository.reset(GlobList.EMPTY, typesToReplace);
            PicsouInit.this.repository.reset(userData, typesToReplace);
          }
        }

        UserPreferences.initMobilePassword(repository, false);

        serverAccess.applyChanges(changeSet, repository);
      }
      catch (Exception e) {
        Log.write("In load ", e);
        throw new InvalidData(Lang.get("login.data.load.fail"), e);
      }
      finally {
        exceptionHandler.setFirstReset(false);
        repository.removeTrigger(upgradeTrigger);
        repository.startChangeSet();
        try {
          upgradeTrigger.postProcessing(repository);
        }
        finally {
          repository.completeChangeSet();
        }
      }
      repository.update(CurrentMonth.KEY,
                        value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                        value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
    }
  }

  public static GlobList additionalGlobToAdd(final GlobRepository globRepository) {
    GlobList additionalGlobToInsert = new GlobList();
    for (Glob glob : globRepository.getAll(Bank.TYPE, GlobMatchers.isFalse(Bank.USER_CREATED))) {
      additionalGlobToInsert.add(glob);
    }
    for (Glob glob : globRepository.getAll(BankEntity.TYPE,
                                           GlobMatchers.linkTargetFieldEquals(BankEntity.BANK, Bank.USER_CREATED, Boolean.FALSE))) {
      additionalGlobToInsert.add(glob);
    }
    return additionalGlobToInsert;
  }

  private void initDirectory(GlobRepository repository) {
    directory.add(ExecutorService.class, Executors.newCachedThreadPool());
    directory.add(BrowsingService.class, BrowsingService.createService());
    directory.add(TransactionAnalyzerFactory.class, new TransactionAnalyzerFactory(PicsouGuiModel.get()));
    directory.add(ImportService.class, new ImportService());
    directory.add(new BackupService(serverAccess, directory, repository, idGenerator, upgradeTrigger));
  }

  public static void createTransientDataForNewUser(GlobRepository repository) {
    repository.startChangeSet();
    try {
      for (int i = 1; i < 32; i++) {
        repository.findOrCreate(Key.create(MonthDay.TYPE, i));
      }
    }
    finally {
      repository.completeChangeSet();
    }

  }

  private void createPersistentDataForNewUser(GlobRepository repository, Directory directory) {

    repository.startChangeSet();
    try {
      repository.findOrCreate(Notes.KEY, value(Notes.TEXT, Lang.get("notes.initial")));
      repository.findOrCreate(UserVersionInformation.KEY,
                              value(UserVersionInformation.CURRENT_JAR_VERSION, PicsouApplication.JAR_VERSION),
                              value(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                              value(UserVersionInformation.CURRENT_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));

      Glob userPreferences = repository.findOrCreate(UserPreferences.KEY);
      if (userPreferences.get(UserPreferences.LAST_VALID_DAY) == null) {
        repository.update(userPreferences.getKey(), UserPreferences.LAST_VALID_DAY,
                          LicenseService.getEndOfTrialPeriod());
      }

      FrameSize frameSize = FrameSize.init(directory.get(JFrame.class));
      LayoutConfig.find(frameSize.screenSize, frameSize.targetFrameSize, repository, true);

      repository.findOrCreate(CurrentMonth.KEY,
                              value(CurrentMonth.LAST_TRANSACTION_DAY, 0),
                              value(CurrentMonth.LAST_TRANSACTION_MONTH, 0),
                              value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                              value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));
      repository.findOrCreate(Account.MAIN_SUMMARY_KEY,
                              value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()),
                              value(Account.IS_IMPORTED_ACCOUNT, true));
      repository.findOrCreate(Account.EXTERNAL_KEY,
                              value(Account.IS_IMPORTED_ACCOUNT, false));
      repository.findOrCreate(Account.SAVINGS_SUMMARY_KEY,
                              value(Account.ACCOUNT_TYPE, AccountType.SAVINGS.getId()));
      repository.findOrCreate(Account.ALL_SUMMARY_KEY,
                              value(Account.ACCOUNT_TYPE, AccountType.MAIN.getId()));

      DefaultSeriesFactory.run(repository, directory);
    }
    finally {
      repository.completeChangeSet();
    }
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

}
