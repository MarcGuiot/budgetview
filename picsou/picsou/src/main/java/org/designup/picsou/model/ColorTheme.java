package org.designup.picsou.model;

import org.globsframework.gui.splits.color.ColorService;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.GlobRepository;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.directory.Directory;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum ColorTheme implements GlobConstantContainer {

  STANDARD(1, "colors/color.properties", "standard.png"),
  CLASSIC_BLUE(2, "colors/color_classic_blue.properties", "classic_blue.png");

  private final int id;
  private final String filePath;
  private final String imagePath;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private ColorTheme(int id, String filePath, String imagePath) {
    this.id = id;
    this.filePath = filePath;
    this.imagePath = imagePath;
  }

  static {
    GlobTypeLoader.init(ColorTheme.class, "colorTheme");
  }

  public ReadOnlyGlob getGlob() {
    return new ReadOnlyGlob(ColorTheme.TYPE,
                            value(ColorTheme.ID, id));
  }

  public int getId() {
    return id;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getImagePath() {
    return "color_themes/" + imagePath;
  }

  public static ColorTheme get(Glob glob) {
    Integer id = glob.get(ColorTheme.ID);
    return get(id);
  }

  public static ColorTheme get(Integer id) {
    switch (id) {
      case 1:
        return STANDARD;
      case 2:
        return CLASSIC_BLUE;
    }
    throw new InvalidParameter("Unexpected value: " + id);
  }
}
