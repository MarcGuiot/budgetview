package org.globs.samples.blog;

import org.globs.samples.blog.gui.BlogApplication;
import org.uispec4j.*;
import org.uispec4j.interception.*;

public class BlogApplicationTest extends UISpecTestCase {
  private Table draftPosts;
  private Table publishedPosts;
  private Window window;
  private Button publishButton;

  protected void setUp() throws Exception {
    setAdapter(new MainClassAdapter(BlogApplication.class));
    window = getMainWindow();
    draftPosts = window.getTable("draftPosts");
    publishedPosts = window.getTable("publishedPosts");
    publishButton = window.getButton("Publish");
  }

  public void testCreatingANewPost() throws Exception {
    assertThat(draftPosts.getHeader().contentEquals("Title", "Category"));
    assertThat(draftPosts.isEmpty());

    assertThat(publishedPosts.getHeader().contentEquals("Title", "Category"));
    assertThat(publishedPosts.isEmpty());

    createNewPost("Hello world");

    assertThat(draftPosts.contentEquals(new Object[][]{
            {"Hello world", ""}
    }));
    assertThat(publishedPosts.isEmpty());

    TextBox titleField = window.getInputTextBox("titleField");
    titleField.setText("Hello, world!");
    assertThat(draftPosts.contentEquals(new Object[][]{
            {"Hello, world!", ""}
    }));
  }

  public void testPublishingAPost() throws Exception {
    createNewPost("Post 1");
    createNewPost("Post 2");
    assertThat(draftPosts.contentEquals(new Object[][]{
            {"Post 1", ""},
            {"Post 2", ""}
    }));
    draftPosts.selectRow(0);
    publishButton.click();
    assertThat(draftPosts.contentEquals(new Object[][]{
            {"Post 2", ""}
    }));
    assertThat(publishedPosts.contentEquals(new Object[][]{
            {"Post 1", ""}
    }));
  }

  private void createNewPost(final String title) {
    Button newPostButton = window.getButton("New post");
    WindowInterceptor
            .init(newPostButton.triggerClick())
            .process(new WindowHandler() {
              public Trigger process(Window dialog) throws Exception {
                dialog.getInputTextBox().setText(title);
                return dialog.getButton("OK").triggerClick();
              }
            })
            .run();
  }
}
