package com.danstutzman.kotlinc.asm

data class Class(
  val name: String,
  val parentPath: String,
  val methods: List<Method>
)
