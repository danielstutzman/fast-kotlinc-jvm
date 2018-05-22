package com.danstutzman.kotlinc.decompile

import org.objectweb.asm.Handle
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MethodPrinter(
  val access: Int,
  val name: String,
  val desc: String
): MethodVisitor(Opcodes.ASM5) {
  private val i9ns = mutableListOf<I9n>()

  fun getMethod() = Method(access, name, desc, i9ns)

  override fun visitFieldInsn(
      opcode:Int, owner:String, name:String, descriptor:String) {
    if (opcode == Opcodes.GETSTATIC) {
      i9ns.add(Getstatic(owner, name, descriptor))
    } else {
      throw RuntimeException("Can't visitFieldInsn for opcode $opcode")
    }
  }

  override fun visitInsn(opcode: Int) {
    if (opcode == Opcodes.RETURN) {
      i9ns.add(Return)
    } else {
      throw RuntimeException("Can't visitIsn for opcode $opcode")
    }
  }

  override fun visitIntInsn(opcode: Int, operand: Int) =
    throw RuntimeException("Can't visitIntInsn")

  override fun visitLdcInsn(cst: Any) {
    if (cst is String) {
      i9ns.add(Ldc(cst))
    } else {
      throw RuntimeException("Can't visitLdcInsn for type ${cst::class}")
    }
  }

  override fun visitInvokeDynamicInsn(name: String, descriptor: String,
      bootstrapMethodHandle: Handle, bootstrapMethodArguments: Array<Any>) =
    throw RuntimeException("Can't visitInvokeDynamicInsn")

  override fun visitJumpInsn(opcode: Int, label: Label) =
    throw RuntimeException("Can't visitJumpInsn")

  override fun visitIincInsn(var_: Int, increment: Int) =
    throw RuntimeException("Can't visitIincInsn")

  override fun visitMethodInsn(opcode: Int, owner: String, name: String,
      desc: String, itf: Boolean) {
    if (itf) {
      throw RuntimeException("visitMethodInsn can't handle itf=true")
    }

    if (opcode == Opcodes.INVOKESPECIAL) {
      i9ns.add(Invokespecial(owner, name, desc))
    } else if (opcode == Opcodes.INVOKEVIRTUAL) {
      i9ns.add(Invokevirtual(owner, name, desc))
    } else {
      throw RuntimeException("Can't visitMethodInsn for opcode $opcode")
    }
  }

  override fun visitMultiANewArrayInsn(descriptor: String, numDimensions: Int) =
    throw RuntimeException("Can't visitMultiANewArrayInsn")

  override fun visitTableSwitchInsn(min: Int, max: Int, dflt: Label,
      labels: Array<Label>) =
    throw RuntimeException("Can't visitTableSwitchInsn")

  override fun visitTryCatchBlock(start:Label, end:Label, handler:Label,
      type:String) =
    throw RuntimeException("Can't visitTryCatchBlock")

  override fun visitTypeInsn(opcode:Int, type:String) =
    throw RuntimeException("Can't visitTypeInsn")

  override fun visitVarInsn(opcode: Int, var_: Int) {
    if (opcode == Opcodes.ALOAD) {
      i9ns.add(Aload(var_))
    } else if (opcode == Opcodes.ASTORE) {
      i9ns.add(Astore(var_))
    } else {
      throw RuntimeException("Can't visitVarIsn for opcode $opcode")
    }
  }
}
