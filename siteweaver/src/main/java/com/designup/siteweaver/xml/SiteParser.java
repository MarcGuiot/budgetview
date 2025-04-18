package com.designup.siteweaver.xml;

import com.designup.siteweaver.model.CopySet;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SiteParser {
  public static Site parse(File configFile) throws Exception {
    Element siteElt = XmlDomParser.parse(FileUtils.createEncodedReader(configFile), "site");
    SiteParser parser = new SiteParser();
    return parser.parseSite(configFile,
                            siteElt,
                            parser.parseRootPage(siteElt),
                            parser.parseFilesToCopy(siteElt),
                            parser.parseIgnorePaths(siteElt),
                            configFile.getParent());
  }

  private SiteParser() {
  }

  private Site parseSite(File configFile, Element siteElt, Page rootPage, List<CopySet> filesToCopy, List<String> targetPathsToIgnore, String inputDir) throws XmlParsingException {
    return new Site(configFile,
                    rootPage,
                    inputDir,
                    XmlDomParser.getOptionalAttribute(siteElt, "pagesDir", ""),
                    XmlDomParser.getOptionalAttribute(siteElt, "filesDir", ""),
                    XmlDomParser.getMandatoryAttribute(siteElt, "url"),
                    filesToCopy,
                    targetPathsToIgnore);
  }

  private Page parseRootPage(Element siteElt) throws XmlParsingException {
    NodeList pageElts = XmlDomParser.getChildrenWithName(siteElt, "page");
    if (pageElts.getLength() > 1) {
      StringBuilder builder = new StringBuilder();
      builder.append("Only one <page> can be defined under <site> - actual content:\n");
      for (int i = 0, max = pageElts.getLength(); i < max; i++) {
        Element element = (Element)pageElts.item(i);
        builder.append("  ")
          .append(element.getTagName() + " / title=" + element.getAttribute("title"))
          .append("\n");
      }
      throw new XmlParsingException(builder.toString());
    }
    if (pageElts.getLength() == 0) {
      throw new XmlParsingException("No <page> tag found under <site>");
    }

    Element pageElt = (Element)pageElts.item(0);
    Page page = createPage(pageElt);
    parseSubPages(pageElt, page);
    return page;
  }

  private void parseSubPages(Element pageElt, Page page) throws XmlParsingException {
    NodeList pageElts = XmlDomParser.getChildrenWithName(pageElt, "page");
    for (int i = 0, max = pageElts.getLength(); i < max; i++) {
      Element subPageElt = (Element)pageElts.item(i);
      Page subPage = createPage(subPageElt);
      page.addSubPage(subPage);
      parseSubPages(subPageElt, subPage);
    }
  }

  private Page createPage(Element pageElt) throws XmlParsingException {
    String title = convert(XmlDomParser.getMandatoryAttribute(pageElt, "title"));
    Page page = new Page(XmlDomParser.getMandatoryAttribute(pageElt, "file"),
                         title,
                         convert(XmlDomParser.getOptionalAttribute(pageElt, "shortTitle", title)),
                         XmlDomParser.getOptionalAttribute(pageElt, "template", null));
    page.setTemplateGenerationEnabled(!XmlDomParser.getOptionalBooleanAttribute(pageElt, "disableTemplate", false));
    parseKeys(pageElt, page);
    parseBorderBoxes(pageElt, page);
    return page;
  }

  private void parseKeys(Element pageElt, Page page) throws XmlParsingException {
    NodeList keyElts = XmlDomParser.getChildrenWithName(pageElt, "key");
    for (int i = 0, max = keyElts.getLength(); i < max; i++) {
      Element keyElt = (Element)keyElts.item(i);
      String name = XmlDomParser.getMandatoryAttribute(keyElt, "name");
      String value = convert(XmlDomParser.getOptionalAttribute(keyElt, "value", null));
      if (value == null) {
        throw new RuntimeException("No value set for key '" + name + "' in page: " + page.getTitle());
      }
      page.addKeyWithValue(name, value);
    }
  }

  private void parseBorderBoxes(Element pageElt, Page page) throws XmlParsingException {
    NodeList keyElts = XmlDomParser.getChildrenWithName(pageElt, "box");
    for (int i = 0, max = keyElts.getLength(); i < max; i++) {
      Element keyElt = (Element)keyElts.item(i);
      page.addBorderBox(XmlDomParser.getMandatoryAttribute(keyElt, "file"));
    }
  }

  private List<CopySet> parseFilesToCopy(Element siteElt) throws XmlParsingException {
    List<CopySet> result = new ArrayList<CopySet>();
    NodeList copyNodes = XmlDomParser.getChildrenWithName(siteElt, "copy");
    for (int i = 0, max = copyNodes.getLength(); i < max; i++) {
      Element copyElt = (Element)copyNodes.item(i);
      CopySet copySet = new CopySet(XmlDomParser.getOptionalAttribute(copyElt, "baseDir", ""));
      addFilesToCopy(copyElt, copySet, "file");
      addFilesToCopy(copyElt, copySet, "dir");
      result.add(copySet);
    }
    return result;
  }

  private void addFilesToCopy(Element siteElt, CopySet result, String tagName) throws XmlParsingException {
    NodeList fileNodes = XmlDomParser.getChildrenWithName(siteElt, tagName);
    for (int j = 0, maxFileElts = fileNodes.getLength(); j < maxFileElts; j++) {
      Element fileElt = (Element)fileNodes.item(j);
      result.add(XmlDomParser.getMandatoryAttribute(fileElt, "path"));
    }
  }

  private List<String> parseIgnorePaths(Element pageElt) throws XmlParsingException {
    List<String> ignorePaths = new ArrayList<String>();
    NodeList keyElts = XmlDomParser.getChildrenWithName(pageElt, "ignore");
    for (int i = 0, max = keyElts.getLength(); i < max; i++) {
      Element keyElt = (Element)keyElts.item(i);
      String targetPath = XmlDomParser.getMandatoryAttribute(keyElt, "targetPath");
      if (!targetPath.equals("/") && !targetPath.startsWith("/")) {
        targetPath = "/" + targetPath;
      }
      ignorePaths.add(targetPath);
    }
    return ignorePaths;
  }

  private String convert(String text) {
    String[][] conversions = {
      {"é", "&eacute;"},
      {"è", "&egrave;"},
      {"ù", "&ugrave;"},
      {"ê", "&ecirc;"},
      {"ç", "&ccedil;"},
      {"à", "&agrave;"},
    };
    String result = text;
    for (int i = 0; i < conversions.length; i++) {
      result = result.replaceAll(conversions[i][0], conversions[i][1]);
    }
    return result;
  }
}
