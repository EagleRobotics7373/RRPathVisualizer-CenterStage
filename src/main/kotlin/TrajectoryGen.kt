import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints
import java.lang.reflect.InvocationTargetException
import kotlin.collections.ArrayList
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KVisibility
import kotlin.reflect.full.*
import kotlin.reflect.jvm.javaType
import kotlin.reflect.jvm.jvmErasure

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
    // Remember to set your track width to an estimate of your actual bot to get accurate trajectory profile duration!
    private var trackWidth: Double = 16.0
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
}

abstract class TrajectoryGenUltimateGoal(
    driveConstraints: DriveConstraints,
    trackWidth: Double
) : TrajectoryGen(driveConstraints, trackWidth) {

    @Config(title = "Starting Line", environment = true)
    var startingLine: StartingLine = StartingLine.FAR

    @Config(title = "Alliance Color", environment = true)
    var allianceColor: AllianceColor = AllianceColor.BLUE

    @Config(title = "Number of Rings")
    var numRings: Int = 0

    protected infix fun Double.reverseIf(allianceColor: AllianceColor): Double =
        if (this@TrajectoryGenUltimateGoal.allianceColor == allianceColor) -this else this

    protected infix fun Double.reverseIf(startingLine: StartingLine): Double =
        if (this@TrajectoryGenUltimateGoal.startingLine == startingLine) -this else this
}


@Target(AnnotationTarget.PROPERTY)
annotation class Config(val title: String = "", val environment: Boolean = false)

@Target(AnnotationTarget.CLASS)
annotation class PrimaryTrajectory

val Double.toRadians get() = (Math.toRadians(this))
