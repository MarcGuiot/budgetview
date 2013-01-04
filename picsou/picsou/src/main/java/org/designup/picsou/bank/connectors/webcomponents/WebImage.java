package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlImage;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import javax.imageio.ImageReader;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WebImage extends WebComponent<HtmlImage> {
  protected WebImage(WebBrowser browser, HtmlImage node) {
    super(browser, node);
  }

  public Icon asIcon() {
    return new ImageIcon(extractFirstImage(node));
  }

  public static BufferedImage extractFirstImage(HtmlImage img) {
    try {
      final ImageReader imageReader = img.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + img.getId());
    }
  }

  public BufferedImage getFirstImage() {
    return extractFirstImage(node);
  }

  public void click() throws WebParsingError {
    HtmlUnit.click(browser, node);
  }
}
