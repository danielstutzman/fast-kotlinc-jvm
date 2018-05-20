package com.danstutzman.kotlinc

sealed class Node {
  data class Class(
    val name: String,
    val parentPath: String,
    val methods: Array<Method>
  ): Node() {
    override fun toString(): String =
      "Class $name $parentPath\n" +
        methods.map { it -> "  $it" }.joinToString("\n")
  }

  data class Method(
    val name: String,
    val type: String,
    val access: Int,
    val statements: Array<Expr>
  ): Node() {
    override fun toString(): String =
      "Method $name $type $access\n" +
        statements.map { it -> "    $it" }.joinToString("\n")
  }

  sealed class Expr {
    data class Identifier(
      val s: String
    ): Expr() {
      override fun toString(): String = s
    }

    data class InvokeSuper(
      val args: Array<Expr>
    ): Expr() {
      override fun toString(): String = "super(...)" +
        args.map { it -> "      $it" }.joinToString("\n")
    }

    data class Field(
      val name: String,
      val receiver: Expr
    ): Expr() {
      override fun toString(): String = "Field $name of" +
        "\n        $receiver"
    }

    data class Call(
      val name: String,
      val receiver: Expr,
      val args: Array<Expr>
    ): Expr() {
      override fun toString(): String = "Call $name on" +
        "\n      $receiver" +
        args.map { it -> "\n      $it" }.joinToString()
    }

    data class StringConstant(
      val s: String
    ): Expr() {
      override fun toString(): String = "\"$s\""
    }
  }
}
