package com.budgetview.server.cloud.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.DefaultBoolean;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.NoObfuscation;
import org.globsframework.metamodel.fields.BooleanField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.StringField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.utils.Utils;

public class CloudBudgetArea {
  public static GlobType TYPE;

  @Key
  @NoObfuscation
  public static IntegerField ID;

  @NoObfuscation
  public static StringField LABEL;

  @NoObfuscation @DefaultBoolean(false)
  public static BooleanField INVERT_AMOUNTS;

  static {
    GlobTypeLoader.init(CloudBudgetArea.class, "budgetAreaEntity");
  }

  public static boolean isUncategorized(Glob budgetAreaEntity) {
    return isUncategorized(budgetAreaEntity.get(CloudBudgetArea.ID));
  }

  public static boolean isUncategorized(Integer budgetAreaEntityId) {
    return Utils.equal(6, budgetAreaEntityId);
  }
}
