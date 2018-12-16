package com.danstutzman.kotlinc.typed

sealed class Type {
  object StringType: Type() {
    override fun toString(): String = "S"
    override fun toDescriptor(): String = "Ljava/lang/String;"
  }

  data class Array(val subtype: Type): Type() {
    override fun toString(): String = "A<$subtype>"
    override fun toDescriptor(): String = "[" + subtype.toDescriptor()
  }

  object VoidType: Type() {
    override fun toString(): String = "V"
    override fun toDescriptor(): String = "V"
  }

  override abstract fun toString(): String
  abstract fun toDescriptor(): String
}
