package com.danstutzman.kotlinc

import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.util.SortedMap

sealed class Bytecode {
  sealed class Instruction {
    // Load reference from local variable
    // The index is an unsigned byte that must be an index into the local variable array of the current frame (§2.6). The local variable at index must contain a reference. The objectref in the local variable at index is pushed onto the operand stack.
    data class aload(
      val localIndex: Int
    ): Instruction() {
      override fun toString(): String = "aload L$localIndex"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(25)
        stream.writeByte(localIndex)
      }
    }

    data class invokespecial(
      val indexToMethod: Int
    ): Instruction() {
      override fun toString(): String = "invokespecial #$indexToMethod"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(183)
        stream.writeShort(indexToMethod)
      }
    }

    // Return void from method
    object return_: Instruction() {
      override fun toString(): String = "return"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(177)
      }
    }

    // Get static field from class
    data class getstatic(
      val indexToField: Int
    ): Instruction() {
      override fun toString(): String = "getstatic #$indexToField"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(178)
        stream.writeShort(indexToField)
      }
    }

    // Push item from run-time constant pool
    data class ldc(
      val index: Int
    ): Instruction() {
      override fun toString(): String = "ldc #$index"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(18)
        stream.writeByte(index)
      }
    }

    // Invoke instance method; dispatch based on class
    data class invokevirtual(
      val indexToMethod: Int
    ): Instruction() {
      override fun toString(): String = "invokevirtual #$indexToMethod"
      override fun write(stream: DataOutputStream) {
        stream.writeByte(182)
        stream.writeShort(indexToMethod)
      }
    }

    abstract fun write(stream: DataOutputStream)
  }

  data class Method(
    val name: Int, // to pool entry index of type UTF8
    val type: Int, // to pool entry index of type UTF8
    val accessFlags: AccessFlags,
    val codeIndex: Int, // to pool entry index of type UTF8 with value "code"
    val maxStack: Int,
    val maxLocals: Int,
    val instructions: Array<Instruction>
  ): Bytecode() {
    override fun toString(): String =
      "Method $name $type $accessFlags\n" +
        instructions.map { it -> "    ${it}" }.joinToString("\n")
    fun write(stream: DataOutputStream) {
      stream.writeShort(accessFlags.toInt())
      stream.writeShort(name)
      stream.writeShort(type)

      stream.writeShort(1) // num attributes

      stream.writeShort(codeIndex)
      val instructionStream1 = ByteArrayOutputStream()
      val instructionStream2 = DataOutputStream(instructionStream1)
      for (instruction in instructions) {
        instruction.write(instructionStream2)
      }
      val instructionBytes = instructionStream1.toByteArray()

      stream.writeInt(2 + 2 + 4 + instructionBytes.size + 2 + 2)
      stream.writeShort(maxStack)
      stream.writeShort(maxLocals)
      stream.writeInt(instructionBytes.size)
      stream.write(instructionBytes)
      stream.writeShort(0) // exception table length
      stream.writeShort(0) // no attributes
    }
  }

  sealed class Entry {
    data class Utf8(
      val s: String
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(1)
        stream.writeUTF(s)
      }
    }

    data class ClassEntry(
      val name: Int // to utf8 index
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(7)
        stream.writeShort(name)
      }
    }

    data class NameAndType(
      val name: Int, // to utf8 index
      val descriptor: Int // to utf8 index
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(12)
        stream.writeShort(name)
        stream.writeShort(descriptor)
      }
    }

    data class Methodref(
      val class_: Int, // to utf8 index
      val nameAndType: Int
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(10)
        stream.writeShort(class_)
        stream.writeShort(nameAndType)
      }
    }

    data class Fieldref(
      val class_: Int, // to utf8 index
      val nameAndType: Int
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(9)
        stream.writeShort(class_)
        stream.writeShort(nameAndType)
      }
    }

    data class StringEntry(
      val utf8: Int
    ): Entry() {
      override fun write(stream: DataOutputStream) {
        stream.writeByte(8)
        stream.writeShort(utf8)
      }
    }

    abstract fun write(stream: DataOutputStream)
  }

  data class Class(
    val constantPool: SortedMap<Int, Entry>,
    val accessFlags: AccessFlags,
    val thisClass: Int,
    val parentClass: Int,
    val methods: List<Method>
  ): Bytecode() {
    override fun toString(): String =
      "Class #$thisClass #$parentClass\n" +
        constantPool.map { (index, entry) ->
          val paddedIndex = "#$index".padStart(3)
          "  $paddedIndex $entry\n"
        }.joinToString("") +
        methods.map { it -> "  ${it}" }.joinToString("\n")
    fun write(stream: DataOutputStream) {
      stream.writeInt(0xcafebabe.toInt())
      stream.writeShort(0)
      stream.writeShort(52) // version 1.8

      stream.writeShort(constantPool.size + 1)
      for (entry in constantPool.values) {
        entry.write(stream)
      }

      stream.writeShort(accessFlags.toInt())
      stream.writeShort(thisClass)
      stream.writeShort(parentClass)
      stream.writeShort(0) // num interfaces
      stream.writeShort(0) // num fields

      stream.writeShort(methods.size)
      for (method in methods) { method.write(stream) }
      stream.writeShort(0) // num attributes
    }
  }
}
