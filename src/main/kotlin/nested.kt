package com.danstutzman.kotlinc

sealed class Nested {
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

    abstract fun getType(): Type
  }

  data class Method(
    val name: String,
    val paramTypes: List<Type>,
    val accessFlags: AccessFlags,
    val returnType: Type,
    val returnExpr: Expr
  ): Nested() {
    override fun toString(): String =
      "Method $name ${paramTypes} $accessFlags\n" +
        "    $returnExpr"
  }

  data class Class(
    val name: String,
    val parentPath: String,
    val methods: List<Method>
  ): Nested() {
    override fun toString(): String =
      "Class $name $parentPath\n" +
        methods.map { it -> "  ${it}" }.joinToString("\n")
  }
}
