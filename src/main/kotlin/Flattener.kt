package com.danstutzman.kotlinc

import java.util.SortedMap
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun flattenClass(class_: Nested.Class): ByteArray {
  val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
  cw.visit(Opcodes.V1_5,
    Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
    class_.name, null, class_.parentPath, null)
  for (method in class_.methods) {
    flattenMethod(method, cw)
  }
  cw.visitEnd()
  return cw.toByteArray()
}

fun flattenMethod(method: Nested.Method, cw: ClassWriter) {
  val descriptor = "(" +
    method.paramTypes.map { it.toDescriptor() }.joinToString("") +
    ")" + "V"
  val access = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL
  val mw = cw.visitMethod(access, method.name, descriptor, null, null)
  flattenI9n(method.returnExpr, mw)
  mw.visitInsn(Opcodes.RETURN)
  mw.visitMaxs(0, 0) // computes automatically
  mw.visitEnd()
}

fun flattenI9n(i9n: Nested.Expr, mw: MethodVisitor) {
  if (i9n is Nested.Expr.Class) {
    mw.visitLdcInsn(i9n.classPath)

  } else if (i9n is Nested.Expr.ConstantString) {
    mw.visitLdcInsn(i9n.string)

  } else if (i9n == Nested.Expr.SuperInConstructor) {
    mw.visitVarInsn(Opcodes.ALOAD, 0)

  } else if (i9n is Nested.Expr.Field) {
    mw.visitFieldInsn(Opcodes.GETSTATIC, i9n.classPath, i9n.fieldName,
      i9n.fieldType)

  } else if (i9n is Nested.Expr.InvokeSpecial) {
    flattenI9n(i9n.objectExpr, mw)
    mw.visitMethodInsn(Opcodes.INVOKESPECIAL, i9n.classPath, i9n.methodName,
      i9n.methodType, false)

  } else if (i9n is Nested.Expr.InvokeVirtual) {
    flattenI9n(i9n.objectExpr, mw)
    i9n.args.forEach { flattenI9n(it, mw) }
    mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, i9n.classPath, i9n.methodName,
      i9n.methodType, false)

  } else if (i9n is Nested.Expr.Sequence) {
    i9n.exprs.forEach { flattenI9n(it, mw) }

  } else {
    throw RuntimeException("Unknown instruction ${i9n::class}")
  }
}
