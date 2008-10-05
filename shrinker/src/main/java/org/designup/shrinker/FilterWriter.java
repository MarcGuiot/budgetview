package org.designup.shrinker;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class FilterWriter extends ClassWriter {
  public FilterWriter() {
    super(0);
  }

  public MethodVisitor visitMethod(int i, String s, String s1, String s2, String[] strings) {
    MethodVisitor methodVisitor = super.visitMethod(i, s, s1, s2, strings);
    return new FilterMethodAdapter(methodVisitor);
  }

  private static class FilterMethodAdapter extends MethodAdapter {
    private MethodVisitor tmpMv;

    public FilterMethodAdapter(MethodVisitor methodVisitor) {
      super(methodVisitor);
    }

    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      if (opcode == Opcodes.INVOKESTATIC) {
        if (name.equals("beginRemove")) {
          tmpMv = mv;
          mv = new EmptyVisitor();
        }
        else if (name.equals("endRemove")) {
          mv = tmpMv;
        }
      }
      super.visitMethodInsn(opcode, owner, name, desc);
    }
  }
}
