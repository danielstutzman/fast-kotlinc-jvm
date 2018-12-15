package com.danstutzman.kotlinc.asm

import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

fun serializeClass(c: Class): ByteArray {
  val cw = ClassWriter(ClassWriter.COMPUTE_FRAMES)
  cw.visit(Opcodes.V1_5,
    Opcodes.ACC_PUBLIC + Opcodes.ACC_FINAL + Opcodes.ACC_SUPER,
    c.name, null, c.parentPath, null)
  for (method in c.methods) {
    serializeMethod(method, cw)
  }
  cw.visitEnd()
  return cw.toByteArray()
}

fun serializeMethod(m: Method, cw: ClassWriter) {
  val access = Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC + Opcodes.ACC_FINAL
  val mw = cw.visitMethod(access, m.name, m.descriptor, null, null)
  for (i9n in m.i9ns) {
    serializeI9n(i9n, mw)
  }
  mw.visitMaxs(0, 0) // computes automatically
  mw.visitEnd()
}

fun serializeI9n(i: I9n, mw: MethodVisitor) =
  when (i) {
    is I9n.Areturn -> mw.visitInsn(Opcodes.ARETURN)
    is I9n.Return -> mw.visitInsn(Opcodes.RETURN)
    is I9n.LdcClasspath -> mw.visitLdcInsn(i.classPath)
    is I9n.LdcString -> mw.visitLdcInsn(i.string)
    is I9n.Aload0 -> mw.visitVarInsn(Opcodes.ALOAD, 0)
    is I9n.Getstatic ->
      mw.visitFieldInsn(Opcodes.GETSTATIC, i.classPath, i.fieldName,
        i.fieldType)
    is I9n.Invokespecial ->
      mw.visitMethodInsn(Opcodes.INVOKESPECIAL, i.classPath, i.methodName,
        i.methodType, false)
    is I9n.Invokevirtual ->
      mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL, i.classPath, i.methodName,
        i.methodType, false)
    is I9n.New -> mw.visitTypeInsn(Opcodes.NEW, i.classPath)
    is I9n.Dup -> mw.visitInsn(Opcodes.DUP)
    // else -> throw RuntimeException("Unknown instruction ${i::class}")
  }
