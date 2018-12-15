package com.danstutzman.kotlinc

import com.github.sarahbuisson.kotlinparser.KotlinParser
import com.github.sarahbuisson.kotlinparser.KotlinParserBaseVisitor

interface Ast
data class FileContents(val child: Ast): Ast
data class FunDec(val name: String, val returnExpr: Ast): Ast
data class Sequence(val exprs: List<Ast>): Ast
data class Call(val methodName: String, val arg0: Ast): Ast
data class StringConstant(val s: String): Ast
data class Plus(val child1: Ast, val child2: Ast): Ast

class ToAstVisitor: KotlinParserBaseVisitor<Ast>() {
  override fun visitKotlinFile(ctx: KotlinParser.KotlinFileContext): Ast {
    val child = visit(ctx.topLevelObject().single())
    return FileContents(child)
  }

  override fun visitTopLevelObject(
    ctx: KotlinParser.TopLevelObjectContext
  ): Ast = visit(ctx.functionDeclaration())

  override fun visitFunctionDeclaration(
    ctx: KotlinParser.FunctionDeclarationContext
  ): Ast {
    val name = ctx.identifier().getText()
    val child = visit(ctx.functionBody())
    return FunDec(name, child)
  }

  override fun visitFunctionBody(
    ctx: KotlinParser.FunctionBodyContext
  ): Ast = visit(ctx.block())

  override fun visitBlock(ctx: KotlinParser.BlockContext): Ast {
    if (ctx.statement().size == 1) {
      return visit(ctx.statement().single())
    } else {
      return Sequence(ctx.statement().map { visit(it) })
    }
  }

  override fun visitStatement(ctx: KotlinParser.StatementContext): Ast =
    visit(ctx.expression())

  override fun visitExpression(ctx: KotlinParser.ExpressionContext): Ast =
    visit(ctx.disjunction())

  override fun visitDisjunction(ctx: KotlinParser.DisjunctionContext): Ast =
    visit(ctx.conjunction().single())

  override fun visitConjunction(ctx: KotlinParser.ConjunctionContext): Ast =
    visit(ctx.equality().single())

  override fun visitEquality(ctx: KotlinParser.EqualityContext): Ast =
    visit(ctx.comparison().single())

  override fun visitComparison(ctx: KotlinParser.ComparisonContext): Ast =
    visit(ctx.infixOperation().single())

  override fun visitInfixOperation(
    ctx: KotlinParser.InfixOperationContext
  ): Ast = visit(ctx.elvisExpression().single())

  override fun visitElvisExpression(
    ctx: KotlinParser.ElvisExpressionContext
  ): Ast = visit(ctx.infixFunctionCall().single())

  override fun visitInfixFunctionCall(
    ctx: KotlinParser.InfixFunctionCallContext
  ): Ast = visit(ctx.rangeExpression().single())

  override fun visitRangeExpression(
    ctx: KotlinParser.RangeExpressionContext
  ): Ast = visit(ctx.additiveExpression().single())

  // : multiplicativeExpression (additiveOperator NL* multiplicativeExpression)*
  override fun visitAdditiveExpression(
    ctx: KotlinParser.AdditiveExpressionContext
  ): Ast {
    var out = visit(ctx.multiplicativeExpression(0))
    for (i in 1..ctx.multiplicativeExpression().size - 1) {
      out = Plus(out, visit(ctx.multiplicativeExpression(i)))
    }
    return out
  }

  override fun visitMultiplicativeExpression(
    ctx: KotlinParser.MultiplicativeExpressionContext
  ): Ast = visit(ctx.asExpression().single())

  override fun visitAsExpression(
    ctx: KotlinParser.AsExpressionContext
  ): Ast = visit(ctx.prefixUnaryExpression())

  override fun visitPrefixUnaryExpression(
    ctx: KotlinParser.PrefixUnaryExpressionContext
  ): Ast = visit(ctx.postfixUnaryExpression())

  override fun visitPostfixUnaryExpression(
    ctx: KotlinParser.PostfixUnaryExpressionContext
  ): Ast {
    if (ctx.assignableExpression() != null) {
      return visit(ctx.assignableExpression())
    } else if (ctx.callExpression() != null) {
      return visit(ctx.callExpression())
    } else {
      throw RuntimeException("Unknown postfixUnaryExpression type")
    }
  }

  override fun visitCallExpression(
    ctx: KotlinParser.CallExpressionContext
  ): Ast {
    val methodName = ctx.assignableExpression().getText()
    if (methodName == "println") {
      val child = visit(ctx.valueArguments())
      println("callExpression $methodName $child")
      return Call(methodName, child)
    } else {
      throw RuntimeException("Unknown methodName $methodName")
    }
  }

  override fun visitValueArguments(
    ctx: KotlinParser.ValueArgumentsContext
  ): Ast = visit(ctx.valueArgument().single())

  override fun visitValueArgument(
    ctx: KotlinParser.ValueArgumentContext
  ): Ast = visit(ctx.expression())

  override fun visitAssignableExpression(
    ctx: KotlinParser.AssignableExpressionContext
  ): Ast = visit(ctx.primaryExpression())

  override fun visitPrimaryExpression(
    ctx: KotlinParser.PrimaryExpressionContext
  ): Ast {
    if (ctx.stringLiteral() == null) {
      throw RuntimeException("Unexpected primaryExpression ${ctx.getText()}")
    }
    return visit(ctx.stringLiteral())
  }

  override fun visitStringLiteral(
    ctx: KotlinParser.StringLiteralContext
  ): Ast = visit(ctx.lineStringLiteral())

  override fun visitLineStringLiteral(
    ctx: KotlinParser.LineStringLiteralContext
  ): Ast = visit(ctx.lineStringContent().single())

  override fun visitLineStringContent(
    ctx: KotlinParser.LineStringContentContext
  ): Ast {
    if (ctx.LineStrText() == null) {
      throw RuntimeException("Can't handle LineStrEscapedChar or LineStrRef")
    }
    return StringConstant(ctx.LineStrText().getText())
  }
}
