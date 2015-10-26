package org.designup.picsou.license.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Target;
import org.globsframework.metamodel.fields.*;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.sqlstreams.annotations.AutoIncrement;

public class RepoInfo {
  public static GlobType TYPE;

  @Key
  @AutoIncrement
  public static IntegerField ID;

  public static StringField REPO_ID;

  public static TimeStampField LAST_ACCESS_DATE;

  public static LongField COUNT;

  public static LongField JAR_VERSION;

  @Target(License.class)
  public static LinkField LICENSE_ID;

  static {
    GlobTypeLoader.init(RepoInfo.class);
  }

}