package org.crossbowlabs.globs.wicket.table;

import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlTable;
import com.gargoylesoftware.htmlunit.html.HtmlTableCell;
import com.gargoylesoftware.htmlunit.html.HtmlTableRow;
import java.util.ArrayList;
import java.util.List;
import org.crossbowlabs.globs.metamodel.DummyObject;
import org.crossbowlabs.globs.metamodel.DummyObjectWithMultiLineText;
import org.crossbowlabs.globs.model.*;
import static org.crossbowlabs.globs.model.FieldValue.value;
import org.crossbowlabs.globs.model.format.DescriptionService;
import org.crossbowlabs.globs.model.utils.GlobMatchers;
import org.crossbowlabs.globs.utils.TestUtils;
import org.crossbowlabs.globs.wicket.ComponentFactory;
import org.crossbowlabs.globs.wicket.DummyPage;
import org.crossbowlabs.globs.wicket.FormSubmitListener;
import org.crossbowlabs.globs.wicket.GlobRepositoryLoader;
import org.crossbowlabs.globs.wicket.WebTestCase;
import org.crossbowlabs.globs.wicket.table.columns.AbstractGlobTableColumn;
import org.crossbowlabs.globs.wicket.table.columns.DeleteButtonColumn;
import wicket.Component;
import wicket.markup.html.basic.Label;
import wicket.model.Model;

public class GlobTableTest extends WebTestCase {
  public void testStandardCase() throws Exception {
    load("<dummyObject id='1' name='name1' value='1.1'/>" +
         "<dummyObject id='2' name='name2' value='2.2'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(DummyObject.VALUE)
              .getPanel(componentId);
      }
    });

    assertEquals(3, table.getRowCount());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Value");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "1.10");
    TestUtils.assertEquals(rowAsText(table, 2), "name2", "2.20");
  }

  public void testMatcher() throws Exception {
    load("<dummyObject id='1' name='name1' value='1.1'/>" +
         "<dummyObject id='2' name='name2' value='2.2'/>" +
         "<dummyObject id='3' name='name3' value='3.3'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .setMatcher(GlobMatchers.fieldEquals(DummyObject.NAME, "name2"))
              .add(DummyObject.NAME)
              .add(DummyObject.VALUE)
              .getPanel(componentId);
      }
    });

    assertEquals(2, table.getRowCount());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Value");
    TestUtils.assertEquals(rowAsText(table, 1), "name2", "2.20");
  }

  public void testLink() throws Exception {
    load("<dummyObject id='1' name='name1'/>" +
         "<dummyObject id='2' name='name2' link='1'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(DummyObject.LINK)
              .getPanel(componentId);
      }
    });

    assertEquals(3, table.getRowCount());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Sibling");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "");
    TestUtils.assertEquals(rowAsText(table, 2), "name2", "name1");
  }

  public void testCustomDisplayColumn() throws Exception {
    load("<dummyObject id='1' name='name1'/>" +
         "<dummyObject id='2' name='name2' link='1'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(new AbstractGlobTableColumn("Key") {
                public Component getComponent(String id,
                                              String tableId,
                                              Key key,
                                              MutableFieldValues fieldValues,
                                              int rowIndex,
                                              Component row, GlobRepository repository,
                                              DescriptionService descriptionService) {
                  return new Label(id, new Model(key));
                }
              })
              .getPanel(componentId);
      }
    });

    assertEquals(3, table.getRowCount());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Key");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "dummyObject[id=1]");
    TestUtils.assertEquals(rowAsText(table, 2), "name2", "dummyObject[id=2]");
  }

  public void testRowLevelEdition() throws Exception {
    load("<dummyObject id='1' name='name1' present='false'/>" +
         "<dummyObject id='2' name='name2' present='false'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.ROW)
              .add(DummyObject.NAME)
              .addFieldEditor(DummyObject.PRESENT)
              .addSubmitListener(new DummySubmitListener())
              .addSubmitButton("")
              .getPanel(componentId);
      }
    });

    List list = table.getHtmlElementsByTagName("tr");
    assertEquals(3, list.size());

    HtmlForm form = (HtmlForm)table.getHtmlElementById("component_0");
    form.getInputByName("present_0").click();
    form.submit();

    Glob dummyObject1 = getDummyObject(1);
    assertTrue(dummyObject1.get(DummyObject.PRESENT));
    assertEquals("name1_true", dummyObject1.get(DummyObject.NAME));
  }

  public void DISABLED_testTableLevelEdition() throws Exception {
    load("<dummyObject id='1' name='name1' present='false'/>" +
         "<dummyObject id='2' name='name2' present='false'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.TABLE)
              .add(DummyObject.NAME)
              .addFieldEditor(DummyObject.PRESENT)
              .addSubmitListener(new DummySubmitListener())
              .addSubmitButton("")
              .getPanel(componentId);
      }
    });

    System.out.println(
          "GlobTableTest.testTableLevelEdition\n" + table.getPage().getWebResponse().getContentAsString());

    List list = table.getHtmlElementsByTagName("tr");
    assertEquals(3, list.size());

    HtmlForm form = (HtmlForm)table.getHtmlElementById(DummyPage.COMPONENT_ID + "_form");
    form.getInputByName("present_0").click();
    form.getInputByName("present_1").click();
    form.submit();

    Glob dummyObject1 = getDummyObject(1);
    assertTrue(dummyObject1.get(DummyObject.PRESENT));
    assertEquals("name1_true", dummyObject1.get(DummyObject.NAME));

    Glob dummyObject2 = getDummyObject(2);
    assertTrue(dummyObject2.get(DummyObject.PRESENT));
    assertEquals("name2_true", dummyObject2.get(DummyObject.NAME));
  }

  // Pb with HtmlUnit ?
  public void DISABLED_testMultiLineText() throws Exception {
    String[] comments = {
//      "aaa",
"bbb\nbbb\nbbb",
//      "ccccccccc\nccccccccc\nccccccccc",
//      "dddddddddd dddddddddd dddddddddd"
    };

    for (String comment : comments) {
      repository.create(DummyObjectWithMultiLineText.TYPE,
                        value(DummyObjectWithMultiLineText.COMMENT, comment));
    }

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObjectWithMultiLineText.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObjectWithMultiLineText.COMMENT)
              .getPanel(componentId);
      }
    });

//    assertEquals(5, table.getRowCount());
//    TestUtils.assertEquals(rowAsText(table, 0), "comment");
//    TestUtils.assertEquals(rowAsText(table, 1), "aaa");
//    TestUtils.assertEquals(rowAsText(table, 2), "bbb [...]");
//    TestUtils.assertEquals(rowAsText(table, 3), "ccccccccc [...]");
//    TestUtils.assertEquals(rowAsText(table, 4), "dddddddddd ddddddddd [...]");

    TestUtils.assertEquals(rowAsText(table, 1), "bbb [...]");

    HtmlPage page = (HtmlPage)table.getPage();
    HtmlAnchor anchor = page.getFirstAnchorByText("[...]");
    System.out.println("GlobTableTest.testMultiLineText " + anchor.asXml());
    HtmlPage newPage = (HtmlPage)anchor.click();

    Thread.sleep(4000);
    if (!page.asXml().equals(newPage.asXml())) {

    }

    HtmlTable newTable = (HtmlTable)page.getHtmlElementById(DummyPage.COMPONENT_ID);
//    assertEquals(3, newTable.getRowCount());
    TestUtils.assertEquals(rowAsText(newTable, 1), "bbb\nbbb\nbbb [<<<]");
  }

  // Ajax call for edit button works in real life but cannot be tested.
  // To be tried again when HtmlUnit 1.14 is available.
  public void DISABLED_testStandardRowEditionPanel() throws Exception {
    load("<dummyObject id='1' name='name1' present='false'/>" +
         "<dummyObject id='2' name='name2' present='false'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(DummyObject.PRESENT)
              .addDefaultRowEditor("Edition", "edit")
              .getPanel(componentId);
      }
    });

    List list = table.getHtmlElementsByTagName("tr");
    assertEquals(3, list.size());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Present", "Edition");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "no", "edit");
    TestUtils.assertEquals(rowAsText(table, 2), "name2", "no", "edit");

    HtmlButtonInput button = (HtmlButtonInput)table
          .getHtmlElementById("component_table_rows_0_rowContent_row_rowRenderer_columns_2_cell_linkButton");
    HtmlPage newPage = (HtmlPage)button.click();

    HtmlForm form = (HtmlForm)newPage.getHtmlElementById("editionPanel");
    form.getInputByName("editionPanel_name").setValueAttribute("newName1");
    form.getInputByName("editionPanel_present").setChecked(true);
    HtmlPage afterSubmit = (HtmlPage)form.getInputByName("submit").click();

    HtmlTable newTable = (HtmlTable)afterSubmit.getHtmlElementById(DummyPage.COMPONENT_ID);
    List newList = newTable.getHtmlElementsByTagName("tr");
    assertEquals(3, newList.size());
    TestUtils.assertEquals(rowAsText(newTable, 0), "Name", "Present", "Edition");
    TestUtils.assertEquals(rowAsText(newTable, 1), "name2", "no", "edit");
    TestUtils.assertEquals(rowAsText(newTable, 2), "newName1", "yes", "edit");
  }

  public void testDefaultRowEditorOnlyWorksWithEditPolicyNone() throws Exception {
    checkRendereringError("DefaultRowEditor provides its own form and can only be used "
                          + "when the table edit policy is READ_ONLY",
                          new ComponentFactory() {
                            public Component create(String componentId,
                                                    GlobRepositoryLoader repositoryLoader) {
                              return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.ROW)
                                    .add(DummyObject.NAME)
                                    .add(DummyObject.PRESENT)
                                    .addDefaultRowEditor("edition", "edit")
                                    .getPanel(componentId);
                            }
                          });
  }

  public void testDeleteButtonNotBrokenByRowEditor() throws Exception {
    String xmlDescription = "<dummyObject id='1' name='name1' present='false'/>" +
                            "<dummyObject id='2' name='name2' present='false'/>";
    load(xmlDescription);

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(DummyObject.PRESENT)
              .addDefaultRowEditor("Edition", "edit")
              .add(new DeleteButtonColumn("Deletion", "Delete", "delete_glob"))
              .getPanel(componentId);
      }
    });

    List list = table.getHtmlElementsByTagName("tr");
    assertEquals(3, list.size());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Present", "Edition", "Deletion");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "no", "edit", "Delete");
    TestUtils.assertEquals(rowAsText(table, 2), "name2", "no", "edit", "Delete");

    HtmlButtonInput deleteButton = (HtmlButtonInput)table.getHtmlElementById("delete_glob_0");
    HtmlPage newPage = (HtmlPage)deleteButton.click();

    HtmlTable newTable = (HtmlTable)newPage.getHtmlElementById(DummyPage.COMPONENT_ID);
    List newList = newTable.getHtmlElementsByTagName("tr");
    assertEquals(2, newList.size());
    TestUtils.assertEquals(rowAsText(newTable, 0), "Name", "Present", "Edition", "Deletion");
    TestUtils.assertEquals(rowAsText(newTable, 1), "name2", "no", "edit", "Delete");
  }

  public void testDisabledDeleteButtonColumn() throws Exception {
    load("<dummyObject id='1' name='name1' present='false'/>");

    HtmlTable table = (HtmlTable)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobTableBuilder.init(DummyObject.TYPE, TableEditPolicy.READ_ONLY)
              .add(DummyObject.NAME)
              .add(new DummyDeleteButtonColumn())
              .getPanel(componentId);
      }
    }); 

    List list = table.getHtmlElementsByTagName("tr");
    assertEquals(2, list.size());
    TestUtils.assertEquals(rowAsText(table, 0), "Name", "Deletion");
    TestUtils.assertEquals(rowAsText(table, 1), "name1", "Delete");

    HtmlButtonInput deleteButton = (HtmlButtonInput)table.getHtmlElementById("delete_glob_0");
    HtmlPage newPage = (HtmlPage)deleteButton.click();

    assertAlert("not deletable");
    changeSetListener.assertNoChanges();

    HtmlTable newTable = (HtmlTable)newPage.getHtmlElementById(getComponentId());
    List newList = newTable.getHtmlElementsByTagName("tr");
    assertEquals(2, newList.size());
    TestUtils.assertEquals(rowAsText(newTable, 0), "Name", "Deletion");
    TestUtils.assertEquals(rowAsText(newTable, 1), "name1", "Delete");
  }

  private List<String> rowAsText(HtmlTable table, int rowIndex) {
    List<String> result = new ArrayList<String>();

    HtmlTableRow row = table.getRow(rowIndex);
    List cells = row.getCells();
    for (Object cell : cells) {
      HtmlTableCell htmlCell = (HtmlTableCell)cell;
      result.add(htmlCell.asText());
    }
    return result;
  }

  private static class DummySubmitListener implements FormSubmitListener {
    public void onSubmit(Key key, FieldValues values, GlobRepository repository) {
      repository.update(key, DummyObject.NAME,
                        values.get(DummyObject.NAME) + "_" + values.get(DummyObject.PRESENT));
    }
  }

  protected void load(String xmlDescription) {
    checker.parse(repository, xmlDescription);
    changeSetListener.reset();
  }

  private static class DummyDeleteButtonColumn extends DeleteButtonColumn {
    public DummyDeleteButtonColumn() {
      super("Deletion", "Delete", "delete_glob");
    }

    protected boolean deletionEnabled(Key key,
                                      MutableFieldValues fieldValues,
                                      int rowIndex,
                                      GlobRepository repository,
                                      DescriptionService descriptionService) {
      return false;
    }

    protected String getDeletionDisabledMessage(Key key,
                                                MutableFieldValues fieldValues,
                                                int rowIndex,
                                                GlobRepository repository,
                                                DescriptionService descriptionService) {
      return "not deletable";
    }
  }
}
