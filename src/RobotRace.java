import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
import robotrace.Base;
import robotrace.Vector;
import java.nio.FloatBuffer;

/**
 * Handles all of the RobotRace graphics functionality,
 * which should be extended per the assignment.
 *
 * OpenGL functionality:
 * - Basic commands are called via the gl object;
 * - Utility commands are called via the glu and
 *   glut objects;
 *
 * GlobalState:
 * The gs object contains the GlobalState as described
 * in the assignment:
 * - The camera viewpoint angles, phi and theta, are
 *   changed interactively by holding the left mouse
 *   button and dragging;
 * - The camera view width, vWidth, is changed
 *   interactively by holding the right mouse button
 *   and dragging upwards or downwards;
 * - The center point can be moved up and down by
 *   pressing the 'q' and 'z' keys, forwards and
 *   backwards with the 'w' and 's' keys, and
 *   left and right with the 'a' and 'd' keys;
 * - Other settings are changed via the menus
 *   at the top of the screen.
 *
 * Textures:
 * Place your "track.jpg", "brick.jpg", "head.jpg",
 * and "torso.jpg" files in the same folder as this
 * file. These will then be loaded as the texture
 * objects track, bricks, head, and torso respectively.
 * Be aware, these objects are already defined and
 * cannot be used for other purposes. The texture
 * objects can be used as follows:
 *
 * gl.glColor3f(1f, 1f, 1f);
 * track.bind(gl);
 * gl.glBegin(GL_QUADS);
 * gl.glTexCoord2d(0, 0);
 * gl.glVertex3d(0, 0, 0);
 * gl.glTexCoord2d(1, 0);
 * gl.glVertex3d(1, 0, 0);
 * gl.glTexCoord2d(1, 1);
 * gl.glVertex3d(1, 1, 0);
 * gl.glTexCoord2d(0, 1);
 * gl.glVertex3d(0, 1, 0);
 * gl.glEnd();
 *
 * Note that it is hard or impossible to texture
 * objects drawn with GLUT. Either define the
 * primitives of the object yourself (as seen
 * above) or add additional textured primitives
 * to the GLUT object.
 */
public class RobotRace extends Base {

    /** Array of the four robots. */
    private final Robot[] robots;

    /** Instance of the camera. */
    private final Camera camera;

    /** Instance of the race track. */
    private final RaceTrack raceTrack;

    /** Instance of the terrain. */
    private final Terrain terrain;

    /**
     * Constructs this robot race by initializing robots,
     * camera, track, and terrain.
     */
    public RobotRace() {

        // Create a new array of four robots
        robots = new Robot[4];

        // Initialize robot 0
        robots[0] = new Robot(Material.GOLD
            /* add other parameters that characterize this robot */);

        // Initialize robot 1
        robots[1] = new Robot(Material.SILVER
            /* add other parameters that characterize this robot */);

        // Initialize robot 2
        robots[2] = new Robot(Material.WOOD
            /* add other parameters that characterize this robot */);

        // Initialize robot 3
        robots[3] = new Robot(Material.ORANGE
            /* add other parameters that characterize this robot */);

        // Initialize the camera
        camera = new Camera();

        // Initialize the race track
        raceTrack = new RaceTrack();

        // Initialize the terrain
        terrain = new Terrain();
    }

    /**
     * Called upon the start of the application.
     * Primarily used to configure OpenGL.
     */
    @Override
    public void initialize() {
        // Enable blending.
        gl.glEnable(GL_BLEND);
        gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // Enable shading.
        gl.glShadeModel(GL_SMOOTH);
        gl.glEnable(GL_LIGHTING);
        gl.glEnable(GL_LIGHT0);

        //FloatBuffer ambient = FloatBuffer.wrap(new float[] {0.3f, 0.3f, 0.3f, 1.0f});
        //gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambient);

        FloatBuffer lightPos = FloatBuffer.wrap(new float[] {-1f, 0f, 1f, 1f});
        FloatBuffer whiteColor = FloatBuffer.wrap(new float[] {0.6f, 0.6f, 0.6f, 1f});

        gl.glLightfv(GL_LIGHT0, GL_POSITION, lightPos);

        gl.glLightfv(GL_LIGHT0, GL_AMBIENT, whiteColor);
        gl.glLightfv(GL_LIGHT0, GL_DIFFUSE, whiteColor);
        gl.glLightfv(GL_LIGHT0, GL_SPECULAR, whiteColor);

        // Enable anti-aliasing.
        gl.glEnable(GL_LINE_SMOOTH);
        gl.glEnable(GL_POLYGON_SMOOTH);
        gl.glHint(GL_LINE_SMOOTH_HINT, GL_NICEST);
        gl.glHint(GL_POLYGON_SMOOTH_HINT, GL_NICEST);

        // Enable depth testing.
        gl.glEnable(GL_DEPTH_TEST);
        gl.glDepthFunc(GL_LESS);

        // Enable textures.
        gl.glEnable(GL_TEXTURE_2D);
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glBindTexture(GL_TEXTURE_2D, 0);

        // normalize
        gl.glEnable(GL_NORMALIZE);
    }

    /**
     * Configures the viewing transform.
     */
    @Override
    public void setView() {
        // Select part of window.
        gl.glViewport(0, 0, gs.w, gs.h);

        // Set projection matrix.
        gl.glMatrixMode(GL_PROJECTION);
        gl.glLoadIdentity();

        // derive position of E
        // first we derive the vector V from the polar coordinates
        //camera.eye = new Vector(gs.vDist * Math.sin(gs.theta) * Math.cos(gs.phi),
                                //gs.vDist * Math.sin(gs.theta) * Math.sin(gs.phi),
                                //gs.vDist * Math.cos(gs.theta));
        camera.eye = new Vector(gs.vDist * Math.cos(gs.theta) * Math.cos(gs.phi),
                                gs.vDist * Math.sin(gs.theta) * Math.cos(gs.phi),
                                gs.vDist * Math.sin(gs.phi));
        camera.eye = camera.eye.add(gs.cnt);

        // camera center
        camera.center = gs.cnt;

        // for now, leave up as Z axis
        camera.up = Vector.Z;

        // znear = 0.1*gs.vDist
        // zfar = 10.0*gs.vDist
        //glu.gluPerspective(40, (float)gs.w / (float)gs.h, 0.1, 100);

        // calculate field of view
        // arctan((vWidth / 2) / (zNear+zFar) / 2) * 2
        double zNear = 0.1 * gs.vDist;
        double zFar = 10.0 * gs.vDist;
        double fovy = Math.atan((gs.vWidth / 2) / ((zNear + zFar) / 2)) * 2;
        fovy = Math.toDegrees(fovy);

        // Set the perspective.
        // Modify this to meet the requirements in the assignment.
        glu.gluPerspective(fovy, (float)gs.w / (float)gs.h, zNear, zFar);

        // tan(alpha) = (gs.vWidth / 2) / gs.vDist
        // alpha = arctan((gs.vWidth / 2) / gs.vDist)

        // Set camera.
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity();

        // Update the view according to the camera mode
        camera.update(gs.camMode);
        glu.gluLookAt(camera.eye.x(),    camera.eye.y(),    camera.eye.z(),
                      camera.center.x(), camera.center.y(), camera.center.z(),
                      camera.up.x(),     camera.up.y(),     camera.up.z());
    }

    /**
     * Draws the entire scene.
     */
    @Override
    public void drawScene() {
        // Background color.
        gl.glClearColor(1f, 1f, 1f, 0f);

        // Clear background.
        gl.glClear(GL_COLOR_BUFFER_BIT);

        // Clear depth buffer.
        gl.glClear(GL_DEPTH_BUFFER_BIT);

        // Set color to black.
        gl.glColor3f(0f, 0f, 0f);

        gl.glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);

        // Draw the axis frame
        if (gs.showAxes) {
            // enable material colors
            gl.glEnable(GL_COLOR_MATERIAL);
            drawAxisFrame();
            gl.glDisable(GL_COLOR_MATERIAL);
        }

        gl.glTranslated(-1.05, 0, 0);

        // Draw the robots
        robots[0].draw(false);

        gl.glTranslated(0.7, 0, 0);

        robots[1].draw(false);

        gl.glTranslated(0.7, 0, 0);

        robots[2].draw(false);

        gl.glTranslated(0.7, 0, 0);

        robots[3].draw(false);

        // Draw race track
        raceTrack.draw(gs.trackNr);

        // Draw terrain
        terrain.draw();
        /*

        // Unit box around origin.
        glut.glutWireCube(1f);

        // Move in x-direction.
        gl.glTranslatef(2f, 0f, 0f);

        // Rotate 30 degrees, around z-axis.
        gl.glRotatef(30f, 0f, 0f, 1f);

        // Scale in z-direction.
        gl.glScalef(1f, 1f, 2f);

        // Translated, rotated, scaled box.
        glut.glutWireCube(1f);*/
    }

    /**
     * Draw a box.
     *
     * Not necessarily a cube. This method will scale, and then scale back.
     *
     * @param w Width (to the x-axis)
     * @param d Depth (to the y-axis)
     * @param h Height (to the z-axis)
     * @param wired Wired cube or not
     */
    public void drawBox(double w, double d, double h, boolean wired)
    {
        gl.glPushMatrix();
        gl.glScaled(w, d, h);
        if (wired) {
            glut.glutWireCube(1f);
        } else {
            glut.glutSolidCube(1f);
        }
        gl.glPopMatrix();
    }

    /**
     * Draw a cone by a given direction vector, base and height.
     */
    public void drawCone(Vector v, double base, double height, int slices,
            int stacks, boolean wired)
    {
        gl.glPushMatrix();
        v = v.cross(Vector.Z);
        gl.glRotated(-90, v.x(), v.y(), v.z());
        if (wired) {
            glut.glutWireCone(base, height, slices, stacks);
        } else {
            glut.glutSolidCone(base, height, slices, stacks);
        }
        gl.glPopMatrix();
    }


    /**
     * Draws the axis along the given vector.
     */
    public void drawAxis(Vector v)
    {
        gl.glPushMatrix();
        // draw line
        gl.glBegin(GL_LINES);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(v.x(), v.y(), v.z());
        gl.glEnd();

        float size = 0.08f;

        // draw the cube

        // translate
        gl.glTranslated(v.x(), v.y(), v.z());
        // draw cube
        glut.glutSolidCube(size);

        // draw the cone
        // first, normalize, and scale by half the size of the cube
        Vector u = v.normalized();
        u = u.scale(size / 2);
        gl.glTranslated(u.x(), u.y(), u.z());

        Vector x = v.cross(Vector.Z);

        drawCone(v, size / 2, size, 30, 30, false);

        // translate back
        gl.glPopMatrix();
    }

    /**
     * Draws the x-axis (red), y-axis (green), z-axis (blue),
     * and origin (yellow).
     */
    public void drawAxisFrame() {
        // draw origin
        gl.glColor3f(1f, 1f, 0f);
        glut.glutSolidSphere(0.032f, 32, 32);

        gl.glColor3f(1f, 0f, 0f);
        drawAxis(Vector.X);

        gl.glColor3f(0f, 1f, 0f);
        drawAxis(Vector.Y);

        gl.glColor3f(0f, 0f, 1f);
        drawAxis(Vector.Z);

        gl.glColor3f(0f, 0f, 0f);
    }

    /**
     * Materials that can be used for the robots.
     */
    public enum Material {

        /**
         * Gold material properties.
         * Modify the default values to make it look like gold.
         */
        GOLD (
            new float[] {0.24725f, 0.1995f, 0.0745f, 1.0f},
            new float[] {0.75164f, 0.60648f, 0.22648f, 1.0f},
            new float[] {0.628281f, 0.555802f, 0.366065f, 1.0f}),

        /**
         * Silver material properties.
         * Modify the default values to make it look like silver.
         */
        SILVER (
            new float[] {0.19225f, 0.19225f, 0.19225f, 1.0f},
            new float[] {0.50754f, 0.50754f, 0.50754f, 1.0f},
            new float[] {0.508273f, 0.508273f, 0.508273f, 1.0f}),

        /**
         * Wood material properties.
         * Modify the default values to make it look like wood.
         */
        WOOD (
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.29411f, 0.172549f, 0.054901f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f}),

        /**
         * Orange material properties.
         * Modify the default values to make it look like orange.
         */
        ORANGE (
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.9f, 0.4f, 0.0f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f});

        float[] ambient;

        /** The diffuse RGBA reflectance of the material. */
        float[] diffuse;

        /** The specular RGBA reflectance of the material. */
        float[] specular;

        /**
         * Constructs a new material with diffuse and specular properties.
         */
        private Material(float[] ambient, float[] diffuse, float[] specular) {
            this.ambient = ambient;
            this.diffuse = diffuse;
            this.specular = specular;
        }

        /**
         * Set the correct surface color.
         */
        public void setSurfaceColor(GL2 gl)
        {
            gl.glMaterialfv(GL_FRONT, GL_AMBIENT, FloatBuffer.wrap(ambient));
            gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, FloatBuffer.wrap(diffuse));
            gl.glMaterialfv(GL_FRONT, GL_SPECULAR, FloatBuffer.wrap(specular));
        }
    }

    /**
     * Represents a Robot, to be implemented according to the Assignments.
     *
     * Robot design:
     *
     * (right) side (left or right, doesn't matter, except for the origin):
     *
     *  \   +   /
     *  =========
     *  =========
     *  /       \
     * *
     *
     * "/" and "\", leg or arm
     * "=" torso
     * "+" head
     *
     * "*" means the origin, which is on the front side of the robot
     *
     * Legs rotate from side to side, with an angle around 45 degrees. Under
     * its legs, there are nano-sized rollers that will translate this
     * side-to-side motion to forward motion.
     *
     * The hands rotate from the front to the back, above its torso, with an
     * angle around 45 degrees. This 'waving' doesn't actually have a
     * function. However, some robots do believe this does help them to get to
     * the finish faster. It turns out that most robots do this because they
     * belong to the "Amalgamated union of robots who really have nothing
     * better to do with their lives".
     *
     * (right) side (left or right, doesn't matter:
     *
     *  \   +   /
     *  =========
     *  =========
     *  /       \
     * *
     *
     * "/" and "\", leg or arm
     * "=" torso
     * "+" head
     *
     * front and back (look the same):
     * -|-
     * ===
     *  |
     *  *
     *
     * "-" part of head (with eye)
     * "=" torso
     * "|" leg or arm
     *
     * top view:
     *     .-
     * *====+====
     *     .-
     *
     * "=" torso
     * "+" head
     * "-" part of head
     * "." eye
     *
     * legs:  width: 0.1  depth: 0.1  height: 0.3
     * torso: width: 0.2  depth: 1.5  height: 0.4
     * arms:  width: 0.1  depth: 0.1  height: 0.3
     * head:  width: 0.4  depth: 0.3  height: 0.2
     *
     * You should also note that the robot's head is quite wide, and its eyes
     * are on the side. This way, they can see around their front arm
     *
     * The length of a meter, is defined as one unit (1.0) in the OpenGL
     * metrics.
     *
     * we draw the robot in the negative x direction, from the origin thus,
     * the front of its front foot is parallel with the (local) y-axis.
     */
    private class Robot {

        // the position of a body part is described as the front bottom
        // coordinate. Since the robot is mostly two-dimensional in starting
        // coordinates, the x is usually 0

        // general leg (box)
        public final static double LEG_WIDTH = 0.1;
        public final static double LEG_DEPTH = 0.1;
        public final static double LEG_HEIGHT = 0.3;

        // general arm (box)
        public final static double ARM_WIDTH = 0.1;
        public final static double ARM_DEPTH = 0.1;
        public final static double ARM_HEIGHT = 0.3;

        // torso (box)
        public final static double TORSO_POS_X = 0.0;
        public final static double TORSO_POS_Y = 0.0;
        public final static double TORSO_POS_Z = 0.3;

        public final static double TORSO_WIDTH = 0.2;
        public final static double TORSO_DEPTH = 1.5;
        public final static double TORSO_HEIGHT = 0.4;

        // back leg pos (box)
        public final static double B_LEG_POS_X = 0.0;
        public final static double B_LEG_POS_Y = 1.4;
        public final static double B_LEG_POS_Z = 0.0;

        // front arm pos (box)
        public final static double F_ARM_POS_X = 0.0;
        public final static double F_ARM_POS_Y = 0.0;
        public final static double F_ARM_POS_Z = 0.7;

        // back arm pos (box)
        public final static double B_ARM_POS_X = 0.0;
        public final static double B_ARM_POS_Y = 1.4;
        public final static double B_ARM_POS_Z = 0.7;

        // head (box)
        public final static double HEAD_POS_X = 0.0;
        public final static double HEAD_POS_Y = 0.5;
        public final static double HEAD_POS_Z = 0.7;

        public final static double HEAD_WIDTH = 0.4;
        public final static double HEAD_DEPTH = 0.3;
        public final static double HEAD_HEIGHT = 0.2;

        // eye
        public final static double EYE_DIR_X = 0.0;
        public final static double EYE_DIR_Y = 1.0;
        public final static double EYE_DIR_Z = 0;

        public final static double EYE_BASE = 0.04;
        public final static double EYE_HEIGHT = 0.03;

        public final static int EYE_SLICES = 10;
        public final static int EYE_STACKS = 10;

        public final static double L_EYE_POS_X = 0.15;
        public final static double L_EYE_POS_Y = 0.5;
        public final static double L_EYE_POS_Z = 0.8;

        public final static double R_EYE_POS_X = -0.15;
        public final static double R_EYE_POS_Y = 0.5;
        public final static double R_EYE_POS_Z = 0.8;



        /** The material from which this robot is built. */
        private final Material material;

        /**
         * Constructs the robot with initial parameters.
         */
        public Robot(Material material
            /* add other parameters that characterize this robot */) {
            this.material = material;

            // code goes here ...
        }

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         */
        public void draw(boolean stickFigure) {
            // apparently, stickFigure is not set to gs.showStick
            stickFigure = gs.showStick;

            // set the correct material properties
            material.setSurfaceColor(gl);

            // front leg
            gl.glPushMatrix();
            gl.glTranslated(0, -(LEG_DEPTH / 2), LEG_HEIGHT / 2);
            drawBox(LEG_WIDTH, LEG_DEPTH, LEG_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // torso
            gl.glPushMatrix();
            gl.glTranslated(TORSO_POS_X,
                            -(TORSO_POS_Y + TORSO_DEPTH / 2),
                            TORSO_POS_Z + TORSO_HEIGHT / 2);
            drawBox(TORSO_WIDTH, TORSO_DEPTH, TORSO_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // back leg
            gl.glPushMatrix();
            gl.glTranslated(B_LEG_POS_X,
                            -(B_LEG_POS_Y + LEG_DEPTH / 2),
                            B_LEG_POS_Z + LEG_HEIGHT / 2);
            drawBox(LEG_WIDTH, LEG_DEPTH, LEG_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // front arm
            gl.glPushMatrix();
            gl.glTranslated(F_ARM_POS_X,
                            -(F_ARM_POS_Y + ARM_DEPTH / 2),
                            F_ARM_POS_Z + ARM_HEIGHT / 2);
            drawBox(ARM_WIDTH, ARM_DEPTH, ARM_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // back arm
            gl.glPushMatrix();
            gl.glTranslated(B_ARM_POS_X,
                            -(B_ARM_POS_Y + ARM_DEPTH / 2),
                            B_ARM_POS_Z + ARM_HEIGHT / 2);
            drawBox(ARM_WIDTH, ARM_DEPTH, ARM_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // head
            gl.glPushMatrix();
            gl.glTranslated(HEAD_POS_X,
                            -(HEAD_POS_Y + HEAD_DEPTH / 2),
                            HEAD_POS_Z + HEAD_HEIGHT / 2);
            drawBox(HEAD_WIDTH, HEAD_DEPTH, HEAD_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // left eye
            gl.glPushMatrix();
            gl.glTranslated(L_EYE_POS_X,
                            -(L_EYE_POS_Y),
                            L_EYE_POS_Z);
            drawCone(new Vector(EYE_DIR_X, EYE_DIR_Y, EYE_DIR_Z),
                     EYE_BASE, EYE_HEIGHT, EYE_SLICES, EYE_STACKS, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // right eye
            gl.glPushMatrix();
            gl.glTranslated(R_EYE_POS_X,
                            -(R_EYE_POS_Y),
                            R_EYE_POS_Z);
            drawCone(new Vector(EYE_DIR_X, EYE_DIR_Y, EYE_DIR_Z),
                     EYE_BASE, EYE_HEIGHT, EYE_SLICES, EYE_STACKS, stickFigure);
            gl.glPopMatrix();
        }
    }

    /**
     * Implementation of a camera with a position and orientation.
     */
    private class Camera {

        /** The position of the camera. */
        public Vector eye = new Vector(3f, 6f, 5f);

        /** The point to which the camera is looking. */
        public Vector center = Vector.O;

        /** The up vector. */
        public Vector up = Vector.Z;

        /**
         * Updates the camera viewpoint and direction based on the
         * selected camera mode.
         */
        public void update(int mode) {
            robots[0].toString();

            // Helicopter mode
            if (1 == mode) {
                setHelicopterMode();

            // Motor cycle mode
            } else if (2 == mode) {
                setMotorCycleMode();

            // First person mode
            } else if (3 == mode) {
                setFirstPersonMode();

            // Auto mode
            } else if (4 == mode) {
                // code goes here...

            // Default mode
            } else {
                setDefaultMode();
            }
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the camera's default mode.
         */
        private void setDefaultMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the helicopter mode.
         */
        private void setHelicopterMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the motorcycle mode.
         */
        private void setMotorCycleMode() {
            // code goes here ...
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the first person mode.
         */
        private void setFirstPersonMode() {
            // code goes here ...
        }

    }

    /**
     * Implementation of a race track that is made from Bezier segments.
     */
    private class RaceTrack {

        /** Array with control points for the O-track. */
        private Vector[] controlPointsOTrack;

        /** Array with control points for the L-track. */
        private Vector[] controlPointsLTrack;

        /** Array with control points for the C-track. */
        private Vector[] controlPointsCTrack;

        /** Array with control points for the custom track. */
        private Vector[] controlPointsCustomTrack;

        /**
         * Constructs the race track, sets up display lists.
         */
        public RaceTrack() {
            // code goes here ...
        }

        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {

            // The test track is selected
            if (0 == trackNr) {
                // code goes here ...

            // The O-track is selected
            } else if (1 == trackNr) {
                // code goes here ...

            // The L-track is selected
            } else if (2 == trackNr) {
                // code goes here ...

            // The C-track is selected
            } else if (3 == trackNr) {
                // code goes here ...

            // The custom track is selected
            } else if (4 == trackNr) {
                // code goes here ...

            }
        }

        /**
         * Returns the position of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getPoint(double t) {
            // / 10 * cos(2*pi*t) \
            // | 14 * sin(2*pi*t) |
            // \ 1                /

            return new Vector(10 * Math.cos(2 * Math.PI * t),
                              14 * Math.sin(2 * Math.PI * t),
                              1);
        }

        /**
         * Returns the tangent of the curve at 0 <= {@code t} <= 1.
         */
        public Vector getTangent(double t) {
            // / 10 * cos(2*pi*t) \
            // | 14 * sin(2*pi*t) |
            // \ 1                /

            return new Vector(-10 * Math.sin(2 * Math.PI * t),
                              14 * Math.cos(2 * Math.PI * t),
                              0);
        }

    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {

        /**
         * Can be used to set up a display list.
         */
        public Terrain() {
            // code goes here ...
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            // code goes here ...
        }

        /**
         * Computes the elevation of the terrain at ({@code x}, {@code y}).
         */
        public float heightAt(float x, float y) {
            return 0; // <- code goes here
        }
    }

    /**
     * Main program execution body, delegates to an instance of
     * the RobotRace implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
    }

}
