package kotlinc

data class AccessFlags(
	val public: Boolean = false,
	val static: Boolean = false
) {
	fun toInt(): Int =
		(if (public) 1 else 0) +
		(if (static) 8 else 0)
	override fun toString(): String =
		arrayOf(
			if (public) "PUBLIC" else null,
			if (static) "STATIC" else null
		).filterNotNull().joinToString("|")
}
