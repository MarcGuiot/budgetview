package org.crossbowlabs.globs.sqlstreams;

import org.crossbowlabs.globs.metamodel.Field;
import org.crossbowlabs.globs.metamodel.GlobType;
import org.crossbowlabs.globs.model.GlobRepository;
import org.crossbowlabs.globs.model.impl.DefaultGlobRepository;
import org.crossbowlabs.globs.model.utils.GlobIdGenerator;
import org.crossbowlabs.globs.sqlstreams.constraints.Constraint;
import org.crossbowlabs.globs.utils.Log;
import org.crossbowlabs.globs.utils.T3uples;
import org.crossbowlabs.globs.utils.exceptions.InvalidParameter;

import java.util.ArrayList;
import java.util.List;

public class SqlGlobRepositoryBuilder {
  private SqlService sqlService;
  private List<T3uples<Constraint, GlobType, Field[]>> requestToRetrieve =
    new ArrayList<T3uples<Constraint, GlobType, Field[]>>();

  private SqlGlobRepositoryBuilder(SqlService sqlService) {
    this.sqlService = sqlService;
  }

  public void add(Constraint constraint, Field... fields) {
    if (fields.length <= 0) {
      Log.write("No field added to SqlGlobRepositoryBuilder");
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

  static SqlGlobRepositoryBuilder init(SqlService sqlService) {
    return new SqlGlobRepositoryBuilder(sqlService);
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
    SqlConnection sqlConnection = sqlService.getDb();
    for (T3uples<Constraint, GlobType, Field[]> t3uples : requestToRetrieve) {
      SelectBuilder builder = sqlConnection.getQueryBuilder(t3uples.getSecond(), t3uples.getFirst());
      for (Field field : t3uples.getThird()) {
        builder.select(field);
      }
      repository.add(builder.getQuery().executeAsGlobs());
    }
  }
}
