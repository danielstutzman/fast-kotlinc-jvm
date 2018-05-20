package kotlinc

import java.util.SortedMap

typealias UtfIdx = Int
typealias StringIdx = Int
typealias ClassIdx = Int
typealias NameAndTypeIdx = Int
typealias FieldrefIdx = Int
typealias MethodrefIdx = Int

fun flattenClass(class_: Nested.Class): Bytecode.Class {
  val flattener = Flattener()
  val thisClass = flattener.addClass(flattener.addUtf(class_.name))
  val parentClass = flattener.addClass(flattener.addUtf(class_.parentPath))
  val methods = class_.methods.map { flattener.flattenMethod(it) }
  return Bytecode.Class(
    constantPool = flattener.getConstantPool(),
    accessFlags = AccessFlags(public=true),
    thisClass = thisClass,
    parentClass = parentClass,
    methods = methods
  )
}

class Flattener {
  var nextIdx = 1
  val utfs = HashMap<String, UtfIdx>()
  val strings = HashMap<UtfIdx, StringIdx>()
  val classes = HashMap<UtfIdx, ClassIdx>()
  val nameAndTypes = HashMap<Pair<UtfIdx, UtfIdx>, NameAndTypeIdx>()
  val fieldrefs = HashMap<Pair<ClassIdx, NameAndTypeIdx>, FieldrefIdx>()
	val methodrefs = HashMap<Pair<ClassIdx, NameAndTypeIdx>, MethodrefIdx>()

  fun addUtf(s: String): UtfIdx {
    var utfIdx = utfs.get(s)
    if (utfIdx == null) {
      utfIdx = nextIdx
      utfs.put(s, utfIdx)
      nextIdx += 1
    }
    return utfIdx
  }

  fun addString(utfIdx: UtfIdx): UtfIdx {
    var stringIdx = strings.get(utfIdx)
    if (stringIdx == null) {
      stringIdx = nextIdx
      strings.put(utfIdx, stringIdx)
      nextIdx += 1
    }
    return stringIdx
  }

  fun addClass(utfIdx: UtfIdx): ClassIdx {
    var classIdx = classes.get(utfIdx)
    if (classIdx == null) {
      classIdx = nextIdx
      classes.put(utfIdx, classIdx)
      nextIdx += 1
    }
    return classIdx
  }

  fun addNameAndType(key: Pair<UtfIdx, UtfIdx>): NameAndTypeIdx {
    var nameAndTypeIdx = nameAndTypes.get(key)
    if (nameAndTypeIdx == null) {
      nameAndTypeIdx = nextIdx
      nameAndTypes.put(key, nameAndTypeIdx)
      nextIdx += 1
    }
    return nameAndTypeIdx
  }

  fun addFieldref(key: Pair<ClassIdx, NameAndTypeIdx>): FieldrefIdx {
    var fieldrefIdx = fieldrefs.get(key)
    if (fieldrefIdx == null) {
      fieldrefIdx = nextIdx
      fieldrefs.put(key, fieldrefIdx)
      nextIdx += 1
    }
    return fieldrefIdx
  }

  fun addMethodref(key: Pair<ClassIdx, NameAndTypeIdx>): MethodrefIdx {
    var methodrefIdx = methodrefs.get(key)
    if (methodrefIdx == null) {
      methodrefIdx = nextIdx
      methodrefs.put(key, methodrefIdx)
      nextIdx += 1
    }
    return methodrefIdx
  }

  fun getConstantPool(): SortedMap<Int, Bytecode.Entry> {
    val pool = sortedMapOf<Int, Bytecode.Entry>()
    for ((s, utfIdx) in utfs) {
      pool.put(utfIdx, Bytecode.Entry.Utf8(s))
    }
    for ((utfIdx, stringIdx) in strings) {
      pool.put(stringIdx, Bytecode.Entry.StringEntry(utfIdx))
    }
    for ((utfIdx, classIdx) in classes) {
      pool.put(classIdx, Bytecode.Entry.ClassEntry(utfIdx))
    }
    for ((pair, nameAndTypeIdx) in nameAndTypes) {
      pool.put(nameAndTypeIdx,
        Bytecode.Entry.NameAndType(pair.first, pair.second))
    }
    for ((pair, fieldrefIdx) in fieldrefs) {
      pool.put(fieldrefIdx, Bytecode.Entry.Fieldref(pair.first, pair.second))
    }
    for ((pair, methodrefIdx) in methodrefs) {
      pool.put(methodrefIdx, Bytecode.Entry.Methodref(pair.first, pair.second))
    }
    return pool
  }

  fun flattenMethod(method: Nested.Method): Bytecode.Method {
	  val descriptor = "(" +
      method.paramTypes.map { it.toDescriptor() }.joinToString("") +
      ")" + "V"
		return Bytecode.Method(
			name = addUtf(method.name),
			type = addUtf(descriptor),
			accessFlags = method.accessFlags,
			maxStack = 2, // TODO
			maxLocals = 2, // TODO
      instructions = flattenI9n(method.returnExpr) +
        arrayOf(Bytecode.Instruction.return_),
			codeIndex = addUtf("Code")
		)
	}

  fun flattenI9n(i9n: Nested.Expr): Array<Bytecode.Instruction> {
    if (i9n is Nested.Expr.Class) {
      val classIdx = addClass(addUtf(i9n.classPath))
      return arrayOf(Bytecode.Instruction.ldc(classIdx))

    } else if (i9n is Nested.Expr.ConstantString) {
      return arrayOf(Bytecode.Instruction.ldc(addString(addUtf(i9n.string))))

    } else if (i9n == Nested.Expr.SuperInConstructor) {
      return arrayOf(Bytecode.Instruction.aload(0))

    } else if (i9n is Nested.Expr.Field) {
      val classIdx = addClass(addUtf(i9n.classPath))
      val nameAndTypeIdx =
        addNameAndType(Pair(addUtf(i9n.fieldName), addUtf(i9n.fieldType)))
      val fieldrefIdx = addFieldref(Pair(classIdx, nameAndTypeIdx))
      return arrayOf(Bytecode.Instruction.getstatic(fieldrefIdx))

    } else if (i9n is Nested.Expr.InvokeSpecial) {
      val classIdx = addClass(addUtf(i9n.classPath))
      val nameAndTypeIdx =
        addNameAndType(Pair(addUtf(i9n.methodName), addUtf(i9n.methodType)))
      val methodrefIdx = addMethodref(Pair(classIdx, nameAndTypeIdx))
      return flattenI9n(i9n.objectExpr) +
        arrayOf(Bytecode.Instruction.invokespecial(methodrefIdx))

    } else if (i9n is Nested.Expr.InvokeVirtual) {
      val classIdx = addClass(addUtf(i9n.classPath))
      val nameAndTypeIdx =
        addNameAndType(Pair(addUtf(i9n.methodName), addUtf(i9n.methodType)))
			val methodrefIdx = addMethodref(Pair(classIdx, nameAndTypeIdx))
      return flattenI9n(i9n.objectExpr) +
        i9n.args.flatMap { flattenI9n(it).toList() } +
        arrayOf(Bytecode.Instruction.invokevirtual(methodrefIdx))

    } else {
      throw RuntimeException("Unknown instruction ${i9n::class}")
    }
  }
}
