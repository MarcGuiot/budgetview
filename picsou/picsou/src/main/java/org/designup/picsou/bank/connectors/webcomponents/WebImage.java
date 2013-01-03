package org.designup.picsou.bank.connectors.webcomponents;

import com.gargoylesoftware.htmlunit.html.HtmlImage;
import org.designup.picsou.bank.connectors.webcomponents.utils.HtmlUnit;
import org.designup.picsou.bank.connectors.webcomponents.utils.WebParsingError;

import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class WebImage extends WebComponent<HtmlImage>{
  protected WebImage(WebBrowser browser, HtmlImage node) {
    super(browser, node);
  }

  public BufferedImage getFirstImage() {
    try {
      final ImageReader imageReader = node.getImageReader();
      return imageReader.read(0);
    }
    catch (IOException e) {
      throw new RuntimeException("Can not load image " + node.getId());
    }
  }

  public void click() throws WebParsingError {
    HtmlUnit.click(browser, node);
  }
}
