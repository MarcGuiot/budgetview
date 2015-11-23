package org.designup.picsou.model;

import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.Glob;
import org.globsframework.model.impl.ReadOnlyGlob;
import org.globsframework.model.utils.GlobConstantContainer;
import org.globsframework.utils.exceptions.InvalidParameter;

import static org.globsframework.model.FieldValue.value;

public enum ColorTheme implements GlobConstantContainer {

  STANDARD(1, "colors/color.properties", "themes/theme_classic.properties", "standard.png"),
  BLUE(2, "colors/color_blue.properties", "themes/theme_classic.properties", "blue.png"),
  PINK(3, "colors/color_pink.properties", "themes/theme_classic.properties", "pink.png"),
  BLACK(4, "colors/color_black.properties", "themes/theme_classic.properties", "black.png"),
  TURQUOISE(5, "colors/color_turquoise.properties", "themes/theme_classic.properties", "turquoise.png"),
  ORANGE(6, "colors/color_orange.properties", "themes/theme_classic.properties", "orange.png"),
  GREEN(7, "colors/color_green.properties", "themes/theme_classic.properties", "green.png");

  private final int id;
  private final String colorFilePath;
  private String themeFilePath;
  private final String imagePath;

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  private ColorTheme(int id, String colorFilePath, String themeFilePath, String imagePath) {
    this.id = id;
    this.colorFilePath = colorFilePath;
    this.themeFilePath = themeFilePath;
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

  public String getColorFileName() {
    return colorFilePath.substring(colorFilePath.lastIndexOf("/") + 1);
  }

  public String getColorFilePath() {
    return colorFilePath;
  }

  public String getThemeFilePath() {
    return themeFilePath;
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
        return BLUE;
      case 3:
        return PINK;
      case 4:
        return BLACK;
      case 5:
        return TURQUOISE;
      case 6:
        return ORANGE;
      case 7:
        return GREEN;
    }
    throw new InvalidParameter("Unexpected value: " + id);
  }
}
