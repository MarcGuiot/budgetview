package org.designup.picsou.model;

import org.designup.picsou.utils.Lang;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum AnalysisViewType implements GlobConstantContainer {
  CHARTS(0),
  TABLE(1),
  BOTH(2);

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private final int id;

  AnalysisViewType(int id) {
    this.id = id;
  }

  static {
    GlobTypeLoader.init(AnalysisViewType.class, "analysisViewType");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ProfileType.TYPE,
                            value(ProfileType.ID, id));
  }

  public String getLabel() {
    return Lang.get("seriesAnalysis.view." + name().toLowerCase());
  }

  public int getId() {
    return id;
  }

  public static AnalysisViewType get(GlobRepository repository) {
    Glob glob = repository.find(UserPreferences.KEY);
    if (glob == null) {
      return CHARTS;
    }
    Integer viewType = glob.get(UserPreferences.ANALYSIS_VIEW_TYPE);
    switch (viewType) {
      case 0:
        return CHARTS;
      case 1:
        return TABLE;
      case 2:
        return BOTH;
      default:
        throw new InvalidParameter("Unknown value: " + viewType);
    }
  }
}

