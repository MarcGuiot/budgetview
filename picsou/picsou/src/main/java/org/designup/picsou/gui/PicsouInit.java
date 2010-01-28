package org.designup.picsou.gui;

import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.gui.browsing.BrowsingService;
import org.designup.picsou.gui.components.dialogs.MessageAndDetailsDialog;
import org.designup.picsou.gui.config.ConfigService;
import org.designup.picsou.gui.config.RegistrationTrigger;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.series.view.SeriesWrapperUpdateTrigger;
import org.designup.picsou.gui.startup.BackupService;
import org.designup.picsou.gui.upgrade.UpgradeTrigger;
import org.designup.picsou.gui.utils.DataCheckerAction;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.AppVersionInformation;
import org.designup.picsou.model.User;
import org.designup.picsou.triggers.*;
import org.designup.picsou.utils.Lang;
import org.designup.picsou.bank.SpecificBankLoader;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.utils.Log;
import org.globsframework.utils.Strings;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;
import picsou.AwtExceptionHandler;

import javax.swing.*;
import java.util.Collection;

public class PicsouInit {

  private GlobRepository repository;
  private ServerAccess serverAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;
  private boolean firstReset = true;
  private ServerChangeSetListener changeSetListenerToDb;

  public static PicsouInit init(ServerAccess serverAccess, Directory directory, boolean registeredUser) {
    return new PicsouInit(serverAccess, directory, registeredUser);
  }

  private PicsouInit(ServerAccess serverAccess, final Directory directory, boolean registeredUser) {
    this.serverAccess = serverAccess;
    this.directory = directory;

    idGenerator = new DefaultGlobIdGenerator();
    this.repository =
      GlobRepositoryBuilder.init(idGenerator, new ShowDialogAndExitExceptionHandler())
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    repository.findOrCreate(User.KEY,
                            value(User.IS_REGISTERED_USER, registeredUser));
    repository.findOrCreate(AppVersionInformation.KEY,
                            value(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(AppVersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));

    AwtExceptionHandler.setRepository(repository, directory);

    changeSetListenerToDb = new ServerChangeSetListener(serverAccess);
    this.repository.addChangeListener(changeSetListenerToDb);

    upgradeTrigger = new UpgradeTrigger(directory);
    initTriggerRepository(serverAccess, directory, this.repository);
    repository.addTrigger(new SeriesWrapperUpdateTrigger());

    initDirectory(this.repository);

    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION, repository, directory);
    }
    SpecificBankLoader loader = new SpecificBankLoader();
    loader.load(repository, directory);
  }

  public static void initTriggerRepository(ServerAccess serverAccess, Directory directory, final GlobRepository repository) {
    repository.addTrigger(new CurrentMonthTrigger());
    repository.addTrigger(new SeriesRenameTrigger());
    repository.addTrigger(new AccountDeleteTrigger());
    repository.addTrigger(new SeriesDeletionTrigger());
    repository.addTrigger(new RegistrationTrigger(directory));
    repository.addTrigger(new RegisterLicenseTrigger(serverAccess));
    repository.addTrigger(new MonthTrigger(directory));
    repository.addTrigger(new DeferredAccountTrigger());
    repository.addTrigger(new DeferredCardDayTrigger());
    repository.addTrigger(new DeferredOperationTrigger());
    repository.addTrigger(new DeferredDayChangeTrigger());
    repository.addTrigger(new MonthsToSeriesBudgetTrigger(directory));
    repository.addTrigger(new IrregularSeriesBudgetCreationTrigger());
    repository.addTrigger(new NotImportedTransactionAccountTrigger());
    repository.addTrigger(new ObservedSeriesStatTrigger());
    repository.addTrigger(new PastTransactionUpdateSeriesBudgetTrigger());
    repository.addTrigger(new TransactionPlannedTrigger());
    repository.addTrigger(new ImportedToNotImportedAccountTransactionTrigger());
    repository.addTrigger(new UpdateAccountOnTransactionDelete());
    repository.addTrigger(new PositionTrigger());
    repository.addTrigger(new PlannedSeriesStatTrigger());
    repository.addTrigger(new SeriesStatSummaryTrigger());
    repository.addTrigger(new BudgetStatTrigger());
    repository.addTrigger(new SavingsBudgetStatTrigger());
  }

  public PreLoadData loadUserData(String user, boolean useDemoAccount, boolean autoLogin) {
    return new PreLoadData(user, useDemoAccount, autoLogin);
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
      Collection<GlobType> globTypeCollection = PicsouGuiModel.getUserSpecificType();
      typesToReplace = globTypeCollection.toArray(new GlobType[globTypeCollection.size()]);
      idGenerator.reset(globTypeCollection);

      userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
        public void update(IntegerField field, Integer lastAllocatedId) {
          idGenerator.update(field, lastAllocatedId);
        }
      });
    }

    public void load() {
      repository.reset(GlobList.EMPTY, typesToReplace);
      repository.addTriggerAtFirst(upgradeTrigger);

      try {
        repository.startChangeSet();
        repository.update(User.KEY,
                          value(User.NAME, user),
                          value(User.AUTO_LOGIN, autoLogin),
                          value(User.IS_DEMO_USER, useDemoAccount));
        if (!userData.isEmpty()) {
          firstReset = true;
          try {
            repository.reset(userData, typesToReplace);
          }
          catch (Exception e) {
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
            repository.reset(userData, typesToReplace);
            DataCheckerAction action = new DataCheckerAction(repository, directory);
            action.check();
            firstReset = false;

            userData = serverAccess.getUserData(changeSet, new ServerAccess.IdUpdater() {
              public void update(IntegerField field, Integer lastAllocatedId) {
                idGenerator.update(field, lastAllocatedId);
              }
            });
            PicsouInit.this.repository.reset(GlobList.EMPTY, typesToReplace);
            PicsouInit.this.repository.reset(userData, typesToReplace);
          }
        }
        else {
          upgradeTrigger.createDataForNewUser(repository);
        }
        serverAccess.applyChanges(changeSet, repository);
      }
      catch (Exception e) {
        throw new InvalidData(Lang.get("login.data.load.fail"), e);
      }
      finally {
        repository.completeChangeSet();
        repository.removeTrigger(upgradeTrigger);
      }
    }
  }

  private void initDirectory(GlobRepository repository) {
    directory.add(BrowsingService.class, BrowsingService.createService());

    TransactionAnalyzerFactory factory = new TransactionAnalyzerFactory(PicsouGuiModel.get());
    directory.add(TransactionAnalyzerFactory.class, factory);
    ImportService importService = new ImportService();
    directory.add(ImportService.class, importService);

    directory.add(new BackupService(serverAccess, directory, repository, idGenerator, upgradeTrigger));
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

  private class ShowDialogAndExitExceptionHandler implements ExceptionHandler {

    public void onException(Throwable ex) {
      Log.write(ex.getMessage(), ex);
      if (!firstReset || !PicsouApplication.EXIT_ON_DATA_ERROR) {
        MessageAndDetailsDialog dialog = new MessageAndDetailsDialog("exception.title",
                                                                     "exception.content",
                                                                     Strings.toString(ex),
                                                                     directory.get(JFrame.class),
                                                                     directory);
        dialog.show();
        if (PicsouApplication.EXIT_ON_DATA_ERROR) {
          System.exit(10);
        }
      }
    }
  }
}
