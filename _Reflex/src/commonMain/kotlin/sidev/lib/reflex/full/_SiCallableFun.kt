package sidev.lib.reflex.full

import sidev.lib.check.asNotNullTo
import sidev.lib.check.notNull
import sidev.lib.check.notNullTo
import sidev.lib.reflex.SiCallable
import sidev.lib.reflex.SiClass
import sidev.lib.reflex.SiParameter
import sidev.lib.reflex.core.ReflexTemplate
import sidev.lib.reflex.core.SiReflexConst
import sidev.lib.reflex.native_.getIsAccessible
import sidev.lib.reflex.native_.setIsAccessible
import sidev.lib.reflex.si
import kotlin.jvm.JvmName
import kotlin.reflect.KCallable

@set:JvmName("setAccessible")
@get:JvmName("getAccessible")
var SiCallable<*>.isAccessible: Boolean
    get()= descriptor.native.notNullTo { getIsAccessible(it) } ?: false
    set(v){ descriptor.native.notNull { setIsAccessible(it, v) } }



/**
 * Cara aman untuk memanggil [KCallable.callBy].
 *
 * Fungsi ini melakukan pengecekan terhadap tipe argumen, nullabilitas, dan opsionalitasnya
 * sehingga meminimalisir terjadi error saat pemanggilan [KCallable].
 */
fun <R> SiCallable<R>.callBySafely(args: Map<SiParameter, Any?>): R{
    val newArgs= HashMap<SiParameter, Any?>()
    for(param in parameters){
        var value= args[param]
//        prine("SiCallable<R>.callBySafely() param= $param value= $value")
        if(value == null){
            if(param.isOptional) continue
            if(!param.type.isMarkedNullable)
                throw IllegalArgumentException("Nilai argumen param: \"$param\" tidak boleh null")
        } else if(param.type.classifier.asNotNullTo { it: SiClass<*> -> it.isAssignableFrom(value!!::class.si) } != true ){
            if(param.type != ReflexTemplate.typeDynamic){
                if(param.isOptional) continue
                if(param.type.isMarkedNullable) value= null
                else throw IllegalArgumentException("Tipe param: \"$param\" adalah ${param.type.classifier}, namun argumen bertipe ${value::class}")
            }
        }
        newArgs[param]= value
    }
    return callBy(newArgs)
}

fun <R> SiCallable<R>.forceCall(vararg args: Any?): R{
    val initAccessible= isAccessible
    isAccessible= true
    val vals= call(*args)
    isAccessible= initAccessible
    return vals
}

fun <R> SiCallable<R>.forceCallBy(args: Map<SiParameter, Any?>): R{
    val initAccessible= isAccessible
    isAccessible= true
    val vals= callBy(args)
    isAccessible= initAccessible
    return vals
}

@get:JvmName("isConstructor")
val SiCallable<*>.isConstructor: Boolean
    get()= if((descriptor.host as? SiClass<*>)?.isArray == true) true
        else toString().matches(SiReflexConst.FUNCTION_CONSTRUCTOR_NAME_PATTERN.toRegex())
