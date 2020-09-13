package sidev.lib.reflex

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.exception.IllegalStateExc
import sidev.lib.reflex.native_.getKClass
import kotlin.reflect.KClass

expect val KClass<*>.nativeFullName: String
expect val KClass<*>.nativeSimpleName: String

/** Kelas native dari paltform-specific. */
expect val KClass<*>.native: Any

val KClass<*>.fullName: String
    get()= nativeFullName //qualifiedName ?: nativeFullName
val KClass<*>.simpleName: String
    get()= this.simpleName ?: nativeSimpleName

/** Berguna untuk mengambil kelas dari instance dg tipe generic yg tidak punya batas atas Any. */
val Any.clazz: KClass<*>
    get()= this::class