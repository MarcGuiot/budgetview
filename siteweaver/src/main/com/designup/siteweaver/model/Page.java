package com.designup.siteweaver.model;

import java.util.*;

public class Page {

  private String title;
  private String shortTitle;
  private String filePath;
  private Page parentPage;
  private String templateFile;
  private boolean templateGenerationEnabled = true;
  private List<Page> subPages = new ArrayList<Page>();
  private Map<String, String> keyValues = new HashMap<String, String>();
  private List<String> borderBoxesFiles = new ArrayList<String>();

  public Page(String filePath, String title, String shortTitle) {
    this(filePath, title, shortTitle, null);
  }

  public Page(String filePath, String title, String shortTitle, String templateFile) {
    this.filePath = filePath;
    this.title = title;
    this.shortTitle = shortTitle;
    this.templateFile = templateFile;
  }

  public String toString() {
    return shortTitle;
  }

  public String getFilePath() {
    return filePath;
  }

  public String getOutputFilePath() {
    if (filePath.endsWith(".html")) {
      return filePath;
    }
    return filePath.substring(0, filePath.lastIndexOf('.')) + ".html";
  }

  public String getUrl() {
    if (filePath.equals("index.html")) {
      return "/";
    }
    if (filePath.endsWith("/index.html")) {
      return "/" + filePath.replace("/index.html", "").replace("_", "-");
    }
    return "/" + filePath.replace(".html", "").replace("_", "-");
  }

  public String getTitle() {
    return title;
  }

  public String getShortTitle() {
    return shortTitle;
  }

  public String getTemplateFilePath() {
    if (templateFile != null) {
      return templateFile;
    }
    if (parentPage != null) {
      return parentPage.getTemplateFilePath();
    }
    return null;
  }

  /**
   * Enables the template generation mechanism for this page.
   * If disabled, the contents of this page will be dumped as is in
   * the output page without using the template generation.
   */
  public void setTemplateGenerationEnabled(boolean enabled) {
    templateGenerationEnabled = enabled;
  }

  public boolean isTemplateGenerationEnabled() {
    return templateGenerationEnabled;
  }

  public boolean isRootPage() {
    return parentPage == null;
  }

  public boolean isDescendantOf(Page page) {
    if (page == this) {
      return true;
    }
    return parentPage != null && parentPage.isDescendantOf(page);
  }

  public void addSubPage(Page page) {
    page.setParentPage(this);
    subPages.add(page);
  }

  public Page getSubPageAtIndex(int i) {
    return subPages.get(i);
  }

  public int getSubPagesCount() {
    return subPages.size();
  }

  public boolean hasParentPage() {
    return (parentPage != null);
  }

  public Page getParentPage() {
    return parentPage;
  }

  private void setParentPage(Page newParentPage) {
    parentPage = newParentPage;
  }

  public Page getRootPage() {
    if (parentPage != null) {
      return parentPage.getRootPage();
    }
    return this;
  }

  public Page getNextPageInDepthFirstTraversal() {
    if (hasSubPages()) {
      return subPages.get(0);
    }
    Page parent = parentPage;
    Page child = this;
    Page nextPage;
    while (parent != null) {
      nextPage = parent.getNextSubPage(child);
      if (nextPage != null) {
        return nextPage;
      }
      child = parent;
      parent = child.getParentPage();
    }
    return child;
  }

  public boolean hasSubPages() {
    return (subPages.size() > 0);
  }

  public List<Page> getSubPages() {
    return Collections.unmodifiableList(subPages);
  }

  private Page getNextSubPage(Page subPage) {
    for (Iterator<Page> iter = subPages.iterator(); iter.hasNext(); ) {
      if (iter.next() == subPage) {
        if (iter.hasNext()) {
          return iter.next();
        }
        break;
      }
    }
    return null;
  }

  public void addKeyWithValue(String key, String value) {
    keyValues.put(key, value);
  }

  public boolean isTrue(String key, boolean defaultValue, boolean inherited) {
    String value = getValueForKey(key, inherited);
    if (value == null) {
      return defaultValue;
    }
    return "true".equalsIgnoreCase(value);
  }

  public String getValueForKey(String key, boolean inherited) {
    String value = keyValues.get(key);
    if (inherited && (value == null) && hasParentPage()) {
      value = parentPage.getValueForKey(key, inherited);
    }
    return value;
  }

  public void addBorderBox(String fileName) {
    borderBoxesFiles.add(fileName);
  }

  public String[] getBorderBoxesFiles() {
    return borderBoxesFiles.toArray(new String[borderBoxesFiles.size()]);
  }

  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Page page = (Page)o;

    if (!filePath.equals(page.filePath)) {
      return false;
    }
    if (!shortTitle.equals(page.shortTitle)) {
      return false;
    }
    if (!title.equals(page.title)) {
      return false;
    }

    return true;
  }

  public int hashCode() {
    int result = title.hashCode();
    result = 31 * result + shortTitle.hashCode();
    result = 31 * result + filePath.hashCode();
    return result;
  }
}
