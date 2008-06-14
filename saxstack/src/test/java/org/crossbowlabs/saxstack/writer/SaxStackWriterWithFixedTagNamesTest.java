package org.globsframework.saxstack.writer;

import java.io.IOException;
import java.util.List;

public class SaxStackWriterWithFixedTagNamesTest extends SaxStackWriterTestCase {
  public void testStandardOutput() throws Exception {
    checkOutput(new RootBuilder(root),
                "<contacts>" +
                "  <contact name='me' phone='512'/>" +
                "  <category name='family'>" +
                "    <contact name='Grandpa' phone='567'/>" +
                "    <category name='home'>" +
                "      <contact name='Bart' phone='756'/>" +
                "      <contact name='Homer' phone='757'/>" +
                "    </category>" +
                "  </category>" +
                "  <category name='work'>" +
                "    <contact name='Ralph' phone='123'/>" +
                "  </category>" +
                "</contacts>");
  }

  public void testFilteredOutput() throws Exception {
    checkOutput(new RootBuilder(root),
                "contacts/category/contact",
                "<contacts>" +
                "  <category name='family'>" +
                "    <contact name='Grandpa' phone='567'/>" +
                "  </category>" +
                "  <category name='work'>" +
                "    <contact name='Ralph' phone='123'/>" +
                "  </category>" +
                "</contacts>");
  }

  public void testFilteredOutputWithAllSubTree() throws Exception {
    checkOutput(new RootBuilder(root),
                "contacts/category/*",
                "<contacts>" +
                "  <category name='family'>" +
                "    <contact name='Grandpa' phone='567'/>" +
                "    <category name='home'>" +
                "      <contact name='Bart' phone='756'/>" +
                "      <contact name='Homer' phone='757'/>" +
                "    </category>" +
                "  </category>" +
                "  <category name='work'>" +
                "    <contact name='Ralph' phone='123'/>" +
                "  </category>" +
                "</contacts>");
  }

  public void testFilterAttr() throws Exception {
    checkOutput(new RootBuilder(root),
                "contacts/category[name]/contact[name,phone]",
                "<contacts>" +
                "  <category name='family'>" +
                "    <contact name='Grandpa' phone='567'/>" +
                "  </category>" +
                "  <category name='work'>" +
                "    <contact name='Ralph' phone='123'/>" +
                "  </category>" +
                "</contacts>");

    checkOutput(new RootBuilder(root),
                "contacts/category[]/contact[phone]",
                "<contacts>" +
                "  <category>" +
                "    <contact phone='567'/>" +
                "  </category>" +
                "  <category>" +
                "    <contact phone='123'/>" +
                "  </category>" +
                "</contacts>");
  }

  public void testRootTagError() throws Exception {
    checkOutputIsEmpty(new RootBuilder(root), "unknown");
  }

  public void testEmptyFilter() throws Exception {
    checkOutputIsEmpty(new RootBuilder(root), "");
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

  public static class CategoryBuilder extends IteratorBasedXmlNodeBuilder {
    public CategoryBuilder(List categories) {
      super("category", categories);
    }

    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
      Category category = (Category)getNextItem();
      tag.addAttribute("name", category.getName());
      if (category.getAdditionalInfo() != null) {
        tag.addValue(category.getAdditionalInfo());
      }
      return getChildren(category);
    }

    protected XmlNodeBuilder[] getChildren(Category category) {
      return new XmlNodeBuilder[]{
        new ContactBuilder(category.getContacts()),
        new CategoryBuilder(category.getSubCategories())
      };
    }
  }

  public static class ContactBuilder extends IteratorBasedXmlNodeBuilder {

    public ContactBuilder(List categories) {
      super("contact", categories);
    }

    public XmlNodeBuilder[] processNext(XmlTag tag) throws IOException {
      Contact contact = (Contact)getNextItem();
      tag.addAttribute("name", contact.getName());
      tag.addAttribute("phone", contact.getPhone());
      return new XmlNodeBuilder[0];
    }
  }
}
