package sidev.lib.reflex.common.native

import sidev.lib.check.asNotNullTo
import sidev.lib.collection.findIndexed
import sidev.lib.console.prine
import sidev.lib.reflex.common.*
import sidev.lib.reflex.common.core.ReflexFactory
import sidev.lib.reflex.common.core.ReflexLoader
import sidev.lib.reflex.common.core.createNativeWrapper
import sidev.lib.reflex.common.core.createType
import kotlin.reflect.*


internal val KType.si: SiType get()= ReflexFactory.createType(
    createNativeWrapper(this),
    when(val cls= classifier){
        is KClass<*> -> try{ ReflexLoader.loadClass(cls) } catch (e: Error){ (cls as KClassifier).si }
        is KTypeParameter -> ReflexFactory.createTypeParameter(
            createNativeWrapper(cls), null, cls.upperBounds.map { it.si }, cls.variance.si
        )
        else -> null
    },
    arguments.map { typeProjection ->
//        prine("KType.si argument it= $typeProjection")
        val siType= if(classifier is KClass<*>)
            typeProjection.type?.classifier.asNotNullTo { typeParam: KTypeParameter ->
                typeParam.upperBounds.findIndexed { it.value.classifier == classifier }
                    ?.let { (it.value.classifier as KClass<*>).si.typeParameters[it.index] }
                    ?.let { it.createType() }
            } ?: typeProjection.type?.si
        else typeProjection.type?.si
        SiTypeProjection(typeProjection.variance?.si, siType)
    }, isMarkedNullable
)
/*
internal val KParameter.si: SiParameter
    get()= ReflexFactory.createParameter(createNativeWrapper(this), null)
 */
/*
internal val KParameter.siNative: SiNativeParameter get()= NativeReflexFactory.createParameter(
    NativeReflexFactory.createParameter(this, index, isOptional, type.si),
    index, isOptional, type.si
)
 */
/*
internal val KTypeParameter.siNative: SiNativeClassifier
    get()= NativeReflexFactory.createClassifier(this)
 */

internal val KTypeParameter.si: SiTypeParameter get()= ReflexFactory.createTypeParameter(
    createNativeWrapper(this), null, upperBounds.map { it.si }, variance.si
)
/**
 * Untuk type param yg upperBound-nya circular,
 * misalkan: class Enum<E: Enum<E>>
 */
internal val KTypeParameter.siCircular: SiTypeParameter get()= ReflexFactory.createTypeParameter(
    createNativeWrapper(this), null, emptyList(), variance.si
)

internal val KTypeProjection.si: SiTypeProjection
    get()= SiTypeProjection(variance?.si, type?.si)

internal val KVariance.si: SiVariance get()= when(this){
    KVariance.IN -> SiVariance.IN
    KVariance.OUT -> SiVariance.OUT
    KVariance.INVARIANT -> SiVariance.INVARIANT
}

internal val KParameter.Kind.si: SiParameter.Kind get()= when(this){
    KParameter.Kind.INSTANCE -> SiParameter.Kind.INSTANCE
    KParameter.Kind.EXTENSION_RECEIVER -> SiParameter.Kind.RECEIVER
    KParameter.Kind.VALUE -> SiParameter.Kind.VALUE
}

internal val KVisibility.si: SiVisibility get()= when(this){
    KVisibility.PUBLIC -> SiVisibility.PUBLIC
    KVisibility.PROTECTED -> SiVisibility.PROTECTED
    KVisibility.INTERNAL -> SiVisibility.INTERNAL
    KVisibility.PRIVATE -> SiVisibility.PRIVATE
}