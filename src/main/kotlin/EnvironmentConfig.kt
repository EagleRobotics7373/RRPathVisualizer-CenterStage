object EnvironmentConfig {

    private val boolSettings = emptyList<TrajectoryGenSetting<Boolean>>().toMutableList()
    private val intSettings = emptyList<TrajectoryGenSetting<Int>>().toMutableList()
    private val doubleSettings = emptyList<TrajectoryGenSetting<Double>>().toMutableList()
    private val enumSettings = emptyList<TrajectoryGenSetting<Any?>>().toMutableList()

    fun register(setting: TrajectoryGenSetting<*>) {
        when (setting) {
            is TrajectoryGenSettingBool -> boolSettings.add(setting)
            is TrajectoryGenSettingInt -> intSettings.add(setting)
            is TrajectoryGenSettingDouble -> doubleSettings.add(setting)
            is TrajectoryGenSettingEnum -> enumSettings.add(setting)
        }
    }

    fun updateValue(setting: TrajectoryGenSetting<*>) {
        when (setting) {
            is TrajectoryGenSettingBool ->
                boolSettings.filter { it.name == setting.name && it != setting }.forEach { it.value = setting.value }
            is TrajectoryGenSettingInt ->
                intSettings.filter { it.name == setting.name && it != setting }.forEach { it.value = setting.value }
            is TrajectoryGenSettingDouble ->
                doubleSettings.filter { it.name == setting.name && it != setting }.forEach { it.value = setting.value }
            is TrajectoryGenSettingEnum ->
                enumSettings.filter { it.name == setting.name && it != setting }.forEach { it.value = setting.value }
        }
    }
}