package com.danstutzman.kotlinc

sealed class Nested {
  sealed class Expr {
    object SuperInConstructor: Expr() {
      override fun toString(): String = "Super"
    }

    data class InvokeSpecial(
      val classPath: String,
      val objectExpr: Expr,
      val methodName: String,
      val methodType: String
    ): Expr() {
      override fun toString(): String =
        "InvokeSpecial $classPath $methodName $methodType" +
          "\n      $objectExpr"
    }

    data class Class(
      val classPath: String
    ): Expr() {
      override fun toString(): String = "Class $classPath"
    }

    data class Field(
      val classPath: String,
      val fieldName: String,
      val fieldType: String
    ): Expr() {
      override fun toString(): String =
        "Field $classPath $fieldName $fieldType"
    }

    data class ConstantString(
      val string: String
    ): Expr() {
      override fun toString(): String = "ConstantString \"$string\""
    }

    data class InvokeVirtual(
      val classPath: String,
      val objectExpr: Expr,
      val methodName: String,
      val methodType: String,
      val args: Array<Expr>
    ): Expr() {
      override fun toString(): String =
        "InvokeVirtual $classPath $methodName $methodType\n" +
          "      $objectExpr" +
          args.map { it -> "\n      $it" }.joinToString()
    }
  }

  data class Method(
    val name: String,
    val paramTypes: List<Type>,
    val accessFlags: AccessFlags,
    val returnExpr: Expr
  ): Nested() {
    override fun toString(): String =
      "Method $name ${paramTypes} $accessFlags\n" +
        "    $returnExpr"
  }

  data class Class(
    val name: String,
    val parentPath: String,
    val methods: Array<Method>
  ): Nested() {
    override fun toString(): String =
      "Class $name $parentPath\n" +
        methods.map { it -> "  ${it}" }.joinToString("\n")
  }
}
