package sidev.lib.reflex.core

import sidev.lib.`val`.SuppressLiteral
import sidev.lib.collection.lazy_list.asCached
import sidev.lib.console.prine
import sidev.lib.exception.IllegalStateExc
import sidev.lib.environment.Platform
import sidev.lib.environment.platform
import sidev.lib.property.mutableLazy
import sidev.lib.property.reevaluateLazy
import sidev.lib.reflex.*
import sidev.lib.reflex.SiClassImpl
import sidev.lib.reflex.SiFieldImpl
import sidev.lib.reflex.SiFunctionImpl
import sidev.lib.reflex.SiMutableFieldImpl
import sidev.lib.reflex.SiMutableProperty1Impl
import sidev.lib.reflex.SiParamterImpl
import sidev.lib.reflex.SiProperty1Impl
import sidev.lib.reflex.SiPropertyGetter1
import sidev.lib.reflex.SiPropertySetter1
import sidev.lib.reflex.SiTypeImpl
import sidev.lib.reflex.SiTypeParameterImpl
import sidev.lib.reflex.native_.*
import sidev.lib.reflex.native_.getReturnType
import sidev.lib.reflex.native_.getVisibility


internal expect val SiNative.nativeInnerName: String?
internal expect val SiNative.nativeFullName: String?
internal expect val SiNative.nativeSimpleName: String?

private val SiNative.qualifiedNativeName: String
    get()= nativeFullName ?: NativeReflexConst.TEMPLATE_NATIVE_NAME


internal fun createNativeWrapper(native: Any): SiNative = object : SiNative {
    override val implementation: Any = native
}


fun SiClassifier.createType(
    arguments: List<SiTypeProjection> = emptyList(),
    nullable: Boolean = false
): SiType = ReflexFactory.createType(
    if(descriptor.native != null) createNativeWrapper(descriptor.native!!) else null,
    this, arguments, nullable
)

val SiClass<*>.startProjectedType: SiType
    get()= createType(typeParameters.map { SiTypeProjection.STAR })

object ReflexFactory{
    fun createType(
        nativeCounterpart: SiNative?,
        classifier: SiClassifier?,
        arguments: List<SiTypeProjection> = emptyList(),
        nullable: Boolean = false,
        modifier: Int= 0
    ): SiType {
        val typeParam= if(classifier is SiClass<*>) classifier.typeParameters
            else emptyList()
        if(arguments.size < typeParam.size)
            throw IllegalArgumentException("arguments.size: ${arguments.size} < typeParam.size: ${typeParam.size}.")
        return _createType(nativeCounterpart, classifier, arguments, nullable, modifier)
    }

    /**
     * Sama dg [createType] namun tidak melakukan pengecekan jml type argument untuk kepentingan refleksi internal.
     */
    internal fun _createType(
        nativeCounterpart: SiNative?,
        classifier: SiClassifier?,
        arguments: List<SiTypeProjection> = emptyList(),
        nullable: Boolean = false,
        modifier: Int= 0
    ): SiType {
        return object : SiTypeImpl() {
            override val descriptor: SiDescriptor = createDescriptor(nativeCounterpart = nativeCounterpart, modifier = modifier)
            override var arguments: List<SiTypeProjection> = arguments
            override val classifier: SiClassifier? = classifier
            override val isMarkedNullable: Boolean = nullable
        }
    }


    fun createParameter(
        nativeCounterpart: SiNative?, //Untuk mengakomodasi parameter setter dan getter.
        hostCallable: SiCallable<*>?,
        index: Int, type: SiType,
        name: String?= null,
        kind: SiParameter.Kind= SiParameter.Kind.VALUE,
        defaultValue: Any?= null,
        modifier: Int= 0
    ): SiParameter = object: SiParamterImpl() {
        override val annotations: MutableList<Annotation> by lazy {
            nativeCounterpart?.implementation?.let{ getNativeAnnotations(it).toMutableList() }
                ?: arrayListOf()
        }
        override val descriptor: SiDescriptor = createDescriptor(hostCallable, nativeCounterpart, modifier)
        override val index: Int = index
        override val name: String? = name ?: nativeCounterpart?.qualifiedNativeName
        override val isOptional: Boolean = SiModifier.isOptional(this)
        override val isVararg: Boolean = SiModifier.isVararg(this)
        override var type: SiType = type
        override val kind: SiParameter.Kind = kind
        override val defaultValue: Any? = defaultValue
    }

    internal fun createParameterLazyly(
        nativeCounterpart: SiNative?, //Untuk mengakomodasi parameter setter dan getter.
        hostCallable: SiCallable<*>?,
        index: Int, //type: SiType,
        name: String?= null,
        kind: SiParameter.Kind= SiParameter.Kind.VALUE,
        defaultValue: Any?= null,
        modifier: Int= 0
    ): SiParameter = object: SiParamterImpl() {
        override val annotations: MutableList<Annotation> by lazy {
            nativeCounterpart?.implementation?.let{ getNativeAnnotations(it).toMutableList() }
                ?: arrayListOf()
        }
        override val descriptor: SiDescriptor = createDescriptor(hostCallable, nativeCounterpart, modifier)
        override val index: Int = index
        override val name: String? = name ?: nativeCounterpart?.qualifiedNativeName
        override val isOptional: Boolean = SiModifier.isOptional(this)
        override val isVararg: Boolean = SiModifier.isVararg(this)
        override val type: SiType by reevaluateLazy { eval ->
            val type= if(nativeCounterpart != null) getReturnType(nativeCounterpart.implementation)
                else ReflexTemplate.typeDynamic.also { eval.value= true; return@reevaluateLazy it }
            eval.value= platform != Platform.JS || isTypeFinal(type.descriptor.native!!)
            type
        }
        override val kind: SiParameter.Kind = kind
        override val defaultValue: Any? = defaultValue
    }


    fun createTypeParameter(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?,
        upperBounds: List<SiType>, variance: SiVariance,
        modifier: Int= 0
    ): SiTypeParameter = object: SiTypeParameterImpl() {
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val name: String = nativeCounterpart.qualifiedNativeName
        override var upperBounds: List<SiType> = upperBounds
        override val variance: SiVariance = variance
    }

    fun <R> createCallable(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
        returnType: SiType = ReflexTemplate.typeAnyNullable,
        parameters: List<SiParameter> = emptyList(),
        typeParameters: List<SiTypeParameter> = emptyList(),
        modifier: Int= 0,
        defaultCallBlock: ((args: Array<out Any?>) -> R)?= null,
        callBlock: (args: Array<out Any?>) -> R
    ): SiCallable<R> = object : SiCallableImplDelegate<R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val callBlock: (args: Array<out Any?>) -> R = callBlock
        override val defaultCallBlock: ((args: Array<out Any?>) -> R)? = defaultCallBlock
        override var descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
            set(v){
                if(v.owner !is SiCallable<*>)
                    throw IllegalStateExc(stateOwner = this::class,
                        currentState = "descriptor.owner !is SiCallable<*>",
                        expectedState = "descriptor.owner is SiCallable<*>")
                field= v
            }
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType = returnType
        override val parameters: List<SiParameter> = parameters
        override val typeParameters: List<SiTypeParameter> by lazy{
            if(typeParameters.isNotEmpty()) typeParameters
            else ReflexFactoryHelper.getTypeParameter(descriptor.host as? SiClass<*>, descriptor.owner as SiCallable<*>, nativeCounterpart.implementation)
        }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }

    internal fun <R> createCallableLazyly(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
//        returnType: SiType= ReflexTemplate.typeAnyNullable,
        parameters: List<SiParameter> = emptyList(),
        typeParameters: List<SiTypeParameter> = emptyList(),
        modifier: Int= 0,
        defaultCallBlock: ((args: Array<out Any?>) -> R)?= null,
        callBlock: ((args: Array<out Any?>) -> R)?= null
    ): SiCallable<R> = object : SiCallableImplDelegate<R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val callBlock: (args: Array<out Any?>) -> R by lazy {
            callBlock ?: getFuncCallBlock<R>((descriptor.host as SiClass<*>).descriptor.native!!, descriptor.native!!)
        }
        override val defaultCallBlock: ((args: Array<out Any?>) -> R)? by lazy {
            defaultCallBlock ?: run {
                val nativeClass= (descriptor.host as? SiClass<*>)?.descriptor?.native
                val nativeFunc= descriptor.native
                if(nativeClass != null && nativeFunc != null)
                    getFuncDefaultCallBlock(nativeClass, nativeFunc)
                else null
            }
        }
        override var descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
            set(v){
                if(v.owner !is SiCallable<*>)
                    throw IllegalStateExc(stateOwner = this::class,
                        currentState = "descriptor.owner !is SiCallable<*>",
                        expectedState = "descriptor.owner is SiCallable<*>")
                field= v
            }
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType by reevaluateLazy {
            val type= getReturnType(nativeCounterpart.implementation)
            it.value= platform != Platform.JS || isTypeFinal(type.descriptor.native!!)
            type
        }
        override val parameters: List<SiParameter> by lazy {
//            prine("crateCallableLazyli() parameters.isNotEmpty() || descriptor.native == null= ${parameters.isNotEmpty() || descriptor.native == null} this= $this")
            if(parameters.isNotEmpty() || descriptor.native == null) parameters
            else ReflexLoader.loadSiParam(descriptor.native!!).map { it.mutableHost= descriptor.owner; it }.asCached()
        }
        override val typeParameters: List<SiTypeParameter> by lazy {
            if(typeParameters.isNotEmpty()) typeParameters
            else ReflexFactoryHelper.getTypeParameter(descriptor.host as? SiClass<*>, descriptor.owner as SiCallable<*>, nativeCounterpart.implementation)
        }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }


    fun <R> createFunction(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
        returnType: SiType = ReflexTemplate.typeAnyNullable,
        parameters: List<SiParameter> = emptyList(),
        typeParameters: List<SiTypeParameter> = emptyList(),
        modifier: Int= 0,
        defaultCallBlock: ((args: Array<out Any?>) -> R)?= null,
        callBlock: (args: Array<out Any?>) -> R
    ): SiFunction<R> {
        val callable= createCallable(
            nativeCounterpart, host, returnType, parameters, typeParameters, modifier, defaultCallBlock, callBlock
        ) as SiCallableImplDelegate
        return object : SiFunctionImpl<R>(), SiCallable<R> by callable{
            override val annotations: MutableList<Annotation>
                get() = callable.annotations
            override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier) //Agar ownernya jadi SiFunction
            init{
                callable.descriptor= this.descriptor
            }
            override val callBlock: (args: Array<out Any?>) -> R = callBlock
            override val defaultCallBlock: ((args: Array<out Any?>) -> R)? = defaultCallBlock
            override fun call(vararg args: Any?): R = callable.call(*args)
            override fun callBy(args: Map<SiParameter, Any?>): R = callable.callBy(args)
        }
    }

    internal fun <R> createFunctionLazyly(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
//        returnType: SiType= ReflexTemplate.typeAnyNullable,
        parameters: List<SiParameter> = emptyList(),
        typeParameters: List<SiTypeParameter> = emptyList(),
        modifier: Int= 0,
        defaultCallBlock: ((args: Array<out Any?>) -> R)?= null,
        callBlock: ((args: Array<out Any?>) -> R)?= null
    ): SiFunction<R> {
        val callable= createCallableLazyly(
            nativeCounterpart, host, parameters, typeParameters, modifier, defaultCallBlock, callBlock
        ) as SiCallableImplDelegate
        return object : SiFunctionImpl<R>(), SiCallable<R> by callable{
            override val annotations: MutableList<Annotation>
                get() = callable.annotations
            override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier) //Agar ownernya jadi SiFunction
            init{
                callable.descriptor= this.descriptor
            }
            override val callBlock: (args: Array<out Any?>) -> R by lazy {
                callBlock ?: getFuncCallBlock<R>((descriptor.host as SiClass<*>).descriptor.native!!, descriptor.native!!)
            }
            override val defaultCallBlock: ((args: Array<out Any?>) -> R)? by lazy {
                defaultCallBlock ?: run {
                    val nativeClass= (descriptor.host as? SiClass<*>)?.descriptor?.native
                    val nativeFunc= descriptor.native
                    if(nativeClass != null && nativeFunc != null)
                        getFuncDefaultCallBlock(nativeClass, nativeFunc)
                    else null
                }
            }
            override fun call(vararg args: Any?): R = callable.call(*args)
            override fun callBy(args: Map<SiParameter, Any?>): R = callable.callBy(args)
        }
    }


    fun <T, R> createProperty1(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
        type: SiType = ReflexTemplate.typeAnyNullable,
        modifier: Int= 0
    ): SiProperty1<T, R> = object : SiProperty1Impl<T, R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType = type
        override val backingField: SiField<T, R>? by lazy{ _createFieldFromProperty<T, R>(this) }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }

    internal fun <T, R> createProperty1Lazyly(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
//        type: SiType= ReflexTemplate.typeAnyNullable,
        modifier: Int= 0
    ): SiProperty1<T, R> = object : SiProperty1Impl<T, R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType by reevaluateLazy {
            val type= getReturnType(nativeCounterpart.implementation)
            it.value= platform != Platform.JS || isTypeFinal(type.descriptor.native!!)
            type
        }
        override val backingField: SiField<T, R>? by lazy{ _createFieldFromProperty<T, R>(this) }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }


    fun <T, R> createMutableProperty1(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
        type: SiType = ReflexTemplate.typeAnyNullable,
        modifier: Int= 0
    ): SiMutableProperty1<T, R> = object : SiMutableProperty1Impl<T, R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType = type
        override val backingField: SiMutableField<T, R>? by lazy{ _createFieldFromProperty<T, R>(this) }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }


    internal fun <T, R> createMutableProperty1Lazyly(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer?= null,
//        type: SiType= ReflexTemplate.typeAnyNullable,
        modifier: Int= 0
    ): SiMutableProperty1<T, R> = object : SiMutableProperty1Impl<T, R>(){
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val name: String = nativeCounterpart.qualifiedNativeName
        override val returnType: SiType by reevaluateLazy {
            val type= getReturnType(nativeCounterpart.implementation)
            it.value= platform != Platform.JS || isTypeFinal(type.descriptor.native!!)
            type
        }
        override val backingField: SiMutableField<T, R>? by lazy{ _createFieldFromProperty<T, R>(this) }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }

    internal fun <T, R> createPropertyGetter1(
        property: SiProperty1<T, R>, visibility: SiVisibility = SiVisibility.PUBLIC,
        modifier: Int= 0
    ): SiPropertyGetter1<T, R> = object : SiPropertyGetter1<T, R>(property){
        override val descriptor: SiDescriptor = createDescriptor(property, modifier = modifier)
        override val visibility: SiVisibility = visibility
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }
    internal fun <T, R> createPropertySetter1(
        property: SiProperty1<T, R>, visibility: SiVisibility = SiVisibility.PUBLIC,
        modifier: Int= 0
    ): SiPropertySetter1<T, R> = object : SiPropertySetter1<T, R>(property){
        override val descriptor: SiDescriptor = createDescriptor(property, modifier = modifier)
        override val visibility: SiVisibility = visibility
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }


    fun <T: Any> createClass(
        nativeCounterpart: SiNative,
        host: SiDescriptorContainer? = null,
        constructors: List<SiFunction<T>> = emptyList(),
        members: Collection<SiCallable<*>> = emptyList(),
        typeParameters: List<SiTypeParameter> = emptyList(),
        modifier: Int= 0
    ): SiClass<T> = object : SiClassImpl<T>() {
        override val annotations: MutableList<Annotation> by lazy {
            getNativeAnnotations(nativeCounterpart.implementation).toMutableList()
        }
        override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
        override val qualifiedName: String? = nativeCounterpart.nativeFullName //nativeCounterpart.qualifiedNativeName
        override val simpleName: String? = nativeCounterpart.nativeSimpleName //ReflexFactoryHelper.getSimpleName(nativeCounterpart, qualifiedName)
        override var members: Collection<SiCallable<*>> by mutableLazy {
            if(members.isNotEmpty()) members else ReflexLoader.loadSiMember(this).asCached()
        }
        override var constructors: List<SiFunction<T>> by mutableLazy {
            if(constructors.isNotEmpty()) constructors else ReflexLoader.loadSiConstructors(this).asCached()
        }
        override val typeParameters: List<SiTypeParameter> by lazy {
            if(typeParameters.isNotEmpty()) typeParameters
            else ReflexFactoryHelper.getTypeParameter(this, nativeCounterpart.implementation)
        }
        override val supertypes: List<SiType> by lazy {
            ReflexFactoryHelper.getSupertypes(this, nativeCounterpart.implementation)
        }
        override val visibility: SiVisibility = getVisibility(nativeCounterpart.implementation)
        override val isAbstract: Boolean = SiModifier.isAbstract(this)
    }

    fun createAnnotation(
        nativeCounterpart: SiNative?,
        host: SiAnnotatedElement?= null,
    ): SiAnnotation = object : SiAnnotationImpl(){
        override val descriptor: SiDescriptor = createDescriptor(
            if(host is SiDescriptorContainer) host else null,
            nativeCounterpart
        )
    }
///*
    fun <R, T>createField(
    nativeCounterpart: SiNative?,
    host: SiDescriptorContainer?= null,
    name: String,
    type: SiType,
    modifier: Int= 0
    ): SiField<R, T> {
        val getBlock= if(nativeCounterpart != null) getPropGetValueBlock<T>(nativeCounterpart.implementation) //(arrayOf(receiver as Any))
        else {
            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            val propGetter= (host as? SiProperty<T>)?.getter
            { receivers: Array<out Any> -> propGetter?.call(receivers.first())!! }
        }
        return object : SiFieldImpl<R, T>(
            { receiver: R -> getBlock(arrayOf<Any>(receiver!!)) }
        ){
            override val annotations: MutableList<Annotation> by lazy {
                nativeCounterpart?.implementation?.let{ getNativeAnnotations(it).toMutableList() }
                    ?: arrayListOf()
            }
            override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
            override val name: String = name
            override val type: SiType = type
        }
    }

    fun <R, T>createMutableField(
        nativeCounterpart: SiNative?,
        host: SiDescriptorContainer?= null,
        name: String,
        type: SiType,
        modifier: Int= 0
    ): SiMutableField<R, T> {
        val getBlock= if(nativeCounterpart != null) getPropGetValueBlock(nativeCounterpart.implementation) //(arrayOf(receiver as Any))
        else {
//            prine("createField() getBlock else")
            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            val propGetter= (host as? SiProperty<T>)?.getter
            { receivers: Array<out Any> -> propGetter?.call(receivers.first())!! }
        }
        val setBlock= if(nativeCounterpart != null) getPropSetValueBlock(nativeCounterpart.implementation) //(arrayOf(receiver as Any))
        else {
            @Suppress(SuppressLiteral.UNCHECKED_CAST)
            val propSetter= (host as? SiMutableProperty<T>)?.setter
            { receivers: Array<out Any>, value: T -> propSetter?.call(receivers.first(), value)!! }
        }
        return object : SiMutableFieldImpl<R, T>(
            { receiver: R -> getBlock(arrayOf<Any>(receiver!!)) },
            { receiver: R, value: T -> setBlock(arrayOf<Any>(receiver!!), value) }
        ){
            override val annotations: MutableList<Annotation> by lazy {
                nativeCounterpart?.implementation?.let{ getNativeAnnotations(it).toMutableList() }
                    ?: arrayListOf()
            }
            override val descriptor: SiDescriptor = createDescriptor(host, nativeCounterpart, modifier)
            override val name: String = name
            override val type: SiType = type
        }
    }

    /**
     * Untuk kepentingan internal, semua field adalah mutable field, namun bbrp tidak diekspos fungsi set-nya.
     * Pada dasarnya, semua field itu mutable. Namun API Reflex sangat menyarankan immutability sehingga
     * bbrp akses `set` dibatasi.
     */
    internal fun <R, T>_createField(
        nativeCounterpart: SiNative?,
        host: SiDescriptorContainer?= null,
        name: String,
        type: SiType,
        modifier: Int= 0
    ): SiMutableField<R, T> = createMutableField(nativeCounterpart, host, name, type, modifier)

    /**
     * Untuk kepentingan internal, semua field adalah mutable field, namun bbrp tidak diekspos fungsi set-nya.
     * Pada dasarnya, semua field itu mutable. Namun API Reflex sangat menyarankan immutability sehingga
     * bbrp akses `set` dibatasi.
     */
    internal fun <R, T> _createFieldFromProperty(property: SiProperty<T>): SiMutableField<R, T>?{
        return property.descriptor.native?.let { getNativeField(it) }?.let { createNativeWrapper(it) }
            ?.let { nativeField ->
                _createField(nativeField, property, property.name, property.returnType, getModifiers(nativeField.implementation))
            }
    }
// */
}