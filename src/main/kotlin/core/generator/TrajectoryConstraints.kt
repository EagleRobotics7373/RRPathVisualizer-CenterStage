package core.generator

import com.acmerobotics.roadrunner.trajectory.constraints.*
import java.util.*

fun getMecanumVelocityConstraint(
    maxVel: Double,
    maxAngularVel: Double,
    trackWidth: Double
): TrajectoryVelocityConstraint {
    return MinVelocityConstraint(
        Arrays.asList(
            AngularVelocityConstraint(maxAngularVel),
            MecanumVelocityConstraint(maxVel, trackWidth)
        )
    )
}

fun getTankVelocityConstraint(
    maxVel: Double,
    maxAngularVel: Double,
    trackWidth: Double
): TrajectoryVelocityConstraint {
    return MinVelocityConstraint(
        listOf(
            AngularVelocityConstraint(maxAngularVel),
            TankVelocityConstraint(maxVel, trackWidth)
        )
    )
}

fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint {
    return ProfileAccelerationConstraint(maxAccel)
}
