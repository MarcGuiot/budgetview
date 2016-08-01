package com.budgetview.bank.connectors.webcomponents;

import com.budgetview.bank.connectors.webcomponents.utils.WebCommandFailed;
import com.gargoylesoftware.htmlunit.html.*;
import com.budgetview.bank.connectors.webcomponents.utils.HtmlUnit;
import com.budgetview.bank.connectors.webcomponents.utils.WebParsingError;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * NB: Uses javascript events an not hrefs
 */
public class WebImageMap extends WebComponent<HtmlMap> {

  private BasicAreaMap areaMap = new BasicAreaMap();

  protected WebImageMap(WebBrowser browser, HtmlMap map) throws WebParsingError {
    super(browser, map);
    DomNodeList<HtmlElement> areas = node.getElementsByTagName("area");
    for (HtmlElement area : areas) {
      String shape = area.getAttribute("shape");
      if (!shape.equalsIgnoreCase("rect")) {
        throw new WebParsingError(this, "Can only manage 'rect' areas");
      }
      Rectangle rect = extractRect(area);
      areaMap.add(rect, (HtmlArea)area);
    }
  }

  private Rectangle extractRect(HtmlElement area) throws WebParsingError {
    String coordinates = area.getAttribute("coords");
    String[] elts = coordinates.split(",");
    try {
      int left = Integer.parseInt(elts[0]);
      int top = Integer.parseInt(elts[1]);
      int right = Integer.parseInt(elts[2]);
      int bottom = Integer.parseInt(elts[3]);
      return new Rectangle(left, top, right - left, bottom - top);
    }
    catch (NumberFormatException e) {
      throw new WebParsingError(this, "Unable to parse coordinates '" + coordinates + "'");
    }
  }

  public WebImage getImage() throws WebParsingError {
    HtmlPage page = (HtmlPage)node.getPage();
    return new WebImage(browser,
                        (HtmlImage)(HtmlImage)HtmlUnit.getElementWithAttribute(page.getDocumentElement(), "img", "usemap", "#" + node.getNameAttribute(), HtmlImage.class));
  }

  public WebPage click(int x, int y) throws WebCommandFailed {
    HtmlArea clickedArea = areaMap.getArea(x, y);
    if (clickedArea != null) {
      return browser.doClick(clickedArea);
    }
    return browser.getCurrentPage();
  }

  public String getName() {
    return node.getNameAttribute();
  }

  public class BasicAreaMap {
    private Map<Rectangle, HtmlArea> areas = new HashMap<Rectangle, HtmlArea>();

    public void add(Rectangle rectangle, HtmlArea area) {
      areas.put(rectangle, area);
    }

    public HtmlArea getArea(int x, int y) {
      for (Rectangle rectangle : areas.keySet()) {
        if (rectangle.contains(x, y)) {
          return areas.get(rectangle);
        }
      }
      return null;
    }
  }
}
