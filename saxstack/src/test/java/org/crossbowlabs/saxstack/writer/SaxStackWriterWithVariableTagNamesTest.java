package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class SaxStackWriterWithVariableTagNamesTest extends SaxStackWriterTestCase {
  public void testStandardOutput() throws Exception {
    checkOutput(new RootBuilder(root),
                "<contacts>" +
                "  <me phone='512'/>" +
                "  <family>" +
                "    <Grandpa phone='567'/>" +
                "    <home>" +
                "      <Bart phone='756'/>" +
                "      <Homer phone='757'/>" +
                "    </home>" +
                "  </family>" +
                "  <work>" +
                "    <Ralph phone='123'/>" +
                "  </work>" +
                "</contacts>");
  }

  public void testFilteredOutput() throws Exception {
    checkOutput(new RootBuilder(root),
                "contacts/family/home/Bart",
                "<contacts>" +
                "  <family>" +
                "    <home>" +
                "      <Bart phone='756'/>" +
                "    </home>" +
                "  </family>" +
                "</contacts>");

    checkOutput(new RootBuilder(root),
                "contacts/family/home/Homer",
                "<contacts>" +
                "  <family>" +
                "    <home>" +
                "      <Homer phone='757'/>" +
                "    </home>" +
                "  </family>" +
                "</contacts>");

    checkOutput(new RootBuilder(root),
                "contacts/family/home",
                "<contacts>" +
                "  <family>" +
                "    <home/>" +
                "  </family>" +
                "</contacts>");
  }

  public void testFilterAttribut() throws Exception {
    checkOutput(new RootBuilder(root),
                "contacts/family/home/Homer[]",
                "<contacts>" +
                "  <family>" +
                "    <home>" +
                "      <Homer/>" +
                "    </home>" +
                "  </family>" +
                "</contacts>");
  }

  public static class RootBuilder implements XmlRootBuilder {
    private Category root;

    public RootBuilder(Category root) {
      this.root = root;
    }

    public String getTagName() {
      return "contacts";
    }

    public XmlNodeBuilder[] process(XmlTag rootTag) {
      return new XmlNodeBuilder[]{
        new ContactBuilder(root.getContacts()),
        new CategoryBuilder(root.getSubCategories())
      };
    }
  }

  static class CategoryBuilder implements XmlNodeBuilder {
    private Iterator categories;
    private Category current;

    public CategoryBuilder(List categories) {
      this.categories = categories.iterator();
      if (this.categories.hasNext()) {
        current = (Category)this.categories.next();
      }
    }

    public boolean hasNext() {
      return current != null;
    }

    public String getNextTagName() {
      return current.getName();
    }

    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
      try {
        return new XmlNodeBuilder[]{
          new ContactBuilder(current.getContacts()),
          new CategoryBuilder(current.getSubCategories())
        };
      }
      finally {
        if (categories.hasNext()) {
          current = (Category)categories.next();
        }
        else {
          current = null;
        }
      }
    }
  }

  static class ContactBuilder implements XmlNodeBuilder {
    private Iterator contacts;
    private Contact current;

    public ContactBuilder(List categories) {
      this.contacts = categories.iterator();
      if (contacts.hasNext()) {
        current = (Contact)contacts.next();
      }
    }

    public boolean hasNext() {
      return current != null;
    }

    public String getNextTagName() {
      return current.getName();
    }

    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
      tag.addAttribute("phone", current.getPhone());
      if (contacts.hasNext()) {
        current = (Contact)contacts.next();
      }
      else {
        current = null;
      }
      return new XmlNodeBuilder[0];
    }
  }
}
