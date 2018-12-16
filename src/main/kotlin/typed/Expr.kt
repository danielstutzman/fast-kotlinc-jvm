package com.danstutzman.kotlinc.typed

import com.danstutzman.kotlinc.AccessFlags

sealed class Expr {
  object SuperInConstructor: Expr() {
    override fun toString(): String = "Super"
    override fun getType() = Type.VoidType
  }

  data class InvokeSpecial(
    val classPath: String,
    val objectExpr: Expr,
    val methodName: String,
    val methodType: String,
    @get:JvmName("getType_") val type: Type
  ): Expr() {
    override fun toString(): String =
      "InvokeSpecial $classPath $methodName $methodType" +
        "\n      $objectExpr"
    override fun getType() = type
  }

  data class Class(
    val classPath: String,
    @get:JvmName("getType_") val type: Type
  ): Expr() {
    override fun toString(): String = "Class $classPath"
    override fun getType() = Type.VoidType
  }

  data class Field(
    val classPath: String,
    val fieldName: String,
    val fieldType: String,
    @get:JvmName("getType_") val type: Type
  ): Expr() {
    override fun toString(): String =
      "Field $classPath $fieldName $fieldType"
    override fun getType() = Type.VoidType
  }

  data class ConstantString(
    val string: String
  ): Expr() {
    override fun toString(): String = "ConstantString \"$string\""
    override fun getType() = Type.StringType
  }

  data class InvokeVirtual(
    val classPath: String,
    val objectExpr: Expr,
    val methodName: String,
    val methodType: String,
    @get:JvmName("getType_") val type: Type,
    val args: List<Expr>
  ): Expr() {
    override fun toString(): String =
      "InvokeVirtual $classPath $methodName $methodType\n" +
        "      $objectExpr" +
        args.map { it -> "\n      $it" }.joinToString()
    override fun getType() = type
  }

  data class Sequence(val exprs: List<Expr>): Expr() {
    override fun toString(): String =
      "Sequence\n" +
        exprs.map { it -> "\n      $it" }.joinToString()
    override fun getType() =
      if (exprs.size > 0) exprs.last().getType() else Type.VoidType
  }

  data class New(val classPath: String): Expr() {
    override fun getType() = Type.VoidType
  }

  data class AppendString(
    val expr1: Expr,
    val expr2: Expr
  ): Expr() {
    override fun getType() = Type.StringType
  }

  abstract fun getType(): Type
}
