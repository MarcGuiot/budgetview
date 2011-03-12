package org.globsframework.wicket.form;

import com.gargoylesoftware.htmlunit.html.*;
import org.apache.wicket.Component;
import org.globsframework.metamodel.*;
import static org.globsframework.model.FieldValue.value;
import org.globsframework.model.FieldValues;
import org.globsframework.model.FieldValuesBuilder;
import org.globsframework.model.Glob;
import org.globsframework.model.Key;
import org.globsframework.model.utils.DefaultFieldValues;
import org.globsframework.utils.Dates;
import org.globsframework.wicket.ComponentFactory;
import org.globsframework.wicket.GlobRepositoryLoader;
import org.globsframework.wicket.WebTestCase;

import java.util.List;

public abstract class GlobFormTest extends WebTestCase {
  public void testStandardCase() throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObject.TYPE)
          .add(DummyObject.ID)
          .add(DummyObject.NAME)
          .add(DummyObject.VALUE)
          .add(DummyObject.DATE)
          .create(componentId);
      }
    });

    form.<HtmlInput>getInputByName("component_id").setValueAttribute("1");
    form.<HtmlInput>getInputByName("component_name").setValueAttribute("name 1");
    form.<HtmlInput>getInputByName("component_value").setValueAttribute("1.25");
    form.<HtmlInput>getInputByName("component_date").setValueAttribute("25/12/2007");
    HtmlPage newPage = (HtmlPage)form.<HtmlInput>getInputByName("submit").click();

    dumpPage(newPage);
    assertNoMessages(form);

    Glob createdObject = repository.get(Key.create(DummyObject.TYPE, 1));
    assertEquals("name 1", createdObject.get(DummyObject.NAME));
    assertEquals(1.25, createdObject.get(DummyObject.VALUE));
    assertEquals(Dates.parse("2007/12/25"), createdObject.get(DummyObject.DATE));

    HtmlForm newForm = (HtmlForm)newPage.getHtmlElementById(getComponentId());
    assertEquals("", newForm.<HtmlInput>getInputByName("component_name").getValueAttribute());
    assertEquals("", newForm.<HtmlInput>getInputByName("component_date").getValueAttribute());
  }

  public void testEditingAnExistingObject() throws Exception {

    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 0),
                      value(DummyObject.NAME, "obj0"));

    final Key key = Key.create(DummyObject.TYPE, 1);
    repository.create(DummyObject.TYPE, key.toArray());

    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        FieldValues values = FieldValuesBuilder.init()
          .set(DummyObject.NAME, "name 1")
          .set(DummyObject.VALUE, 1.25)
          .set(DummyObject.DATE, Dates.parse("2007/12/25"))
          .set(DummyObject.LINK, 0)
          .get();
        return GlobFormBuilder.init(key, new DefaultFieldValues(values))
          .add(DummyObject.NAME)
          .add(DummyObject.VALUE)
          .add(DummyObject.DATE)
          .add(DummyObject.LINK)
          .create(componentId);
      }
    });

    assertEquals("name 1", form.<HtmlInput>getInputByName("component_name").getValueAttribute());
    assertEquals("1.25", form.<HtmlInput>getInputByName("component_value").getValueAttribute());
    assertEquals("25/12/2007", form.<HtmlInput>getInputByName("component_date").getValueAttribute());
    List selectedOptions = form.getSelectByName("component_link").getSelectedOptions();
    assertEquals(1, selectedOptions.size());
    assertEquals("obj0", ((HtmlOption)selectedOptions.get(0)).asText());
    form.<HtmlInput>getInputByName("submit").click();

    assertNoMessages(form);

    Glob modifiedObject = repository.get(key);
    assertEquals("name 1", modifiedObject.get(DummyObject.NAME));
    assertEquals(1.25, modifiedObject.get(DummyObject.VALUE));
    assertEquals(Dates.parse("2007/12/25"), modifiedObject.get(DummyObject.DATE));
  }

  public void testEditingAKeyFieldIsForbidden() throws Exception {
    checkRendereringError("Key field 'id' cannot be edited in a form",
                          new ComponentFactory() {
                            public Component create(String componentId,
                                                    GlobRepositoryLoader repositoryLoader) {
                              return GlobFormBuilder.init(Key.create(DummyObject.TYPE, 1),
                                                          new DefaultFieldValues())
                                .add(DummyObject.ID)
                                .create(componentId);
                            }
                          }
    );
  }

  public void testFieldsMustBePartOfTheGivenType() throws Exception {
    checkRendereringError("Field 'dummyObject2.label' is not part of type 'dummyObject'",
                          new ComponentFactory() {
                            public Component create(String componentId,
                                                    GlobRepositoryLoader repositoryLoader) {
                              return GlobFormBuilder.init(DummyObject.TYPE)
                                .add(DummyObject2.LABEL)
                                .create(componentId);
                            }
                          }
    );

    checkRendereringError("Field 'dummyObject2.label' is not part of type 'dummyObject'",
                          new ComponentFactory() {
                            public Component create(String componentId,
                                                    GlobRepositoryLoader repositoryLoader) {
                              return GlobFormBuilder.init(Key.create(DummyObject.TYPE, 1),
                                                          new DefaultFieldValues())
                                .add(DummyObject2.LABEL)
                                .create(componentId);
                            }
                          }
    );
  }

  public void testLink() throws Exception {
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 10),
                      value(DummyObject.NAME, "obj10"));
    repository.create(DummyObject.TYPE,
                      value(DummyObject.ID, 11),
                      value(DummyObject.NAME, "obj11"));

    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObject.TYPE)
          .add(DummyObject.ID)
          .add(DummyObject.LINK)
          .create(componentId);
      }
    });

    form.<HtmlInput>getInputByName("component_id").setValueAttribute("1");

    HtmlSelect select = form.getSelectByName("component_link");
    List<HtmlOption> options = select.getOptions();
    assertEquals(3, options.size());
    assertEquals("", options.get(0).getValueAttribute());
    assertEquals("obj10", options.get(1).asText());
    assertEquals("obj11", options.get(2).asText());
    select.setSelectedAttribute(options.get(2), true);
    form.<HtmlInput>getInputByName("submit").click();

    assertNoMessages(form);

    Glob createdObject = repository.get(Key.create(DummyObject.TYPE, 1));
    assertEquals(new Integer(11), createdObject.get(DummyObject.LINK));
  }

  public void testMissingRequiredField() throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObjectWithRequiredFields.TYPE)
          .add(DummyObjectWithRequiredFields.NAME)
          .add(DummyObjectWithRequiredFields.VALUE)
          .create(componentId);
      }
    });

    HtmlPage newPage = (HtmlPage)form.<HtmlInput>getInputByName("submit").click();

    checkMessages(newPage, "field 'name' is required", "field 'value' is required");

    assertFalse(repository.contains(DummyObjectWithRequiredFields.TYPE));
  }

  public void testMissingRequiredLink() throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObjectWithRequiredFields.TYPE)
          .add(DummyObjectWithRequiredLink.LINK)
          .create(componentId);
      }
    });

    HtmlPage newPage = (HtmlPage)form.<HtmlInput>getInputByName("submit").click();

    checkMessages(newPage, "field 'link' is required.");

    assertFalse(repository.contains(DummyObjectWithRequiredFields.TYPE));
  }

  public void testMultiLineTextField() throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObjectWithMultiLineText.TYPE)
          .add(DummyObjectWithMultiLineText.ID)
          .add(DummyObjectWithMultiLineText.COMMENT)
          .create(componentId);
      }
    });

    String comment = "Hello,\nHow are you?";

    form.<HtmlInput>getInputByName("component_id").setValueAttribute("1");
    HtmlTextArea textArea = form.getTextAreaByName("component_comment");
    textArea.setText(comment);
    form.<HtmlInput>getInputByName("submit").click();

    assertNoMessages(form);

    Glob createdObject = repository.get(Key.create(DummyObjectWithMultiLineText.TYPE, 1));
    assertEquals(comment, createdObject.get(DummyObjectWithMultiLineText.COMMENT));
  }

  public void testInvalidDateValue() throws Exception {
    checkInvalidValue(DummyObject.DATE,
                      "invalid value",
                      "'invalid value' is not a valid Date.");
  }

  public void testDateFormats() throws Exception {
    checkDateFormat("25/12/7", "2007/12/25");
    checkDateFormat("25/12/07", "2007/12/25");
    checkDateFormat("25/12/007", "2007/12/25");
  }

  public void testMaxSizeStringValidation() throws Exception {
    checkInvalidValue(DummyObjectWithMaxSizeString.TEXT,
                      "more than ten chars",
                      "'more than ten chars' must be at most 10 chars.");
  }

  public void testInvalidNumberValue() throws Exception {
    checkInvalidValue(DummyObject.VALUE,
                      "not a double",
                      "'not a double' is not a valid Double.");
  }

  public void testInvalidDoubleWithCommaValue() throws Exception {
    checkInvalidValue(DummyObject.VALUE,
                      "1,5",
                      "'1,5' is not a valid Double.");
  }

  public void testValuesArePreservedWhenFormValidationFails() throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObject.TYPE)
          .add(DummyObject.ID)
          .add(DummyObject.NAME)
          .add(DummyObject.DATE)
          .create(componentId);
      }
    });

    form.<HtmlInput>getInputByName("component_id").setValueAttribute("1");
    form.<HtmlInput>getInputByName("component_name").setValueAttribute("name 1");
    form.<HtmlInput>getInputByName("component_date").setValueAttribute("not a date!");
    HtmlPage newPage = (HtmlPage)form.<HtmlInput>getInputByName("submit").click();

    checkMessages(newPage, "'not a date!' is not a valid Date.");

    HtmlForm newForm = (HtmlForm)newPage.getHtmlElementById(getComponentId());
    assertEquals("name 1", newForm.<HtmlInput>getInputByName("component_name").getValueAttribute());
    assertEquals("not a date!", newForm.<HtmlInput>getInputByName("component_date").getValueAttribute());

    assertFalse(repository.contains(DummyObject.TYPE));

    newForm.<HtmlInput>getInputByName("component_date").setValueAttribute("25/12/2007");
    HtmlPage lastPage = (HtmlPage)newForm.<HtmlInput>getInputByName("submit").click();
    assertNoMessages(lastPage);

    Glob createdObject = repository.get(Key.create(DummyObject.TYPE, 1));
    assertEquals("name 1", createdObject.get(DummyObject.NAME));
    assertEquals(Dates.parse("2007/12/25"), createdObject.get(DummyObject.DATE));
  }

  private void checkInvalidValue(final Field field, String value, String errorMessage) throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(field.getGlobType())
          .add(field)
          .create(componentId);
      }
    });

    form.<HtmlInput>getInputByName("component_" + field.getName()).setValueAttribute(value);
    HtmlPage newPage = (HtmlPage)form.<HtmlInput>getInputByName("submit").click();

    checkMessages(newPage, errorMessage);

    assertFalse(repository.contains(field.getGlobType()));
  }

  private void checkDateFormat(String input, String expectedOutput) throws Exception {
    HtmlForm form = (HtmlForm)renderComponent(new ComponentFactory() {
      public Component create(String componentId, GlobRepositoryLoader repositoryLoader) {
        return GlobFormBuilder.init(DummyObject.TYPE)
          .add(DummyObject.ID)
          .add(DummyObject.DATE)
          .create(componentId);
      }
    });

    form.<HtmlInput>getInputByName("component_id").setValueAttribute("1");
    form.<HtmlInput>getInputByName("component_date").setValueAttribute(input);
    form.<HtmlInput>getInputByName("submit").click();

    assertNoMessages(form);

    Glob createdObject = repository.get(Key.create(DummyObject.TYPE, 1));
    assertEquals(Dates.parse(expectedOutput), createdObject.get(DummyObject.DATE));
  }
}
