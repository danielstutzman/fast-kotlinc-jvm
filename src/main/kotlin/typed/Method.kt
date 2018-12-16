package com.danstutzman.kotlinc.typed

import com.danstutzman.kotlinc.AccessFlags
import com.danstutzman.kotlinc.Type

data class Method(
  val name: String,
  val paramTypes: List<Type>,
  val accessFlags: AccessFlags,
  val returnType: Type,
  val returnExpr: Expr
) {
  override fun toString(): String =
    "Method $name ${paramTypes} $accessFlags\n" +
      "    $returnExpr"
}
