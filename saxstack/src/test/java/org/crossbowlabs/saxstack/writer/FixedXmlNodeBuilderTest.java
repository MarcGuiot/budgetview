package org.globsframework.saxstack.writer;

import java.util.List;

public class FixedXmlNodeBuilderTest extends SaxStackWriterTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    root = new Category("contacts",
                        new Contact[]{
                          new Contact("me", "123")
                        },
                        new Category[]{
                          new Category("empty", new Contact[0], new Category[0])
                        });
  }

  public void testPruneEmpry() throws Exception {
    checkOutput(new RootExporter(root, true),
                "<contacts>" +
                "  <contact name='me' phone='123'/>" +
                "  <category name='empty'/>" +
                "</contacts>");
  }

  public void testDoNotPruneEmpty() throws Exception {
    checkOutput(new RootExporter(root, false),
                "<contacts>" +
                "  <contact name='me' phone='123'/>" +
                "  <category name='empty'>" +
                "    <contacts/>" +
                "  </category>" +
                "</contacts>");
  }

  public static class RootExporter implements XmlRootBuilder {
    private Category root;
    private boolean pruneIfEmpty;

    public RootExporter(Category root, boolean pruneIfEmpty) {
      this.root = root;
      this.pruneIfEmpty = pruneIfEmpty;
    }

    public String getTagName() {
      return "contacts";
    }

    public XmlNodeBuilder[] process(XmlTag rootTag) {
      return new XmlNodeBuilder[]{
        new SaxStackWriterWithFixedTagNamesTest.ContactBuilder(root.getContacts()),
        new CategoryBuilderWithContacts(root.getSubCategories(), pruneIfEmpty)
      };
    }
  }

  static class CategoryBuilderWithContacts extends SaxStackWriterWithFixedTagNamesTest.CategoryBuilder {
    private boolean pruneIfEmpty;

    public CategoryBuilderWithContacts(List categories, boolean pruneIfEmpty) {
      super(categories);
      this.pruneIfEmpty = pruneIfEmpty;
    }

    protected XmlNodeBuilder[] getChildren(Category category) {
      return new XmlNodeBuilder[]{new FixedXmlNodeBuilder("contacts",
                                                          super.getChildren(category), pruneIfEmpty)};
    }
  }
}
