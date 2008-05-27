package org.designup.picsou.server.persistence.sql;

import org.crossbowlabs.globs.utils.TestUtils;
import org.designup.picsou.server.model.DbTestCase;
import org.designup.picsou.server.session.Persistence;

import java.util.Arrays;
import java.util.List;

public class UserCategorizerTest extends DbTestCase {

  Persistence.CategoryInfo c(int... val) {
    Persistence.CategoryInfo info = new Persistence.CategoryInfo();
    for (int i : val) {
      info.add(i);
    }
    return info;
  }

  public void testRegister() throws Exception {
    UserCategorizer categorizer = new UserCategorizer(getConnection());
    categorizer.register(-1L, "some info", 1);
    categorizer.register(-1L, "some other info", 2);
    List<Persistence.CategoryInfo> category =
      categorizer.getCategories(-1, Arrays.asList("some info", "unknown info", "some other info"));
    TestUtils.assertEquals(category, c(1), c(), c(2));
  }

  public void testConflict() throws Exception {
    UserCategorizer categorizer = new UserCategorizer(getConnection());
    categorizer.register(-1L, "some conflict", 2);
    categorizer.register(-1L, "some conflict", 1);
    categorizer.register(-1L, "some conflict", 2);
    TestUtils.assertEquals(categorizer.getCategories(-1, Arrays.asList("some conflict")),
                           c(2, 1));
    categorizer.register(-1L, "some conflict", 1);
    TestUtils.assertEquals(categorizer.getCategories(-1, Arrays.asList("some conflict")),
                           c(2, 1));
    categorizer.register(-1L, "some conflict", 1);
    categorizer.register(-1L, "some conflict", 1);
    TestUtils.assertEquals(categorizer.getCategories(-1, Arrays.asList("some conflict")),
                           c(1, 2));
  }

  public void testUserInfoAreIsolated() throws Exception {
    UserCategorizer categorizer = new UserCategorizer(getConnection());
    categorizer.register(1L, "some info", 2);
    categorizer.register(2L, "some info", 1);
    TestUtils.assertEquals(categorizer.getCategories(1, Arrays.asList("some info")),
                           c(2));
    TestUtils.assertEquals(categorizer.getCategories(2, Arrays.asList("some info")),
                           c(1));
  }
}
