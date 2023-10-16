package trajectories

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import core.generator.*
import kotlin.math.PI

//@Disabled     // uncomment this to block the trajectory from being loaded
class TrajectoryGenSouth : TrajectoryGenCenterStage(
    trajectoryVelocityConstraint = getMecanumVelocityConstraint(40.0, Math.PI, 16.0),
    trajectoryAccelerationConstraint = getAccelerationConstraint(30.0)
) {

    @Config
    var startingHeading: Double = 0.0

    override fun createTrajectory(): ArrayList<Trajectory> {
        startPose = Pose2d(
            39.0,
            -65.0 reverseIf AllianceColor.BLUE,
            startingHeading
        )

        val list = ArrayList<Trajectory>()


        // Small Example Routine
        builder(PI / 2 reverseIf AllianceColor.BLUE)
            .splineToConstantHeading(Vector2d(
                -12.5,
                (if (signalPosition == SignalPosition.LEFT) -46.0 else -43.1) reverseIf AllianceColor.BLUE), Math.PI/2 reverseIf AllianceColor.BLUE)
            .saveAndBuildTo(list)

        return list
    }
}