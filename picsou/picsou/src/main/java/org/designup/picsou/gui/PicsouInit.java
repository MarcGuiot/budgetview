package org.designup.picsou.gui;

import org.crossbowlabs.globs.metamodel.GlobModel;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.delta.DefaultChangeSet;
import org.crossbowlabs.globs.model.delta.MutableChangeSet;
import org.crossbowlabs.globs.model.utils.CachedGlobIdGenerator;
import org.crossbowlabs.globs.model.utils.DefaultChangeSetListener;
import static org.crossbowlabs.globs.model.utils.GlobMatchers.isNotNull;
import org.crossbowlabs.globs.utils.directory.Directory;
import org.crossbowlabs.globs.utils.exceptions.InvalidData;
import org.crossbowlabs.globs.utils.exceptions.ResourceAccessFailed;
import org.crossbowlabs.globs.utils.exceptions.UnexpectedApplicationState;
import org.crossbowlabs.globs.xml.XmlGlobParser;
import org.designup.picsou.client.AllocationLearningService;
import org.designup.picsou.client.ServerAccess;
import org.designup.picsou.client.ServerAccessGlobIdGenerator;
import org.designup.picsou.gui.model.PicsouGuiModel;
import org.designup.picsou.gui.triggers.MonthStatComputer;
import org.designup.picsou.importer.ImportService;
import org.designup.picsou.importer.analyzer.TransactionAnalyzerFactory;
import org.designup.picsou.model.*;
import org.designup.picsou.triggers.SummaryAccountCreationTrigger;
import org.designup.picsou.utils.Lang;

import java.io.*;

public class PicsouInit {

  private GlobRepository repository;
  private Directory directory;

  public static PicsouInit init(ServerAccess serverAccess, String user, boolean newUser, Directory directory) throws IOException {
    return new PicsouInit(serverAccess, user, newUser, directory);
  }

  private PicsouInit(ServerAccess serverAccess, String user, boolean newUser, Directory directory) throws IOException {
    this.directory = directory;

    ServerAccessGlobIdGenerator generator = new ServerAccessGlobIdGenerator(serverAccess);
    repository =
      GlobRepositoryBuilder.init(new CachedGlobIdGenerator(generator))
        .add(directory.get(GlobModel.class).getConstants())
        .get();
    generator.set(repository);


    repository.addTrigger(new SummaryAccountCreationTrigger());

    repository.addChangeListener(new ServerChangeSetListener(serverAccess));

    repository.addTrigger(new MonthStatComputer(repository));

    repository.create(User.TYPE,
                      value(User.ID, User.SINGLETON_ID),
                      value(User.NAME, user));

    MutableChangeSet changeSet = new DefaultChangeSet();
    try {
      GlobList userData = serverAccess.getUserData(changeSet);
      if (newUser) {
        userData.addAll(loadDefaultSubcategories());
      }
      repository.reset(userData, Transaction.TYPE, Account.TYPE, Bank.TYPE, BankEntity.TYPE,
                       TransactionToCategory.TYPE, LabelToCategory.TYPE, Category.TYPE);
    }
    catch (Exception e) {
      throw new InvalidData(Lang.get("login.data.load.fail"), e);
    }
    serverAccess.applyChanges(changeSet, repository);

    initDirectory(repository);
  }

  private void initDirectory(GlobRepository repository) {
    AllocationLearningService learningService = new AllocationLearningService();
    directory.add(AllocationLearningService.class, learningService);

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

  private GlobList loadDefaultSubcategories() {
    String subcatsFile = "/subcats.xml";
    Reader reader = null;
    try {
      InputStream stream = PicsouInit.class.getResourceAsStream(subcatsFile);
      if (stream == null) {
        throw new ResourceAccessFailed("Resource file not found:" + subcatsFile);
      }
      reader = new InputStreamReader(stream, "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new UnexpectedApplicationState(e);
    }
    XmlGlobParser.parse(PicsouModel.get(), repository, reader, "globs");
    GlobList result = new GlobList();
    for (Glob subcat : repository.getAll(Category.TYPE, isNotNull(Category.MASTER))) {
      result.add(subcat.duplicate());
    }
    return result;
  }
}
