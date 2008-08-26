package org.crossbowlabs.webdemo.model;

import org.apache.wicket.Application;
import org.crossbowlabs.webdemo.WebDemoApplication;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.*;
import org.globsframework.sqlstreams.InDbChangeSetVisitor;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlService;
import org.globsframework.wicket.GlobRepositoryLoader;

import java.util.List;
import java.util.Set;

public class RepositoryLoader implements GlobRepositoryLoader {
  private GlobList globs = new GlobList();
  private transient GlobRepository repository;

  public RepositoryLoader() {
    SqlService sqlService = ((WebDemoApplication)Application.get()).getDirectory().get(SqlService.class);
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
            WebDemoApplication application = (WebDemoApplication)Application.get();
            SqlConnection sqlConnection = application.getDirectory().get(SqlService.class).getDb();
            changeSet.visit(new InDbChangeSetVisitor(sqlConnection));
            sqlConnection.commitAndClose();
          }
          catch (Exception e) {
          }
        }

        public void globsReset(GlobRepository repository, Set<GlobType> changedTypes) {
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
