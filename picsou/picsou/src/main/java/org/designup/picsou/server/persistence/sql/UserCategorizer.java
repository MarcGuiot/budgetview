package org.designup.picsou.server.persistence.sql;

import org.designup.picsou.server.model.UserCategoryAssociation;
import org.designup.picsou.server.session.Persistence;
import org.globsframework.sqlstreams.SelectQuery;
import org.globsframework.sqlstreams.SqlConnection;
import org.globsframework.sqlstreams.SqlRequest;
import org.globsframework.sqlstreams.constraints.Constraints;
import org.globsframework.streams.GlobStream;
import org.globsframework.streams.accessors.IntegerAccessor;
import org.globsframework.streams.accessors.StringAccessor;
import org.globsframework.streams.accessors.utils.ValueIntegerAccessor;
import org.globsframework.streams.accessors.utils.ValueLongAccessor;
import org.globsframework.streams.accessors.utils.ValueStringAccessor;
import org.globsframework.utils.MultiMap;
import org.globsframework.utils.Pair;
import org.globsframework.utils.Ref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class UserCategorizer {
  private Retriever retriever;
  private Creator creator;
  private CountAndIDGetter countAndIDGetter;
  private Updater updater;
  private SqlConnection connection;

  public UserCategorizer(SqlConnection connection) {
    this.connection = connection;
    retriever = new Retriever(connection);
    creator = new Creator(connection);
    countAndIDGetter = new CountAndIDGetter(connection);
    updater = new Updater(connection);
  }

  public void register(long userId, String label, Integer categoryId) {
    CountAndIDGetter.IdAndCount idAndCount = countAndIDGetter.getId(userId, label, categoryId);
    if (idAndCount != null) {
      updater.update(idAndCount.getFirst(), idAndCount.getSecond() + 1);
    }
    else {
      creator.create(userId, label, categoryId);
    }
    connection.commit();
  }

  public List<Persistence.CategoryInfo> getCategories(long userId, List<String> labels) {
    MultiMap<String, Retriever.CategoryAndCount> category = retriever.getAssociatedCategory(userId);
    List<Persistence.CategoryInfo> results = new ArrayList<Persistence.CategoryInfo>(labels.size());
    for (String label : labels) {
      List<Retriever.CategoryAndCount> categoryAndCounts = new ArrayList<Retriever.CategoryAndCount>(category.get(label));
      Collections.sort(categoryAndCounts, new Comparator<Retriever.CategoryAndCount>() {
        public int compare(Retriever.CategoryAndCount o1, Retriever.CategoryAndCount o2) {
          return o2.getSecond().compareTo(o1.getSecond());
        }
      });
      Persistence.CategoryInfo info = new Persistence.CategoryInfo();
      for (Retriever.CategoryAndCount categoryAndCount : categoryAndCounts) {
        info.add(categoryAndCount.getFirst());
      }
      results.add(info);
    }
    connection.commit();
    return results;
  }

  static class Creator {
    private SqlRequest createRequest;
    private ValueStringAccessor infoForcreate;
    private ValueLongAccessor userIdForCreate;
    private ValueIntegerAccessor createCategoryId;

    public Creator(SqlConnection sqlConnection) {
      createCategoryId = new ValueIntegerAccessor();
      infoForcreate = new ValueStringAccessor();
      userIdForCreate = new ValueLongAccessor();
      ValueIntegerAccessor countForCreate = new ValueIntegerAccessor(1);
      createRequest = sqlConnection.getCreateBuilder(UserCategoryAssociation.TYPE)
        .set(UserCategoryAssociation.CATEGORY_ID, createCategoryId)
        .set(UserCategoryAssociation.INFO, infoForcreate)
        .set(UserCategoryAssociation.USER_ID, userIdForCreate)
        .set(UserCategoryAssociation.COUNT, countForCreate)
        .getRequest();
    }

    public void create(Long userId, String info, Integer categorie) {
      infoForcreate.setValue(info);
      userIdForCreate.setValue(userId);
      createCategoryId.setValue(categorie);
      createRequest.run();
    }
  }

  static class Updater {
    private SqlRequest updateRequest;
    private ValueIntegerAccessor count;
    protected ValueIntegerAccessor infoId;

    public Updater(SqlConnection sqlConnection) {
      count = new ValueIntegerAccessor();
      infoId = new ValueIntegerAccessor();
      updateRequest =
        sqlConnection.getUpdateBuilder(UserCategoryAssociation.TYPE,
                                       Constraints.equal(UserCategoryAssociation.ID, infoId))
          .update(UserCategoryAssociation.COUNT, count)
          .getRequest();
    }

    public void update(Integer infoId, Integer count) {
      this.count.setValue(count);
      this.infoId.setValue(infoId);
      updateRequest.run();
    }
  }

  static class CountAndIDGetter {
    private ValueIntegerAccessor categoryIdForGet;
    private ValueLongAccessor userIdForGet;
    private ValueStringAccessor infoForGet;
    private SelectQuery getQuery;
    private IntegerAccessor countGet;
    private IntegerAccessor idGet;

    public CountAndIDGetter(SqlConnection sqlConnection) {
      categoryIdForGet = new ValueIntegerAccessor();
      userIdForGet = new ValueLongAccessor();
      infoForGet = new ValueStringAccessor();
      Ref<IntegerAccessor> countGet = new Ref<IntegerAccessor>();
      Ref<IntegerAccessor> idGet = new Ref<IntegerAccessor>();
      getQuery = sqlConnection.getQueryBuilder(UserCategoryAssociation.TYPE,
                                               Constraints.and(Constraints.equal(UserCategoryAssociation.CATEGORY_ID, categoryIdForGet),
                                                               Constraints.equal(UserCategoryAssociation.USER_ID, userIdForGet),
                                                               Constraints.equal(UserCategoryAssociation.INFO, infoForGet)))
        .select(UserCategoryAssociation.COUNT, countGet)
        .select(UserCategoryAssociation.ID, idGet)
        .getQuery();
      this.idGet = idGet.get();
      this.countGet = countGet.get();
    }

    static class IdAndCount extends Pair<Integer, Integer> {
      public IdAndCount(Integer first, Integer second) {
        super(first, second);
      }
    }

    public IdAndCount getId(Long userId, String info, Integer categorie) {
      userIdForGet.setValue(userId);
      infoForGet.setValue(info);
      categoryIdForGet.setValue(categorie);
      GlobStream globStream = getQuery.execute();
      if (globStream.next()) {
        return new IdAndCount(idGet.getInteger(), countGet.getInteger());
      }
      return null;
    }
  }

  static class Retriever {
    private ValueLongAccessor userId;
    private SelectQuery query;
    private IntegerAccessor categoryAccessor;
    protected StringAccessor infoAccessor;
    protected IntegerAccessor countAccessor;

    public Retriever(SqlConnection sqlConnection) {
      userId = new ValueLongAccessor();
      Ref<IntegerAccessor> categoryAccessorRef = new Ref<IntegerAccessor>();
      Ref<StringAccessor> infoAccessorRef = new Ref<StringAccessor>();
      Ref<IntegerAccessor> countRef = new Ref<IntegerAccessor>();
      query = sqlConnection.getQueryBuilder(UserCategoryAssociation.TYPE,
                                            Constraints.equal(UserCategoryAssociation.USER_ID, userId))
        .select(UserCategoryAssociation.CATEGORY_ID, categoryAccessorRef)
        .select(UserCategoryAssociation.INFO, infoAccessorRef)
        .select(UserCategoryAssociation.COUNT, countRef)
        .getQuery();
      categoryAccessor = categoryAccessorRef.get();
      infoAccessor = infoAccessorRef.get();
      countAccessor = countRef.get();
    }

    static class CategoryAndCount extends Pair<Integer, Integer> {

      public CategoryAndCount(Integer first, Integer second) {
        super(first, second);
      }
    }

    public MultiMap<String, CategoryAndCount> getAssociatedCategory(long userId) {
      MultiMap<String, CategoryAndCount> categories = new MultiMap<String, CategoryAndCount>();
      this.userId.setValue(userId);
      GlobStream globStream = query.execute();
      while (globStream.next()) {
        categories.put(infoAccessor.getString(), new CategoryAndCount(categoryAccessor.getInteger(),
                                                                      countAccessor.getInteger()));
      }
      return categories;
    }
  }
}
