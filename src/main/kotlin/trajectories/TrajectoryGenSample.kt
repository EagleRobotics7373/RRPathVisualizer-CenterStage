package trajectories

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import StartingLine.*
import AllianceColor.*
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints
import core.generator.Config
import core.generator.Disabled
import core.generator.TrajectoryGenUltimateGoal
import kotlin.math.PI

//@Disabled     // uncomment this to block the trajectory from being loaded
class TrajectoryGenSample : TrajectoryGenUltimateGoal(
    driveConstraints = MecanumConstraints(
        DriveConstraints(40.0, 20.0, 40.0, PI, PI, 0.0),
        16.0
    ),
    trackWidth = 16.0
) {

    @Config
    var startingHeading: Double = 0.0

    override fun createTrajectory(): ArrayList<Trajectory> {
        startPose = Pose2d(
            -63.0,
            (if (startingLine == CENTER) -24.0 else -24.0 - 24.0) reverseIf BLUE,
            startingHeading
        )

        val list = ArrayList<Trajectory>()


        // Small Example Routine
        builder()
            .splineTo(Vector2d(10.0, 10.0), 0.0)
            .splineTo(Vector2d(15.0, 15.0), 90.0)
            .saveAndBuildTo(list)


        return list
    }
}