package org.designup.picsou.model;

import com.budgetview.shared.utils.PicsouGlobSerializer;
import com.sun.image.codec.jpeg.ImageFormatException;
import org.globsframework.metamodel.GlobType;
import org.globsframework.metamodel.annotations.Key;
import org.globsframework.metamodel.annotations.Required;
import org.globsframework.metamodel.fields.BlobField;
import org.globsframework.metamodel.fields.IntegerField;
import org.globsframework.metamodel.fields.LinkField;
import org.globsframework.metamodel.utils.GlobTypeLoader;
import org.globsframework.model.*;
import org.globsframework.utils.exceptions.InvalidFormat;
import org.globsframework.utils.exceptions.OperationFailed;
import org.globsframework.utils.serialization.SerializedByteArrayOutput;
import org.globsframework.utils.serialization.SerializedInput;
import org.globsframework.utils.serialization.SerializedInputOutputFactory;
import org.globsframework.utils.serialization.SerializedOutput;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class Picture {

  public static GlobType TYPE;

  @Key
  public static IntegerField ID;

  @Required
  public static BlobField IMAGE_DATA;

  static {
    GlobTypeLoader.init(Picture.class, "picture");
  }

  public static void setIcon(org.globsframework.model.Key key, LinkField link, GlobRepository repository, Image image, Dimension maxSize) throws InvalidFormat {
    byte[] bytes = getScaledImageAsBytes(image, maxSize);

    try {
      ImageIO.write(toBufferedImage(new ImageIcon(bytes)), "png", new File("/Users/rmedina/Desktop/bv_saved_picture.jpg"));
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }

    doSetLink(key, link, repository, bytes);
  }

  public static void setIcon(org.globsframework.model.Key key, LinkField link, GlobRepository repository, String path, Dimension maxSize) throws InvalidFormat {
    byte[] bytes = getScaledImageAsBytes(path, maxSize);
    doSetLink(key, link, repository, bytes);
  }

  private static void doSetLink(org.globsframework.model.Key key, LinkField link, GlobRepository repository, byte[] bytes) {
    repository.startChangeSet();
    try {
      Glob picture = repository.create(Picture.TYPE, FieldValue.value(Picture.IMAGE_DATA, bytes));
      repository.update(key, link, picture.get(Picture.ID));
    }
    finally {
      repository.completeChangeSet();
    }
  }

  public static Icon getIcon(Glob glob, LinkField link, GlobRepository repository, Dimension maxSize) {
    if ((maxSize.width == 0) || (maxSize.height == 0)) {
      return null;
    }
    Glob picture = repository.findLinkTarget(glob, link);
    if (picture == null) {
      return null;
    }

    byte[] imageData = picture.get(IMAGE_DATA);
    if (imageData == null) {
      return null;
    }
    ImageIcon imageIcon = new ImageIcon(imageData);
    return new ImageIcon(resize(imageIcon.getImage(), maxSize));
  }

  private static byte[] getScaledImageAsBytes(String path, Dimension maxSize) throws InvalidFormat {
    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
    try {
      BufferedImage bufferedImage = ImageIO.read(new File(path));
      ImageIcon resized = new ImageIcon(resize(bufferedImage, maxSize));

      BufferedImage resizedBufferedImage = toBufferedImage(resized);

      ImageIO.write(resizedBufferedImage, "png", byteArrayOut);
    }
    catch (IOException e) {
      throw new OperationFailed(e);
    }
    catch (ImageFormatException e) {
      throw new InvalidFormat(e);
    }
    return byteArrayOut.toByteArray();
  }

  private static byte[] getScaledImageAsBytes(Image image, Dimension maxSize) throws InvalidFormat {
    ByteArrayOutputStream byteArrayOut = new ByteArrayOutputStream();
    try {
      ImageIcon resized = new ImageIcon(resize(image, maxSize));

      BufferedImage resizedBufferedImage = toBufferedImage(resized);

      ImageIO.write(resizedBufferedImage, "png", byteArrayOut);
    }
    catch (IOException e) {
      throw new OperationFailed(e);
    }
    catch (ImageFormatException e) {
      throw new InvalidFormat(e);
    }
    return byteArrayOut.toByteArray();
  }

  private static BufferedImage toBufferedImage(ImageIcon icon) {
    BufferedImage bufferedImage = new BufferedImage(icon.getIconWidth(),
                                                    icon.getIconHeight(),
                                                    BufferedImage.TYPE_INT_RGB);
    Graphics g = bufferedImage.createGraphics();
    g.drawImage(icon.getImage(), 0, 0, bufferedImage.getWidth(), bufferedImage.getHeight(), Color.WHITE, null);
    g.dispose();
    return bufferedImage;
  }

  private static Image resize(Image image, Dimension maxSize) {
    if ((maxSize.width == 0) || (maxSize.height == 0)) {
      return new ImageIcon().getImage();
    }
    float xScale = (float)maxSize.width / (float)image.getWidth(null);
    float yScale = (float)maxSize.height / (float)image.getHeight(null);
    float minScale = Math.min(xScale, yScale);
    if (minScale < 1.0) {
      return image.getScaledInstance((int)(image.getWidth(null) * minScale),
                                     (int)(image.getHeight(null) * minScale),
                                     Image.SCALE_SMOOTH);
    }
    return image;
  }

  public static class Serializer implements PicsouGlobSerializer {

    public int getWriteVersion() {
      return 1;
    }

    public boolean shouldBeSaved(GlobRepository repository, FieldValues fieldValues) {
      return true;
    }

    public byte[] serializeData(FieldValues values) {
      SerializedByteArrayOutput serializedByteArrayOutput = new SerializedByteArrayOutput();
      SerializedOutput outputStream = serializedByteArrayOutput.getOutput();
      outputStream.writeBytes(values.get(IMAGE_DATA));
      return serializedByteArrayOutput.toByteArray();
    }

    public void deserializeData(int version, FieldSetter fieldSetter, byte[] data, Integer id) {
      if (version == 1) {
        deserializeDataV1(fieldSetter, data);
      }
    }

    private void deserializeDataV1(FieldSetter fieldSetter, byte[] data) {
      SerializedInput input = SerializedInputOutputFactory.init(data);
      fieldSetter.set(IMAGE_DATA, input.readBytes());
    }
  }
}
