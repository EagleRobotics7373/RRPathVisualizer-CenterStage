package core.generator

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints
import core.GraphicsUtil
import kotlin.collections.ArrayList

abstract class TrajectoryGen(
    // Remember to set these constraints to the same values as your DriveConstants.java file in the quickstart
    private var driveConstraints: DriveConstraints = DriveConstraints(
        50.0,
        25.0,
        40.0,
        270.0.toRadians,
        270.0.toRadians,
        0.0
    ),
    // Remember to set your track width to an estimate of your actual bot to get accurate core.trajectory profile duration!
    private var trackWidth: Double = 16.0,

    var fieldImageName: String = "field_generic.png"
) {

    private val combinedConstraints get() = MecanumConstraints(driveConstraints, trackWidth)

    var startPose: Pose2d = Pose2d()

    abstract fun createTrajectory(): ArrayList<Trajectory>

    fun drawOffbounds() {
        GraphicsUtil.fillRect(Vector2d(0.0, -63.0), 18.0, 18.0) // robot against the wall
    }

    protected fun builder(tangent: Double = startPose.heading): TrajectoryBuilder =
        TrajectoryBuilder(startPose, tangent, combinedConstraints)

    protected fun TrajectoryBuilder.saveAndBuild(): Trajectory =
        this.build().also { this@TrajectoryGen.startPose = it.end() }

    protected fun TrajectoryBuilder.saveAndBuildTo(list: ArrayList<Trajectory>) =
        list.add(this.saveAndBuild())

    @Config(title = "Alliance Color", environment = true)
    var allianceColor: AllianceColor = AllianceColor.BLUE

    protected infix fun Double.reverseIf(allianceColor: AllianceColor): Double =
        if (this@TrajectoryGen.allianceColor == allianceColor) -this else this

    enum class AllianceColor {
        RED, BLUE
    }
}

abstract class TrajectoryGenUltimateGoal(
    driveConstraints: DriveConstraints,
    trackWidth: Double
) : TrajectoryGen(driveConstraints, trackWidth, "field_ultimate-goal.png") {

    @Config(title = "Starting Line", environment = true)
    var startingLine: StartingLine = StartingLine.FAR

    @Config(title = "Number of Rings")
    var numRings: Int = 0

    protected infix fun Double.reverseIf(startingLine: StartingLine): Double =
        if (this@TrajectoryGenUltimateGoal.startingLine == startingLine) -this else this

    enum class StartingLine {
        CENTER, FAR
    }
}

abstract class TrajectoryGenFreightFrenzy(
    driveConstraints: DriveConstraints,
    trackWidth: Double
) : TrajectoryGen(driveConstraints, trackWidth, "field_freight-frenzy.png") {

    @Config(title = "Duck Position")
    var duckPosition: DuckPosition = DuckPosition.LEFT

    enum class DuckPosition {
        LEFT, CENTER, RIGHT
    }

}

abstract class TrajectoryGenPowerPlay(
    driveConstraints: DriveConstraints,
    trackWidth: Double
) : TrajectoryGen(driveConstraints, trackWidth, "field_power-play.png") {


    @Config(title = "Starting Row", environment = true)
    var startingRow: TrajectoryGenPowerPlay.StartingRow = TrajectoryGenPowerPlay.StartingRow.ROW2

    @Config(title = "Signal Orientation", environment = true)
    var signalOrientation: TrajectoryGenPowerPlay.SignalOrientation = TrajectoryGenPowerPlay.SignalOrientation.IMAGE1

    enum class StartingRow {
        ROW2, ROW5
    }

    enum class SignalOrientation {
        IMAGE1, IMAGE2, IMAGE3
    }

    protected infix fun Double.reverseIf(startingRow: TrajectoryGenPowerPlay.StartingRow): Double =
        if (this@TrajectoryGenPowerPlay.startingRow == startingRow) -this else this


}

val Double.toRadians get() = (Math.toRadians(this))
