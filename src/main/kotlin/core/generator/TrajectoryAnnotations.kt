package core.generator

@Target(AnnotationTarget.PROPERTY)
annotation class Config(val title: String = "", val environment: Boolean = false)

@Target(AnnotationTarget.CLASS)
annotation class PrimaryTrajectory

@Target(AnnotationTarget.CLASS)
annotation class Disabled