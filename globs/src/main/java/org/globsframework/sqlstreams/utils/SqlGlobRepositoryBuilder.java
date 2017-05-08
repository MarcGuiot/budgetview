package org.globsframework.sqlstreams.utils;

import org.globsframework.metamodel.Field;
import org.globsframework.metamodel.GlobType;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.repository.DefaultGlobRepository;
import org.globsframework.model.repository.GlobIdGenerator;
import org.globsframework.sqlstreams.GlobsDatabase;
import org.globsframework.sqlstreams.SqlSelectBuilder;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.constraints.Constraint;
import org.globsframework.utils.Log;
import org.globsframework.utils.collections.T3uples;
import org.globsframework.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.List;

public class SqlGlobRepositoryBuilder {
  private GlobsDatabase globsDB;
  private List<T3uples<Constraint, GlobType, Field[]>> requestToRetrieve =
    new ArrayList<T3uples<Constraint, GlobType, Field[]>>();

  private SqlGlobRepositoryBuilder(GlobsDatabase globsDB) {
    this.globsDB = globsDB;
  }

  public void add(Constraint constraint, Field... fields) {
    if (fields.length <= 0) {
      Log.write("[Globs] No field added to SqlGlobRepositoryBuilder");
      return;
    }
    T3uples<Constraint, GlobType, Field[]> t3uples =
      new T3uples<Constraint, GlobType, Field[]>(constraint, fields[0].getGlobType(), fields);
    GlobType globType = null;
    for (Field field : fields) {
      if (globType != null && !field.getGlobType().equals(globType)) {
        throw new InvalidParameter("All fields should be owned by the same GlobType");
      }
      globType = field.getGlobType();
    }
    requestToRetrieve.add(t3uples);
  }

  static SqlGlobRepositoryBuilder init(GlobsDatabase globsDB) {
    return new SqlGlobRepositoryBuilder(globsDB);
  }

  public GlobRepository getGlobRepository() {
    DefaultGlobRepository repository = new DefaultGlobRepository();
    init(repository);
    return repository;
  }

  public GlobRepository getGlobRepository(GlobIdGenerator idGenerator) {
    DefaultGlobRepository repository = new DefaultGlobRepository(idGenerator);
    init(repository);
    return repository;
  }

  private void init(DefaultGlobRepository repository) {
    SqlConnection sqlConnection = globsDB.connect();
    for (T3uples<Constraint, GlobType, Field[]> t3uples : requestToRetrieve) {
      SqlSelectBuilder builder = sqlConnection.startSelect(t3uples.getSecond(), t3uples.getFirst());
      for (Field field : t3uples.getThird()) {
        builder.select(field);
      }
      repository.add(builder.getQuery().getList());
    }
  }
}
