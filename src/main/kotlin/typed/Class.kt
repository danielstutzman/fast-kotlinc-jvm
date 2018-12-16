package com.danstutzman.kotlinc.typed

data class Class(
  val name: String,
  val parentPath: String,
  val methods: List<Method>
) {
  override fun toString(): String =
    "Class $name $parentPath\n" +
      methods.map { it -> "  ${it}" }.joinToString("\n")
}
