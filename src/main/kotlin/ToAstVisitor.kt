package kotlinc

import com.github.sarahbuisson.kotlinparser.KotlinParser
import com.github.sarahbuisson.kotlinparser.KotlinParserBaseVisitor

interface Node
data class KotlinFile(val node: Node): Node
data class Fun(val name: String, val child: Node): Node
data class Call(val methodName: String, val child: Node): Node
data class StringConstant(val s: String): Node

class ToAstVisitor: KotlinParserBaseVisitor<Node>() {
  override fun visitKotlinFile(ctx: KotlinParser.KotlinFileContext): Node {
    val child = visit(ctx.topLevelObject().single())
    println("kotlinFile $child")
    return KotlinFile(child)
  }

  override fun visitTopLevelObject(
    ctx: KotlinParser.TopLevelObjectContext
  ): Node = visit(ctx.functionDeclaration())

  override fun visitFunctionDeclaration(
    ctx: KotlinParser.FunctionDeclarationContext
  ): Node {
    val name = ctx.identifier().getText()
    val child = visit(ctx.functionBody())
    return Fun(name, child)
  }

  override fun visitFunctionBody(
    ctx: KotlinParser.FunctionBodyContext
  ): Node = visit(ctx.block())

  override fun visitBlock(ctx: KotlinParser.BlockContext): Node =
    visit(ctx.statement().single())

  override fun visitStatement(ctx: KotlinParser.StatementContext): Node =
    visit(ctx.expression())

  override fun visitExpression(ctx: KotlinParser.ExpressionContext): Node =
    visit(ctx.disjunction())

  override fun visitDisjunction(ctx: KotlinParser.DisjunctionContext): Node =
    visit(ctx.conjunction().single())

  override fun visitConjunction(ctx: KotlinParser.ConjunctionContext): Node =
    visit(ctx.equality().single())

  override fun visitEquality(ctx: KotlinParser.EqualityContext): Node =
    visit(ctx.comparison().single())

  override fun visitComparison(ctx: KotlinParser.ComparisonContext): Node =
    visit(ctx.infixOperation().single())

  override fun visitInfixOperation(
    ctx: KotlinParser.InfixOperationContext
  ): Node = visit(ctx.elvisExpression().single())

  override fun visitElvisExpression(
    ctx: KotlinParser.ElvisExpressionContext
  ): Node = visit(ctx.infixFunctionCall().single())

  override fun visitInfixFunctionCall(
    ctx: KotlinParser.InfixFunctionCallContext
  ): Node = visit(ctx.rangeExpression().single())

  override fun visitRangeExpression(
    ctx: KotlinParser.RangeExpressionContext
  ): Node = visit(ctx.additiveExpression().single())

  override fun visitAdditiveExpression(
    ctx: KotlinParser.AdditiveExpressionContext
  ): Node = visit(ctx.multiplicativeExpression().single())

  override fun visitMultiplicativeExpression(
    ctx: KotlinParser.MultiplicativeExpressionContext
  ): Node = visit(ctx.asExpression().single())

  override fun visitAsExpression(
    ctx: KotlinParser.AsExpressionContext
  ): Node = visit(ctx.prefixUnaryExpression())

  override fun visitPrefixUnaryExpression(
    ctx: KotlinParser.PrefixUnaryExpressionContext
  ): Node = visit(ctx.postfixUnaryExpression())

  override fun visitPostfixUnaryExpression(
    ctx: KotlinParser.PostfixUnaryExpressionContext
  ): Node {
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
  ): Node {
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
  ): Node = visit(ctx.valueArgument().single())

  override fun visitValueArgument(
    ctx: KotlinParser.ValueArgumentContext
  ): Node = visit(ctx.expression())

  override fun visitAssignableExpression(
    ctx: KotlinParser.AssignableExpressionContext
  ): Node = visit(ctx.primaryExpression())

  override fun visitPrimaryExpression(
    ctx: KotlinParser.PrimaryExpressionContext
  ): Node {
    if (ctx.stringLiteral() == null) {
      throw RuntimeException("Unexpected primaryExpression ${ctx.getText()}")
    }
    return visit(ctx.stringLiteral())
  }

  override fun visitStringLiteral(
    ctx: KotlinParser.StringLiteralContext
  ): Node = visit(ctx.lineStringLiteral())

  override fun visitLineStringLiteral(
    ctx: KotlinParser.LineStringLiteralContext
  ): Node = visit(ctx.lineStringContent().single())

  override fun visitLineStringContent(
    ctx: KotlinParser.LineStringContentContext
  ): Node {
    if (ctx.LineStrText() == null) {
      throw RuntimeException("Can't handle LineStrEscapedChar or LineStrRef")
    }
    return StringConstant(ctx.LineStrText().getText())
  }
}
