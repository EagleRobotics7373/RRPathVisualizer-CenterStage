package core.config

import core.generator.Config
import core.generator.PrimaryTrajectory
import core.generator.TrajectoryGen
import java.lang.reflect.InvocationTargetException
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.createType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.functions
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

class TrajectoryContainer(val clazz: Class<out TrajectoryGen>) {
    val name: String get() = this.clazz.simpleName
    val generator = this.clazz.getDeclaredConstructor().newInstance()
    val isPrimary = this.clazz.kotlin.findAnnotation<PrimaryTrajectory>() != null

    override fun toString(): String = name

    @SuppressWarnings("unchecked")
    val settings: List<TrajectoryGenSetting<*>> = clazz.kotlin.memberProperties
        .filter { it.findAnnotation<Config>() != null && it.visibility != KVisibility.PRIVATE }
        .mapNotNull { it as? KMutableProperty1<out TrajectoryGen, *> }
        .mapNotNull { property ->
            when (property.returnType) {
                Boolean::class.createType() -> TrajectoryGenSettingBool(property, generator)
                Int::class.createType() -> TrajectoryGenSettingInt(property, generator)
                Double::class.createType() -> TrajectoryGenSettingDouble(property, generator)
                else -> {
                    if (Class.forName(property.returnType.javaType.typeName).isEnum) {
                        TrajectoryGenSettingEnum(property, generator)
                    } else {
                        println("Property ${property.name} is NOT a valid type!")
                        null
                    }
                }
            }
        }
        .onEach { EnvironmentConfig.register(it) }
}

abstract class TrajectoryGenSetting<T>(
    protected val mutableProperty: KMutableProperty1<out TrajectoryGen, *>,
    private val obj: TrajectoryGen
) {
    val name = mutableProperty.findAnnotation<Config>()?.title.takeIf { it != "" } ?: mutableProperty.name
    var value: T
        @Suppress("UNCHECKED_CAST")
        get() {
            return mutableProperty.getter.call(obj) as T
        }
        set(newValue) {
            mutableProperty.setter.call(obj, newValue)
        }

    val isEnvironmentSetting = mutableProperty.findAnnotation<Config>()?.environment ?: false
    val returnType get() = mutableProperty.returnType
}

class TrajectoryGenSettingBool(mutableProperty: KMutableProperty1<out TrajectoryGen, *>, obj: TrajectoryGen) :
    TrajectoryGenSetting<Boolean>(mutableProperty, obj) {

}

class TrajectoryGenSettingInt(mutableProperty: KMutableProperty1<out TrajectoryGen, *>, obj: TrajectoryGen) :
    TrajectoryGenSetting<Int>(mutableProperty, obj) {
    fun setValue(param: String): Boolean {
        val converted = param.toIntOrNull()
        return if (converted != null) {
            super.value = converted
            true
        } else false
    }
}

class TrajectoryGenSettingDouble(mutableProperty: KMutableProperty1<out TrajectoryGen, *>, obj: TrajectoryGen) :
    TrajectoryGenSetting<Double>(mutableProperty, obj) {
    fun setValue(param: String): Boolean {
        val converted = param.toDoubleOrNull()
        return if (converted != null) {
            super.value = converted
            true
        } else false
    }
}

class TrajectoryGenSettingEnum(mutableProperty: KMutableProperty1<out TrajectoryGen, *>, obj: TrajectoryGen) :
    TrajectoryGenSetting<Any?>(mutableProperty, obj) {
    val values = mutableProperty.returnType.jvmErasure.java.enumConstants.map { it.toString() }
    fun setValue(param: String): Boolean {
        try {
            value = mutableProperty.returnType.jvmErasure.functions.first { it.name == "valueOf" }.call(param)
        } catch (e: InvocationTargetException) {
            return false
        }
        return true
    }

    fun getValueAsString(): String = value.toString()
}
