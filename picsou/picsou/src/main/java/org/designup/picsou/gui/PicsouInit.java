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
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.*;
import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobModel;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.model.ChangeSet;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.GlobList;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.GlobRepositoryBuilder;
import org.globsframework.model.delta.DefaultChangeSet;
import org.globsframework.model.delta.MutableChangeSet;
import org.globsframework.model.impl.DefaultGlobIdGenerator;
import org.globsframework.model.utils.DefaultChangeSetListener;
import org.globsframework.model.utils.GlobUtils;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PicsouInit {

  private GlobRepository repository;
  private ServerAccess serverAccess;
  private Directory directory;
  private DefaultGlobIdGenerator idGenerator;
  private UpgradeTrigger upgradeTrigger;

  public static PicsouInit init(ServerAccess serverAccess, Directory directory, boolean registeredUser) {
    return new PicsouInit(serverAccess, directory, registeredUser);
  }

  private PicsouInit(ServerAccess serverAccess, final Directory directory, boolean registeredUser) {
    this.serverAccess = serverAccess;
    this.directory = directory;

    idGenerator = new DefaultGlobIdGenerator();
    this.repository =
      GlobRepositoryBuilder.init(idGenerator)
        .add(directory.get(GlobModel.class).getConstants())
        .get();

    repository.findOrCreate(User.KEY,
                            value(User.IS_REGISTERED_USER, registeredUser));
    repository.findOrCreate(AppVersionInformation.KEY,
                            value(AppVersionInformation.LATEST_AVALAIBLE_JAR_VERSION, PicsouApplication.JAR_VERSION),
                            value(AppVersionInformation.LATEST_BANK_CONFIG_SOFTWARE_VERSION, PicsouApplication.BANK_CONFIG_VERSION),
                            value(AppVersionInformation.LATEST_AVALAIBLE_SOFTWARE_VERSION, PicsouApplication.APPLICATION_VERSION));

    ExceptionHandler.setRepository(repository);

    this.repository.addChangeListener(new ServerChangeSetListener(serverAccess));

    upgradeTrigger = new UpgradeTrigger(directory);
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

    if (!directory.get(ConfigService.class).loadConfigFileFromLastestJar(directory, this.repository)) {
      directory.get(TransactionAnalyzerFactory.class)
        .load(this.getClass().getClassLoader(), PicsouApplication.BANK_CONFIG_VERSION, repository);
    }
  }

  public PreLoadData loadUserData(String user, boolean useDemoAccount, boolean registeredUser) {
    return new PreLoadData(user, useDemoAccount, registeredUser);
  }

  class PreLoadData {
    MutableChangeSet changeSet;
    GlobList userData;
    private String user;
    private boolean useDemoAccount;
    private boolean registeredUser;
    private GlobType[] typesToReplace;

    PreLoadData(String user, boolean useDemoAccount, boolean registeredUser) {
      this.user = user;
      this.useDemoAccount = useDemoAccount;
      this.registeredUser = registeredUser;
      changeSet = new DefaultChangeSet();
      GlobModel model = directory.get(GlobModel.class);
      Collection<GlobType> globTypeCollection = new ArrayList<GlobType>(model.getAll());
      Set<GlobType> typeNotToRemove = model.getConstants().getTypes();
      typeNotToRemove.addAll(Arrays.asList(PreTransactionTypeMatcher.TYPE, Bank.TYPE, BankEntity.TYPE,
                                           User.TYPE, AppVersionInformation.TYPE));

      globTypeCollection.removeAll(typeNotToRemove);
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
                          value(User.IS_DEMO_USER, useDemoAccount));
        if (!userData.isEmpty()) {
          repository.reset(userData, GlobUtils.toArray(userData.getTypes()));
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
      }
      repository.removeTrigger(upgradeTrigger);
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

}
