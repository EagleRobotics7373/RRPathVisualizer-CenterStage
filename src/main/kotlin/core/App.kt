package core

import core.generator.TrajectoryGen
import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import core.config.*
import core.generator.Disabled
import javafx.animation.KeyFrame
import javafx.animation.Timeline
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.shape.Rectangle
import javafx.scene.text.Font

import javafx.stage.Stage
import javafx.util.Duration
import org.reflections.Reflections
import java.lang.reflect.Modifier
import kotlin.reflect.full.findAnnotation

class App : Application() {

    // List of available core.trajectory generators, and associated settings
    private val trajectoryGenContainers = Reflections("trajectories")
        .getSubTypesOf(TrajectoryGen::class.java)
        .filter { !Modifier.isAbstract(it.modifiers) }
        .filter { it.kotlin.findAnnotation<Disabled>() == null }
        .map { TrajectoryContainer(it) }
        .toList()

    // The current core.trajectory container
    private var trajectoryGenContainer =
        trajectoryGenContainers.firstOrNull { it.isPrimary } ?: trajectoryGenContainers.first()
        set(newValue) {
            field = newValue; trajectoryDidChange()
        }

    // The current list of trajectories from the container
    private var trajectories: ArrayList<Trajectory> = trajectoryGenContainer.generator.createTrajectory()

    // Variable containing multiple aspects relating to the core.trajectory
    private val profile get() = TrajectoryProfile(trajectories)

    private fun configDidChange(trajectoryGenSetting: TrajectoryGenSetting<*>? = null) {
        if (trajectoryGenSetting != null) EnvironmentConfig.updateValue(trajectoryGenSetting)
        trajectories = trajectoryGenContainer.generator.createTrajectory()
        activeTrajectoryIndex = 0
    }

    private fun trajectoryDidChange() {
        configDidChange()
        this.fieldImage = Image(trajectoryGenContainer.generator.fieldImageName)
        settingsContainer.children.clear()
        settingsContainer.children.add(settingsForTrajectoryContainer)
    }

    // Empty group representing settings
    var settingsContainer = Group()
    private val maxNumberOfSettings = trajectoryGenContainers.maxByOrNull { it.settings.count() }?.settings?.count()

    private val settingsForTrajectoryContainer: GridPane
        get() {
            val settingsGridPane = GridPane()
            settingsGridPane.vgap = 4.0
            settingsGridPane.hgap = 12.0
            trajectoryGenContainer.settings.forEachIndexed { index, trajectoryGenSetting ->
                settingsGridPane.addRow(
                    index,
                    Label(trajectoryGenSetting.name).also {
                        it.prefWidth = 125.0
                    },
                    when (trajectoryGenSetting) {
                        is TrajectoryGenSettingBool ->
                            CheckBox()
                                .also {
                                    it.isSelected = trajectoryGenSetting.value
                                    it.onAction = EventHandler<ActionEvent> { _ ->
                                        trajectoryGenSetting.value = it.isSelected
                                        configDidChange(trajectoryGenSetting)
                                    }
                                }
                        is TrajectoryGenSettingDouble ->
                            TextField()
                                .also {
                                    it.text = trajectoryGenSetting.value.toString()
                                    it.setOnKeyPressed { event: KeyEvent? ->
                                        if (event?.code == KeyCode.ENTER) {
                                            trajectoryGenSetting.setValue(it.text)
                                            configDidChange(trajectoryGenSetting)
                                        }
                                    }
                                }
                        is TrajectoryGenSettingInt ->
                            TextField()
                                .also {
                                    it.text = trajectoryGenSetting.value.toString()
                                    it.setOnKeyReleased { event: KeyEvent? ->
                                        if (event == null) return@setOnKeyReleased
                                        else if (event.code == KeyCode.ENTER) {
                                            print("asdfasf")
                                            trajectoryGenSetting.setValue(it.text)
                                            configDidChange(trajectoryGenSetting)
                                        }
//                                    it.text = it.characters.filter { Character.isDigit(it) }.toString()
                                    }
                                }
                        is TrajectoryGenSettingEnum ->
                            ComboBox<String>()
                                .also {
                                    it.items.addAll(*trajectoryGenSetting.values.toTypedArray())
                                    it.value = trajectoryGenSetting.getValueAsString()
                                    it.valueProperty().addListener { _, _, newValue ->
                                        trajectoryGenSetting.setValue(newValue)
                                        configDidChange(trajectoryGenSetting)
                                    }
                                }
                        else -> Label("Not supported!")
                    }.also {
                        it.prefWidth = 150.0
//                    it.style = ":focused {-fx-background-color: -fx-outer-border, -fx-inner-border, -fx-body-color;-fx-background-insets: 0, 1, 2;-fx-background-radius: 5, 4, 3;}"
                        it.style = ":focused { -fx-background-insets: 0,0,1,2;}"
                    }
                )
            }
            (trajectoryGenContainer.settings.count() until (maxNumberOfSettings ?: 0)).forEach {
                settingsGridPane.addRow(trajectoryGenContainer.settings.count() + it, Label(""))
            }
            return settingsGridPane
        }


    val robotRect = Rectangle(100.0, 100.0, 10.0, 10.0)
    val startRect = Rectangle(100.0, 100.0, 10.0, 10.0)
    val endRect = Rectangle(100.0, 100.0, 10.0, 10.0)

    val mouseRect = Rectangle(100.0, 100.0, 10.0, 10.0)
    var mousePositionLabel = Label("")

    lateinit var fieldImage: Image
    lateinit var stage: Stage

    var animationIsPlaying = true
    val animationBtnText: String
        get() = if (animationIsPlaying) "Stop Animation" else "Play Animation"
    var shouldTrackMouse = true

    var activeTrajectoryIndex = 0

    var startTime = Double.NaN

    companion object {
        var WIDTH = 0.0
        var HEIGHT = 0.0
    }

    override fun start(stage: Stage?) {
        this.stage = stage!!
        fieldImage = Image("/field_center-stage.png")

        val root = Group()

        WIDTH = fieldImage.width
        HEIGHT = fieldImage.height
        GraphicsUtil.pixelsPerInch = WIDTH / GraphicsUtil.FIELD_WIDTH
        GraphicsUtil.halfFieldPixels = WIDTH / 2.0

        val canvas = Canvas(WIDTH, HEIGHT)
        val gc = canvas.graphicsContext2D
        val t1 = Timeline(KeyFrame(Duration.millis(10.0), EventHandler<ActionEvent> { run(gc) }))
        t1.cycleCount = Timeline.INDEFINITE

        val overlay = Rectangle(0.0, 0.0, WIDTH, HEIGHT)
        overlay.opacity = 0.0

        print(System.getProperty("user.dir"))

        val headerRoot = Group().also { group ->
            group.children.addAll(

                // Container with all header items
                VBox(
                    HBox(
                        ImageView("/Eagle Head.png")
                            .also {
                                it.isPreserveRatio = true
                                it.fitWidth = 80.0
                            },
                        VBox(
                            // Title label for program
                            Label("Eagle Robotics Path Visualizer").also { // Extra parameters for Label
                                it.font = Font.font(20.0)
                            },
                            Label("Based on github/RechargedGreen/RRPathVisualizer").also { // Extra parameters for Label
                                it.font = Font.font(12.0)
                            }
                        ).also {
                            it.spacing = 1.0
                            it.alignment = Pos.CENTER_LEFT
                        }
                    )
                        .also {
                            it.alignment = Pos.CENTER
                            it.spacing = 10.0
                        },
                    HBox(
                        Label("Current Generator:"),
                        ComboBox<TrajectoryContainer>()
                            .also {
                                it.items.addAll(
                                    *trajectoryGenContainers.toTypedArray()
                                )
                                it.valueProperty().addListener(
                                    ChangeListener { _, _, new ->
                                        print("CHANGE LISTENER CALLED!")
                                        this.trajectoryGenContainer = new
                                    }
                                )
                                it.value = trajectoryGenContainer
                            }
                    ).also {
                        it.spacing = 10.0
                        it.alignment = Pos.CENTER_LEFT
                    },
                    // Horizontal box containing animation controls
                    HBox(
                        // Button to play/pause animation
                        Button(animationBtnText)
                            .also { // Extra parameters for Button
                                it.onAction = EventHandler<ActionEvent> { _ ->
                                    when (animationIsPlaying) {
                                        true -> t1.pause()
                                        false -> t1.playFromStart()
                                    }
                                    animationIsPlaying = !animationIsPlaying
                                    it.text = animationBtnText
                                }
                            },
                        // Checkbox to activate mouse tracking
                        CheckBox("Mouse Movement")
                            .also { // Extra parameters for CheckBox
                                it.isSelected = shouldTrackMouse
                                it.onAction = EventHandler<ActionEvent> { _ ->
                                    shouldTrackMouse = it.isSelected
                                    mousePositionLabel.isVisible = shouldTrackMouse
                                    mouseRect.isVisible = shouldTrackMouse
                                }
                            },
                        // Label denoting the current position of the mouse
                        mousePositionLabel
                    ).also { // Extra parameters for HBox
                        it.spacing = 10.0
                        it.alignment = Pos.CENTER_LEFT
                    },
                    settingsContainer
                ).also { // Extra paremeters for VBox

                    it.padding = Insets(10.0)
                    it.spacing = 15.0
                }
            )
        }

        stage.scene = Scene(
            VBox(
                HBox(
                    headerRoot
                ),
                StackPane(
                    root
                )
            )

        )

        root.children.addAll(canvas, startRect, endRect, robotRect, mouseRect, overlay)

        overlay.onMouseMoved = EventHandler<MouseEvent> { event ->
            if (shouldTrackMouse) {
                val x = event.x
                val y = event.y
                val vec = Vector2d(x, y)
                val vee = vec.fromPixel
                println("$vec, $vee")
                GraphicsUtil.updateRobotRectPixelInput(mouseRect, Pose2d(vec, 0.0), Color.AQUAMARINE, 0.5)
                mousePositionLabel.text = String.format("(%.1f, %.1f)", vee.x, vee.y)
            }
        }

        stage.title = "PathVisualizer"
        stage.isResizable = false

        println("duration ${profile.duration}")

        stage.show()
        stage.requestFocus()
        t1.play()
    }

    fun run(gc: GraphicsContext) {
        if (startTime.isNaN())
            startTime = Clock.seconds

        GraphicsUtil.gc = gc
        gc.drawImage(fieldImage, 0.0, 0.0)

        gc.lineWidth = GraphicsUtil.LINE_THICKNESS

        gc.globalAlpha = 0.5
        GraphicsUtil.setColor(Color.RED)
        trajectoryGenContainer.generator.drawOffbounds()
        gc.globalAlpha = 1.0

        val trajectory = trajectories[activeTrajectoryIndex]

        var x = 0.0
        for (i in 0 until activeTrajectoryIndex)
            x += profile.trajectoryDurations[i]
        val prevDurations: Double = x

        val time = Clock.seconds
        val profileTime = time - startTime - prevDurations
        val duration = profile.trajectoryDurations[activeTrajectoryIndex]

        val start = trajectories.first().start()
        val end = trajectories.last().end()
        val current = trajectory[profileTime]

        if (profileTime >= duration) {
            activeTrajectoryIndex++
            if (activeTrajectoryIndex >= profile.numberOfTrajectories) {
                activeTrajectoryIndex = 0
                startTime = time
            }
        }

        trajectories.forEach { GraphicsUtil.drawSampledPath(it.path) }

        GraphicsUtil.updateRobotRect(startRect, start, GraphicsUtil.END_BOX_COLOR, 0.5)
        GraphicsUtil.updateRobotRect(endRect, end, GraphicsUtil.END_BOX_COLOR, 0.5)

        GraphicsUtil.updateRobotRect(robotRect, current, GraphicsUtil.ROBOT_COLOR, 0.75)
        GraphicsUtil.drawRobotVector(current)

        stage.title = "Profile duration : ${"%.2f".format(duration)} - time in profile ${"%.2f".format(profileTime)}"
    }
}

class TrajectoryProfile(trajectories: ArrayList<Trajectory>) {
    val trajectoryDurations = trajectories.map { it.duration() }
    val duration = trajectoryDurations.sum()
    val numberOfTrajectories = trajectories.size
}


fun main(args: Array<String>) {
    Application.launch(App::class.java, *args)
}