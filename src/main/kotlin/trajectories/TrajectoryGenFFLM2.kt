package trajectories

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import core.generator.TrajectoryGen.AllianceColor.*
import core.generator.*
import kotlin.math.PI

@PrimaryTrajectory
class TrajectoryGenFFLM2: TrajectoryGenFreightFrenzy(
    trajectoryVelocityConstraint = getMecanumVelocityConstraint(40.0, Math.PI, 16.0),
    trajectoryAccelerationConstraint = getAccelerationConstraint(30.0)
) {

    @Config var postAllianceHubTask: PostAllianceHubTask = PostAllianceHubTask.NOTHING
    @Config var startingPosition: StartingPosition = StartingPosition.CENTER

    override fun createTrajectory(): ArrayList<Trajectory> {
        startPose = Pose2d(
            when (startingPosition) {
                StartingPosition.CENTER -> -12.5
                StartingPosition.NEAR_CAROUSEL -> -36.0
                StartingPosition.NEAR_WAREHOUSE -> 12.5
            },
            -63.0 reverseIf BLUE,
            -Math.PI / 2 reverseIf BLUE
        )

        val list = ArrayList<Trajectory>()


        //Grab Capstone

        builder(Math.PI/2 reverseIf BLUE)
            .splineToConstantHeading(Vector2d(
                -12.5,
                (if (duckPosition == DuckPosition.LEFT) -46.0 else -43.1) reverseIf BLUE), Math.PI/2 reverseIf BLUE)
            .saveAndBuildTo(list)

        //Drop Off Capstone

        when (postAllianceHubTask) {
            PostAllianceHubTask.NOTHING -> {
                builder()
                    .forward(2.0)
                    .saveAndBuildTo(list)
            }
            PostAllianceHubTask.CAROUSEL -> {
                builder(-Math.PI/2 reverseIf BLUE)
                    .splineToConstantHeading(Vector2d(-63.0, -53.5 reverseIf BLUE), PI)
                    .saveAndBuildTo(list)

                //Turn Carousel

                builder(Math.PI/2 reverseIf BLUE)
                    .splineToConstantHeading(
                        Vector2d(-62.0, -36.0 reverseIf BLUE),
                        Math.PI / 2 reverseIf BLUE
                    )
                    .saveAndBuildTo(list)
            }
            PostAllianceHubTask.WAREHOUSE -> {
                builder(-Math.PI/2 reverseIf BLUE)
                    .splineTo(
                        Vector2d(12.0, -63.0 reverseIf BLUE),
                        0.0
                    )
                    .strafeTo(Vector2d(40.0, -63.0 reverseIf BLUE))
                    .saveAndBuildTo(list)
            }
        }


        return list
    }

    enum class PostAllianceHubTask {
        CAROUSEL, WAREHOUSE, NOTHING
    }

    enum class StartingPosition {
        NEAR_CAROUSEL, CENTER, NEAR_WAREHOUSE
    }
}