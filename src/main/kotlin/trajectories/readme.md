## Add your team's custom trajectory generators here!

Any Kotlin class extending `TrajectoryGen` and stored in this `trajectories` package will automatically be loaded upon
application start.

Extend the `TrajectoryGenPowerPlay` class to receive three environment config variables:

- Alliance color (red or blue)
- Starting row (row 2 or row 5 of field)
- Signal orientation (image 1, image 2, or image 3)

## Annotations

### Config (for variables)

Add the `@Config` annotation to any `Int`, `Double`, `Boolean`, or `enum class` variable in the trajectory generator
class to be able to edit this variable during runtime.

Parameters:

- Name (String): Designate an alternate name for the config item that will be shown instead during runtime. This will
  also replace the variable name for matching the variable to the environment config.
- Environment (Boolean): If true, the annotated variable will be considered an "environment" variable. The value of this
  will be shared (and automatically updated) with all other variables with the same name and type across different
  trajectory generators.

### PrimaryTrajectory (for classes)

The first trajectory generator class found with the `@PrimaryTrajectory` annotation will be the first visualized upon
application start.

### Disabled (for classes)

Any trajectory generator class annotated with `@Disabled` will not be shown in the application.