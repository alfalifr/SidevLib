package sidev.lib.reflex.full

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.sequence.nestedSequenceSimple
import sidev.lib.reflex.js.JsPrimitiveType
import sidev.lib.reflex.js.getSupertypes
import sidev.lib.reflex.js.jsPureFunction
import sidev.lib.collection.sequence.NestedSequence
import sidev.lib.reflex.js.kotlin.kotlinMetadata
import kotlin.reflect.KClass


actual val KClass<*>.isArray: Boolean
    get()= this == Array::class

actual val KClass<*>.isPrimitive: Boolean get() = when(js.name){
    JsPrimitiveType.STRING.jsConstructorName -> true
    JsPrimitiveType.NUMBER.jsConstructorName -> true
    JsPrimitiveType.BOOLEAN.jsConstructorName -> true
    else -> false
}

actual val KClass<*>.isCopySafe: Boolean
    get()= isBaseType //isPrimitive || this == String::class
            //|| isSubclassOf(Enum::class) TODO <29 Agustus 2020> => Untuk smtr, pengecekan enum dianggap tidak copySafe

actual val KClass<*>.isCollection: Boolean
    get(){
        val clsName= Collection::class.simpleName
        return classesTree.find { it.simpleName ==  clsName } != null
    }

actual val KClass<*>.isMap: Boolean
    get(){
        val clsName= Map::class.simpleName
        return classesTree.find { it.simpleName ==  clsName } != null
    }

actual val KClass<*>.classesTree: NestedSequence<KClass<*>>
    get()= nestedSequenceSimple(this){ supr ->
        getSupertypes(supr).asSequence()
            .map {
                @Suppress(SuppressLiteral.UNCHECKED_CAST_TO_EXTERNAL_INTERFACE)
                (jsPureFunction(it) as JsClass<*>).kotlin
            }
            .iterator()
    }


actual val KClass<*>.isInterface: Boolean
    get() {
        return kotlinMetadata.kind == "interface"
    }

actual val KClass<*>.isCommonSealed: Boolean
    get()= false //TODO <14 Sep 2020> => Karena Kotlin/Js blum bisa mengakses informasi sealed.