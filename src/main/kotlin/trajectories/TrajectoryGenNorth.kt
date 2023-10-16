package trajectories

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import core.generator.*
import kotlin.math.PI

//@Disabled     // uncomment this to block the trajectory from being loaded
@PrimaryTrajectory
class TrajectoryGenNorth : TrajectoryGenCenterStage(
    trajectoryVelocityConstraint = getMecanumVelocityConstraint(40.0, Math.PI, 16.0),
    trajectoryAccelerationConstraint = getAccelerationConstraint(30.0)
) {

    @Config
    var startingHeading: Double = 0.0

    override fun createTrajectory(): ArrayList<Trajectory> {
        startPose = Pose2d(
            14.5,
            -63.5 reverseIf AllianceColor.BLUE,
            startingHeading
        )

        val list = ArrayList<Trajectory>()
        val xposition = if (signalPosition == SignalPosition.LEFT) {
            23.0
        } else if (signalPosition == SignalPosition.CENTER) {
            16.0
        } else {
            1.0
        }
        val yposition = if (signalPosition == SignalPosition.LEFT) {
            30.0
        } else if (signalPosition == SignalPosition.CENTER) {
            25.0
        } else {
            30.0
        }

        val park= Vector2d(62.0, -62.0 reverseIf AllianceColor.BLUE)


        // Small Example Routine
        builder(PI / 2 reverseIf AllianceColor.BLUE)
            .splineToConstantHeading(Vector2d(
                (xposition),
                (-yposition) reverseIf AllianceColor.BLUE), Math.PI/2 reverseIf AllianceColor.BLUE)

            .saveAndBuildTo(list)

        when (postSignalPositionTask) {
            PostSignalPositionTask.NOTHING -> {
                builder()
                    .saveAndBuildTo(list)
            }
           PostSignalPositionTask.PARK -> {
                builder(-Math.PI/2 reverseIf AllianceColor.BLUE)
                    .splineToConstantHeading(Vector2d(-63.0, -53.5 reverseIf AllianceColor.BLUE), PI)
                    .saveAndBuildTo(list)
            }
        }

        return list
    }
}