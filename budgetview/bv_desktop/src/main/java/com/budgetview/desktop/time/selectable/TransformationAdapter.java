package com.budgetview.desktop.time.selectable;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.Stack;

public class TransformationAdapter implements Transformation {
  private AffineTransform transform = new AffineTransform();
  private Graphics2D graphics2D;
  private Stack<AffineTransform> graphicsTransforms = new Stack<AffineTransform>();
  private Stack<AffineTransform> transforms = new Stack<AffineTransform>();

  public TransformationAdapter(Graphics2D graphics2D) {
    this.graphics2D = graphics2D;
  }

  public void translate(double x, double y) {
    transform.translate(x, y);
    graphics2D.translate(x, y);
  }

  public AffineTransform getTransform() {
    return transform;
  }

  public void save() {
    graphicsTransforms.push(graphics2D.getTransform());
    transforms.push(new AffineTransform(transform));
  }

  public void restore() {
    graphics2D.setTransform(graphicsTransforms.pop());
    transform = transforms.pop();
  }

}
