package org.crossbowlabs.webdemo.model;

import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.*;
import org.crossbowlabs.globs.sqlstreams.InDbChangeSetVisitor;
import org.crossbowlabs.globs.sqlstreams.SqlConnection;
import org.crossbowlabs.globs.sqlstreams.SqlService;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.webdemo.WebDemoApplication;
import wicket.Application;

import java.util.List;

public class RepositoryLoader implements GlobRepositoryLoader {
  private GlobList globs = new GlobList();
  private transient GlobRepository repository;

  public RepositoryLoader() {
    SqlService sqlService = ((WebDemoApplication) Application.get()).getDirectory().get(SqlService.class);
    SqlConnection sqlConnection = sqlService.getDb();
    globs = sqlConnection.getQueryBuilder(Person.TYPE).selectAll().getQuery().executeAsGlobs();
    sqlConnection.commitAndClose();
  }

  public GlobRepository getRepository() {
    if (repository == null) {
      repository = GlobRepositoryBuilder.init().add(globs).get();
      repository.addChangeListener(new ChangeSetListener() {
        public void globsChanged(ChangeSet changeSet, GlobRepository repository) {
          try {
            WebDemoApplication application = (WebDemoApplication) Application.get();
            SqlConnection sqlConnection = application.getDirectory().get(SqlService.class).getDb();
            changeSet.visit(new InDbChangeSetVisitor(sqlConnection));
            sqlConnection.commitAndClose();
          }
          catch (Exception e) {
          }
        }

        public void globsReset(GlobRepository repository, List<GlobType> changedTypes) {
        }
      });
    }
    return repository;
  }

  public void detach() {
    if (repository != null) {
      globs = repository.getAll();
    }
    repository = null;
  }
}
