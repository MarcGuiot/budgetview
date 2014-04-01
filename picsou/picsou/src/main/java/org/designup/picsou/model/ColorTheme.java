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
  CLASSIC_BLUE(2, "colors/color_classic_blue.properties", "themes/theme_classic.properties", "classic_blue.png"),
  BLUE(3, "colors/color_blue.properties", "themes/theme_classic.properties", "blue.png"),
  BLACK(4, "colors/color_black.properties", "themes/theme_classic.properties", "black.png"),
  PURPLE(5, "colors/color_purple.properties", "themes/theme_classic.properties", "purple.png"),
  GREEN(6, "colors/color_green.properties", "themes/theme_classic.properties", "green.png"),
  GREY_FLAT(7, "colors/color_grey_flat.properties", "themes/theme_flat.properties", "flat_grey.png"),
  BLUE_FLAT(8, "colors/color_blue_flat.properties", "themes/theme_flat.properties", "flat_blue.png");

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
        return CLASSIC_BLUE;
      case 3:
        return BLUE;
      case 4:
        return BLACK;
      case 5:
        return PURPLE;
      case 6:
        return GREEN;
      case 7:
        return GREY_FLAT;
      case 8:
        return BLUE_FLAT;
    }
    throw new InvalidParameter("Unexpected value: " + id);
  }
}
