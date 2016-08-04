package com.budgetview.desktop;

import com.budgetview.bank.SpecificBankLoader;
import com.budgetview.client.DataAccess;
import com.budgetview.desktop.accounts.utils.MonthDay;
import com.budgetview.desktop.backup.BackupService;
import com.budgetview.desktop.browsing.BrowsingService;
import com.budgetview.desktop.userconfig.UserConfigService;
import com.budgetview.desktop.userconfig.triggers.LicenseActivationTrigger;
import com.budgetview.desktop.model.PicsouGuiModel;
import com.budgetview.desktop.preferences.components.ColorThemeUpdater;
import com.budgetview.desktop.series.view.SeriesWrapperUpdateTrigger;
import com.budgetview.desktop.time.TimeService;
import com.budgetview.desktop.upgrade.ConfigUpgradeTrigger;
import com.budgetview.desktop.upgrade.UpgradeTrigger;
import com.budgetview.desktop.utils.AwtExceptionHandler;
import com.budgetview.desktop.utils.FrameSize;
import com.budgetview.desktop.utils.ShowDialogAndExitExceptionHandler;
import com.budgetview.desktop.utils.datacheck.DataCheckingService;
import com.budgetview.io.importer.ImportService;
import com.budgetview.io.importer.analyzer.TransactionAnalyzerFactory;
import com.budgetview.model.*;
import com.budgetview.model.initial.DefaultSeriesFactory;
import com.budgetview.triggers.*;
import com.budgetview.triggers.projects.*;
import com.budgetview.triggers.savings.SavingsUpdateSeriesMirrorTrigger;
import com.budgetview.utils.Lang;
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

import javax.swing.*;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.globsframework.model.FieldValue.value;

public class PicsouInit {

  private GlobRepository repository;
  private DataAccess dataAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;
  private ServerChangeSetListener changeSetListenerToDb;
  private ShowDialogAndExitExceptionHandler exceptionHandler;

  public static PicsouInit init(DataAccess dataAccess, Directory directory, boolean registeredUser, boolean badJarVersion) {
    return new PicsouInit(dataAccess, directory, registeredUser, badJarVersion);
  }

  private PicsouInit(DataAccess dataAccess, final Directory directory, boolean registeredUser, boolean badJarVersion) {
    this.dataAccess = dataAccess;
    this.directory = directory;

    this.idGenerator = new DefaultGlobIdGenerator();
    this.exceptionHandler = new ShowDialogAndExitExceptionHandler(directory);
    this.repository =
      GlobRepositoryBuilder.init(idGenerator, exceptionHandler)
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    repository.findOrCreate(User.KEY,
                            value(User.LICENSE_ACTIVATION_STATE, badJarVersion ? LicenseActivationState.STARTUP_CHECK_JAR_VERSION.getId() : null),
                            value(User.IS_REGISTERED_USER, registeredUser));
    repository.findOrCreate(AppVersionInformation.KEY,
                            value(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, Application.JAR_VERSION),
                            value(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, Application.BANK_CONFIG_VERSION),
                            value(AppVersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, Application.APPLICATION_VERSION));

    AwtExceptionHandler.setRepository(repository, directory);

    changeSetListenerToDb = new ServerChangeSetListener(dataAccess);
    this.repository.addChangeListener(changeSetListenerToDb);

    upgradeTrigger = new UpgradeTrigger(directory);
    initTriggers(dataAccess, directory, this.repository);
    repository.addTrigger(new SeriesWrapperUpdateTrigger());

    initDirectory(this.repository);

    initBank(directory);

    ColorThemeUpdater.register(repository, directory);
  }

  private void initBank(Directory directory) {
    if (!directory.get(UserConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), Application.BANK_CONFIG_VERSION, repository, directory);
    }

    SpecificBankLoader bankLoader = new SpecificBankLoader();
    bankLoader.load(repository, directory);
  }

  public static void initTriggers(DataAccess dataAccess, Directory directory, final GlobRepository repository) {
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new MonthTrigger());
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
    repository.addTrigger(new SavingsUpdateSeriesMirrorTrigger());
    repository.addTrigger(new SavingsDateActiveBudgetTrigger());
    repository.addTrigger(new UpdateActiveBudgetTrigger());
    repository.addTrigger(new ConfigUpgradeTrigger(directory));
    repository.addTrigger(new SeriesRenameTrigger());
    repository.addTrigger(new AccountGraphTrigger());
    repository.addTrigger(new AccountDeletionTrigger());
    repository.addTrigger(new RealAccountTrigger());
    repository.addTrigger(new AccountSequenceTrigger());
    repository.addTrigger(new SeriesDeletionTrigger());
    repository.addTrigger(new SubSeriesDeletionTrigger());
    repository.addTrigger(new LicenseActivationTrigger(directory));
    repository.addTrigger(new LicenseRegistrationTrigger(dataAccess));
    repository.addTrigger(new AddOnTrigger());
    repository.addTrigger(new DayTrigger());
    repository.addTrigger(new DeferredAccountTrigger());
    repository.addTrigger(new DeferredCardDayTrigger());
    repository.addTrigger(new DeferredOperationTrigger());
    repository.addTrigger(new DeferredDayChangeTrigger());
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new IrregularSeriesBudgetCreationTrigger());
    repository.addTrigger(new ActualSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new SeriesShapeTrigger());
    repository.addTrigger(new PlannedTransactionCreationTrigger());
    repository.addTrigger(new UpdateAccountOnTransactionDelete());
    repository.addTrigger(new PositionTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());
    repository.addTrigger(new SeriesStatSummaryTrigger());
    repository.addTrigger(new BudgetStatTrigger());
    repository.addTrigger(new AccountWeatherTrigger());
    repository.addTrigger(new SavingsBudgetStatTrigger());
    repository.addTrigger(new SubSeriesStatTrigger());
    repository.addTrigger(new SeriesStatToProjectStatTrigger());
    repository.addTrigger(new SeriesStatToProjectItemStatTrigger());
    repository.addTrigger(new ProjectCategorizationWarningTrigger());
    repository.addTrigger(new SeriesStatForGroupsTrigger());
    repository.addTrigger(new DateFormatTrigger(directory));
    repository.addTrigger(new ProjectPicturesDelectionTrigger());
    repository.addTrigger(new ProjectAccountGraphTrigger());
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

      userData = dataAccess.getUserData(changeSet, new DataAccess.IdUpdater() {
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
            userData = dataAccess.getUserData(changeSet, new DataAccess.IdUpdater() {
              public void update(IntegerField field, Integer lastAllocatedId) {
                idGenerator.update(field, lastAllocatedId);
              }
            });
            userData.addAll(additionalGlobToInsert);
            repository.reset(userData, typesToReplace);
            DataCheckingService dataChecker = new DataCheckingService(repository, directory);
            dataChecker.check(e);

            userData = dataAccess.getUserData(changeSet, new DataAccess.IdUpdater() {
              public void update(IntegerField field, Integer lastAllocatedId) {
                idGenerator.update(field, lastAllocatedId);
              }
            });
            PicsouInit.this.repository.reset(GlobList.EMPTY, typesToReplace);
            PicsouInit.this.repository.reset(userData, typesToReplace);
          }
        }

        UserPreferences.initMobilePassword(repository, false);

        dataAccess.applyChanges(changeSet, repository);
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
    directory.add(new BackupService(dataAccess, directory, repository, idGenerator, upgradeTrigger));
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
                              value(UserVersionInformation.CURRENT_JAR_VERSION, Application.JAR_VERSION),
                              value(UserVersionInformation.CURRENT_BANK_CONFIG_VERSION, Application.BANK_CONFIG_VERSION),
                              value(UserVersionInformation.CURRENT_SOFTWARE_VERSION, Application.APPLICATION_VERSION));

      LayoutConfig.find(FrameSize.init(directory.get(JFrame.class)), repository, true);

      repository.findOrCreate(CurrentMonth.KEY,
                              value(CurrentMonth.LAST_TRANSACTION_DAY, 0),
                              value(CurrentMonth.LAST_TRANSACTION_MONTH, 0),
                              value(CurrentMonth.CURRENT_MONTH, TimeService.getCurrentMonth()),
                              value(CurrentMonth.CURRENT_DAY, TimeService.getCurrentDay()));

      repository.findOrCreate(AddOns.KEY);

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
    private DataAccess dataAccess;

    public ServerChangeSetListener(DataAccess dataAccess) {
      this.dataAccess = dataAccess;
    }

    public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
      dataAccess.applyChanges(changeSet, repository);
    }
  }

  public Directory getDirectory() {
    return directory;
  }

  public GlobRepository getRepository() {
    return repository;
  }

}
