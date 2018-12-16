package com.danstutzman.kotlinc.ast

interface Ast
data class FileContents(val child: Ast): Ast
data class FunDec(val name: String, val returnExpr: Ast): Ast
data class Sequence(val exprs: List<Ast>): Ast
data class Call(val methodName: String, val arg0: Ast): Ast
data class StringConstant(val s: String): Ast
data class Plus(val child1: Ast, val child2: Ast): Ast
