package trajectories

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import core.generator.*
import kotlin.math.PI

//@Disabled     // uncomment this to block the trajectory from being loaded
class TrajectoryGenSample : TrajectoryGenPowerPlay(
    trajectoryVelocityConstraint = getMecanumVelocityConstraint(40.0, Math.PI, 16.0),
    trajectoryAccelerationConstraint = getAccelerationConstraint(30.0)
) {

    @Config
    var startingHeading: Double = 0.0

    override fun createTrajectory(): ArrayList<Trajectory> {
        startPose = Pose2d(
            -36.0 reverseIf StartingRow.ROW2,
            -63.0 reverseIf AllianceColor.BLUE,
            startingHeading
        )

        val list = ArrayList<Trajectory>()


        // Small Example Routine
        builder(PI / 2 reverseIf AllianceColor.BLUE)
            .splineTo(Vector2d(10.0, 10.0), 0.0)
            .saveAndBuildTo(list)


        return list
    }
}