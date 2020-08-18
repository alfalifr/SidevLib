package sidev.lib.reflex.common.native

import sidev.lib.exception.ReflexComponentExc
import sidev.lib.reflex.common.*
import sidev.lib.reflex.common.core.ReflexLoader
import sidev.lib.reflex.common.core.createDescriptor
import sidev.lib.reflex.common.core.createNativeWrapper
import kotlin.reflect.*

/*
internal val Any.nativeClass: SiNativeClassifier
    get()= try{ NativeReflexFactory.createClassifier(this) }
        catch (e: ReflexComponentExc){ NativeReflexFactory.createClassifier(this::class) }
 */

val <T: Any> KClass<T>.si: SiClass<T>
    get()= ReflexLoader.loadClass(this) as SiClass<T>


internal val KClassifier.si: SiKClassifier
    get()= object : SiKClassifierImpl(this){
        override val implementation: KClassifier = super.implementation
        override val descriptor: SiDescriptor = createDescriptor(nativeCounterpart = createNativeWrapper(this@si))
    }

/*
/**
 * Berisi fungsi pemetaan dari Kotlin.reflect ke sidev.lib.reflex.common.native
 */
val KClassifier.si: SiKClassifier
    get()= object : SiKClassifierImpl(this),
        SiNativeClassifier by NativeReflexFactory.createClassifier(this) {}

//expect val <T> KCallable<T>.si: SiNativeCallable<T>
 */