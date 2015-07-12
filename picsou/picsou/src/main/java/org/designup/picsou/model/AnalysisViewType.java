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
  BUDGET(0),
  EVOLUTION(2),
  TABLE(1);

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
    return new ReadOnlyGlob(AnalysisViewType.TYPE,
                            value(AnalysisViewType.ID, id));
  }

  public String getLabel() {
    return Lang.get("analysisViewType." + name().toLowerCase());
  }

  public int getId() {
    return id;
  }

  public static AnalysisViewType get(Glob glob) {
    return get(glob.get(ID));
  }

  public static AnalysisViewType get(GlobRepository repository) {
    Glob glob = repository.find(UserPreferences.KEY);
    if (glob == null) {
      return BUDGET;
    }
    Integer viewType = glob.get(UserPreferences.ANALYSIS_VIEW_TYPE);
    return get(viewType);
  }

  public static AnalysisViewType get(Integer viewType) {
    switch (viewType) {
      case 0:
        return BUDGET;
      case 1:
        return TABLE;
      case 2:
        return EVOLUTION;
      default:
        throw new InvalidParameter("Unknown value: " + viewType);
    }
  }
}

