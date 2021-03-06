package sidev.lib.reflex.native_

import sidev.lib.exception.ReflexComponentExc
import sidev.lib.number.or
import sidev.lib.reflex.SiModifier
import sidev.lib.reflex.SiParameter
import sidev.lib.reflex.SiType
import sidev.lib.reflex.SiVisibility
import sidev.lib.reflex.core.ReflexFactory
import sidev.lib.reflex.core.ReflexTemplate
import sidev.lib.reflex.core.createNativeWrapper
import sidev.lib.reflex.js.*
import sidev.lib.reflex.js.kotlin.KotlinJsConst
import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.iterator.iteratorSimple
import sidev.lib.console.log
import sidev.lib.reflex.core.createType
import kotlin.reflect.KClass
import sidev.lib.reflex.js.isFunction
import sidev.lib.structure.data.value.VarImpl


actual val isDynamicEnabled: Boolean = true

@Suppress(SuppressLiteral.UNCHECKED_CAST)
val <T: Any> T.jsClass: JsClass_<T> get(){
    if(this is JsClass_<*>) return this as JsClass_<T>

    @Suppress(SuppressLiteral.UNCHECKED_CAST_TO_EXTERNAL_INTERFACE)
    val jsClass= (if(this is KClass<*>) this else this::class).js as JsClass<T>
//    prine("val jsClass jsClass= ${str(jsClass)}")

    return if(jsClass.name.startsWith(KotlinJsConst.K_FUNCTION_NAME_PREFIX)){
        //Jika jsClass berupa fungsi dg nama prefix Function, artinya [jsClass] merupakan function yg didefinisikan
        // menggunakan js() atau eval(), maka kembalikan fungsi itu sendiri, namun dibungkus [JsClass_],
        // walau tidak memiliki metadata seperti yg disuntikan pada Kotlin.
        if(isFunction)
            JsClassImpl_(this)
        else if(jsClass.isFunction)
            JsClassImpl_(jsClass)
        else {
            val kt = jsClass.kotlin
            log(this)
            log(kt)
            log(jsClass.isFunction)
            throw ReflexComponentExc(currentReflexedUnit = this, detMsg = "`this` bkn merupakan fungsi Js.")
        }
    }
    else jsClass.toClassWrapper()
}


internal actual fun getNativeAnnotations(nativeAnnotatedElement: Any): Sequence<Annotation> = emptySequence()
        //= unavailableReflexAcces_sequence("getNativeAnnotations(nativeAnnotatedElement: Any)", nativeAnnotatedElement, "annotation")

/** Mengambil KClass dari [nativeClass]. */
internal actual fun <T: Any> getKClass(nativeClass: Any): KClass<T> = when(nativeClass){
    is KClass<*> -> nativeClass
    is JsClass_<*> -> nativeClass.kotlin
    else -> throw ReflexComponentExc(currentReflexedUnit = nativeClass::class, detMsg = "nativeClass bkn class.")
} as KClass<T>

/** `this.extension` dapat berupa apa saja. */
internal actual fun getNativeClass(any: Any): Any = any.jsClass

/** `this.extension` yg dimaksud adalah native class. */
internal actual fun getNativeFunctions(nativeClass: Any): Sequence<Any>
        = getDeclaredFunction(nativeClass.jsClass).asSequence()

/** Termasuk yg mutable. */
//TODO <16 Agustus 2020> => untuk smtr sama dg yg mutable karena Kotlin Js hanya menkonfigurasi mutable saja.
//TODO <19 Agustus 2020> => untuk smtr immutable empty.
internal actual fun getNativeProperties(nativeClass: Any): Sequence<Any> = emptySequence()

internal actual fun getNativeMutableProperties(nativeClass: Any): Sequence<Any>
        = getDeclaredProperty(nativeClass.jsClass).asSequence()

/**
 * Fungsi ini hanya mengambil declared field saja.
 * <JS, 23 Agustus 2020> => Untuk Js, field dan property sama.
 */
internal actual fun getNativeFields(nativeClass: Any, nativeProperties: Sequence<Any>): Sequence<Any?>
        = nativeProperties

/**
 * Sama dg [getNativeFields], namun hanya mengambil field dari satu [nativeProperty].
 * <JS, 23 Agustus 2020> => Untuk Js, field dan property sama.
 */
internal actual fun getNativeField(nativeProperty: Any): Any? = nativeProperty

/**
 * Mengambil member yg dapat dipanggil dan dijadikan sbg [SiNativeCallable].
 * Member yg diambil termasuk fungsi dan semua properti.
 */
internal actual fun getNativeMembers(nativeClass: Any): Sequence<Any>
        = getNativeMutableProperties(nativeClass) + getNativeFunctions(nativeClass)

//TODO <16 Agustus 2020> => untuk smtr konstruktor hanya bisa didapatkan 1.
internal actual fun getNativeConstructors(nativeClass: Any): Sequence<Any>
        = Sequence { iteratorSimple(JsConstructorImpl<Any>(jsConstructor(nativeClass.jsClass) as Any)) }
/*
        = object : Sequence<JsCallable<Any>>{
    override fun iterator(): Iterator<JsCallable<Any>> = object : Iterator<JsCallable<Any>>{
        var index= 0
        override fun hasNext(): Boolean{
            prine("js getNativeConstructors() this= $this index= $index")
            return index++ < 1
        }
        override fun next(): JsCallable<Any> = JsCallableImpl<Any>(jsConstructor(nativeClass.jsClass) as Any)
            .also{
                prine("js getNativeConstructors() this= $this index= $index next= $it")
                log(it)
            }
    }
}
 */


/** Biasanya `this` merupakan fungsi yg punya paramter. */
internal actual fun getNativeParameters(nativeFunc: Any): Sequence<Any>
        = getParam((jsPureFunction(nativeFunc) as Any).jsClass).asSequence()

/** Mengambil immediate supertype dari `this`. */
internal actual fun getNativeSupertypes(nativeClass: Any): Sequence<Any>
        = getSupertypes(nativeClass.jsClass).asSequence()


internal actual fun getParamIsOptional(nativeParam: Any): Boolean = (nativeParam as JsParameter).isOptional
internal actual fun getParamIsVararg(nativeParam: Any): Boolean = false
internal actual fun getParamType(nativeParam: Any): SiType = when(nativeParam){
    is JsParameter -> nativeParam.type.classifier?.siClass?.createType() ?: ReflexTemplate.typeDynamic
    else -> ReflexTemplate.typeDynamic
}
internal actual fun getParamKind(nativeParam: Any): SiParameter.Kind = SiParameter.Kind.VALUE
internal actual fun getParamDefaultValue(nativeParam: Any): Any? = (nativeParam as JsParameter).defaultValue
internal actual fun <T> getFuncCallBlock(nativeFuncHost: Any, nativeFunc: Any): (args: Array<out Any?>) -> T
        = { (nativeFunc as JsCallable<T>).call(*it) as T }

internal actual fun <T> getConstrCallBlock(nativeFuncHost: Any, nativeFunc: Any): (args: Array<out Any?>) -> T
        = { (nativeFunc as JsCallable<T>).new(*it) }

internal actual fun <T> getFuncDefaultCallBlock(nativeFuncHost: Any, nativeFunc: Any): ((args: Array<out Any?>) -> T)?
        = getFuncCallBlock(nativeFuncHost, nativeFunc)

internal actual fun <T> getPropGetValueBlock(nativeProp: Any): (receivers: Array<out Any>) -> T = {
    (nativeProp as JsProperty<Any, T>)[it.first()]
}
internal actual fun <T> getPropSetValueBlock(nativeProp: Any): (receivers: Array<out Any>, value: T) -> Unit
        = { receivers, value ->
    (nativeProp as JsMutableProperty<Any, T>)[receivers.first()]= value
}
//TODO <20 Agustus 2020> => classifier String dan Number belum sesuai dg kriteria kelas bawaan Kotlin.
internal actual fun getReturnType(nativeCallable: Any): SiType = when(nativeCallable){
    is JsCallable<*> -> {
//        prine("nativeCallable.returnType.classifier == null => ${nativeCallable.returnType.classifier == null} nativeCallable= $nativeCallable")
        val nativeType= nativeCallable.returnType
        val classifier= nativeType.classifier?.siClass ?: ReflexTemplate.classifierAny
        ReflexFactory.createType(
            createNativeWrapper(nativeType), classifier, nullable = true, modifier = SiModifier.DYNAMIC.id
        )
    }
    else -> ReflexTemplate.typeDynamic
}
/** @return `true` jika [nativeType] sudah final dan gak perlu dievaluasi lagi. */
internal actual fun isTypeFinal(nativeType: Any): Boolean = when(nativeType){
    is JsType -> nativeType.isClassifierResolved
    else -> true
}

/** By default, semua property di Js public. */
internal actual fun getVisibility(nativeReflexUnit: Any): SiVisibility = SiVisibility.PUBLIC

internal actual fun getModifiers(nativeReflexUnit: Any): Int{
    val modifier= VarImpl(0)
    if(nativeReflexUnit !is JsParameter)
        modifier or SiModifier.OPEN.id or SiModifier.MUTABLE.id
    return modifier.value!!
}

/*
//TODO <19 Agustus 2020> => Untuk sementara akses di Js semuanya publik.
internal actual fun getIsAccessible(nativeReflexUnit: Any): Boolean = true
internal actual fun setIsAccessible(nativeReflexUnit: Any, isAccessible: Boolean){ /*sementara msh kosong*/ }
 */