package com.danstutzman.kotlinc.decompile

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.Attribute
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

interface I9n

object Return: I9n

data class Aload(val var_: Int): I9n

data class Astore(val var_: Int): I9n

data class Ldc(val s: String): I9n

data class Getstatic(
  val owner: String,
  val name: String,
  val descriptor: String): I9n

data class Invokespecial(
  val owner: String,
  val name: String,
  val desc: String): I9n

data class Invokevirtual(
  val owner: String,
  val name: String,
  val desc: String): I9n

data class Invokestatic(
  val owner: String,
  val name: String,
  val desc: String): I9n

data class Method(
  val access: Int,
  val name: String,
  val desc: String,
  val i9ns: List<I9n>
) {
  override fun toString(): String = name +
    i9ns.map { "\n    $it" }.joinToString("")
}

data class Class(
  val name: String,
  val superName: String,
  val methods: Set<Method>
) {
  override fun toString(): String = "$name $superName" +
    methods.toList().sortedBy { it.name }.map{ "\n  $it" }.joinToString("")
}

class ClassPrinter: ClassVisitor(Opcodes.ASM5) {
  var name: String? = null
  var superName: String? = null

  val methodPrinters = mutableListOf<MethodPrinter>()

  fun getDecompiledClass(): Class {
    val methods = methodPrinters.map { it.getMethod() }.toSet()
    return Class(name!!, superName!!, methods)
  }

	override fun visit(version: Int, access: Int, name: String,
		signature: String?, superName: String, interfaces: Array<String>) {
    this.name = name
    this.superName = superName
	}

	override fun visitSource(source: String, debug: String?) {}
	override fun visitOuterClass(owner: String, name: String, desc: String) {}
	override fun visitAnnotation(
    desc: String, visible: Boolean): AnnotationVisitor? = null
	override fun visitAttribute(attr: Attribute) {}
	override fun visitInnerClass(
    name: String, outerName: String, innerName: String, access: Int) {}

	override fun visitField(access: Int, name: String, desc: String,
    signature: String, value: Any): FieldVisitor? = null

	override fun visitMethod(
      access: Int, name: String, desc: String, signature: String?,
      exceptions: Array<String>?): MethodVisitor? {
    if (signature != null) {
      throw RuntimeException("visitMethod can't handle non-null signature")
    }

		val printer = MethodPrinter(access, name, desc)
    methodPrinters.add(printer)
    return printer
	}
}
