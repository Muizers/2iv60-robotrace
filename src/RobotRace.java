import com.jogamp.opengl.util.texture.TextureIO;
import com.jogamp.opengl.util.texture.Texture;
import java.awt.Color;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import static javax.media.opengl.GL2.*;
import robotrace.Base;
import robotrace.Vector;
import java.nio.FloatBuffer;
import java.util.Random;
import static javax.media.opengl.GL.GL_REPEAT;
import static javax.media.opengl.GL.GL_TEXTURE_2D;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_S;
import static javax.media.opengl.GL.GL_TEXTURE_WRAP_T;
import static javax.media.opengl.GL2GL3.GL_TEXTURE_1D;

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
     * Speed multiplier.
     *
     * Usually something like 0.1
     */
    public final static double SPEED_MULTIPLIER = 0.05;

    /**
     * Minimum speed.
     *
     * Usually something like 0.1
     */
    public final static double SPEED_MINIMUM = 0.05;

    /**
     * Animation speed.
     */
    public final static double ANIMATION_SPEED = 4.0;

    /**
     * Last time
     */
    private int lastTime = 0;

    /**
     * Random source.
     */
    private Random rand;

    /**
     * Constructs this robot race by initializing robots,
     * camera, track, and terrain.
     */
    public RobotRace() {

        // Create a new array of four robots
        robots = new Robot[4];

        // Initialize robot 0
        robots[0] = new Robot(0, Material.GOLD
            /* add other parameters that characterize this robot */);

        // Initialize robot 1
        robots[1] = new Robot(1, Material.SILVER
            /* add other parameters that characterize this robot */);

        // Initialize robot 2
        robots[2] = new Robot(2, Material.WOOD
            /* add other parameters that characterize this robot */);

        // Initialize robot 3
        robots[3] = new Robot(3, Material.ORANGE
            /* add other parameters that characterize this robot */);

        // Initialize the camera
        camera = new Camera();

        // Initialize the race track
        raceTrack = new RaceTrack();

        // Initialize the terrain
        terrain = new Terrain();

        rand = new Random();
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

        FloatBuffer ambient = FloatBuffer.wrap(new float[] {0.3f, 0.3f, 0.3f, 1.0f});
        gl.glLightModelfv(GL_LIGHT_MODEL_AMBIENT, ambient);

        FloatBuffer lightPos = FloatBuffer.wrap(new float[] {1f, 0f, 1f, 1f});
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
       
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
        gl.glBindTexture(GL_TEXTURE_2D, 0);
        gl.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
        gl.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );

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

        // calculate field of view
        // arctan((vWidth / 2) / (zNear+zFar) / 2) * 2
        double zNear = 0.05 * gs.vDist;
        double zFar = 10.0 * gs.vDist;
        double fovy = Math.atan((gs.vWidth / 2) / ((zNear + zFar) / 2)) * 2;
        fovy = Math.toDegrees(fovy);

        // Set the perspective.
        glu.gluPerspective(fovy, (float)gs.w / (float)gs.h, zNear, zFar);

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

        determineSpeed();

        // Draw the robots
        
            for (int id = 0; id < 4; id++) {
                gl.glPushMatrix();
                // get the robot's position
                Vector position = robots[id].getPosition(gs.tAnim);
                // translate to the position
                gl.glTranslated(position.x(), position.y(), position.z());

                // rotate the robot
                Vector tangent = robots[id].getPositionTangent(gs.tAnim);
                double angle = Math.toDegrees(Math.atan2(-tangent.x(), tangent.y()));
                gl.glRotated(angle, 0, 0, 1);

                // draw the robot
                robots[id].draw(gs.showStick, gs.tAnim);

                gl.glPopMatrix();
            }

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
     * Determine speed.
     */
    public void determineSpeed()
    {
        if (Math.round(gs.tAnim) != lastTime) {
            for (int i = 0; i < 4; i++) {
                robots[i].setSpeed(SPEED_MINIMUM + rand.nextDouble() * SPEED_MULTIPLIER, gs.tAnim);
            }
            lastTime = Math.round(gs.tAnim);
        }
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
     * Rotate a body part.
     *
     * @param aTime
     * @param axis Axis to rotate over
     * @param trans Z-translation.
     */
    public void rotateBodyPart(float aTime, Vector axis, double trans)
    {
        gl.glTranslated(0, 0, trans);
        gl.glRotated(Math.sin(aTime * ANIMATION_SPEED) * 45, axis.x(), axis.y(), axis.z());
        gl.glTranslated(0, 0, trans * -1);
    }

    /**
     * Materials that can be used for the robots.
     */
    public enum Material {
        
        /**
         * Water material properties.
         */
        WATER (
            new float[] {0.5f, 0.5f, 0.5f, 0.5f},
            new float[] {0.5f, 0.5f, 0.5f, 0.5f},
            new float[] {0.1f, 0.1f, 0.1f, 0.5f}),

        /**
         * Gold material properties.
         */
        GOLD (
            new float[] {0.24725f, 0.1995f, 0.0745f, 1.0f},
            new float[] {0.75164f, 0.60648f, 0.22648f, 1.0f},
            new float[] {0.628281f, 0.555802f, 0.366065f, 1.0f}),

        /**
         * Silver material properties.
         */
        SILVER (
            new float[] {0.19225f, 0.19225f, 0.19225f, 1.0f},
            new float[] {0.50754f, 0.50754f, 0.50754f, 1.0f},
            new float[] {0.508273f, 0.508273f, 0.508273f, 1.0f}),

        /**
         * Wood material properties.
         */
        WOOD (
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.29411f, 0.172549f, 0.054901f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f}),

        /**
         * Orange material properties.
         */
        ORANGE (
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.9f, 0.4f, 0.0f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f}),
        
        /**
         * Blue material properties.
         */
        BLUE (
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.0f, 0.0f, 1.0f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f}),
        
        /**
         * Black material properties.
         */
        BLACK (
        new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f},
            new float[] {0.0f, 0.0f, 0.0f, 1.0f}),
        
        /**
         * Gray material properties.
         */
        GRAY (
            new float[] {0.08f, 0.08f, 0.08f, 1.0f},
            new float[] {0.50754f, 0.50754f, 0.50754f, 1.0f},
            new float[] {0.508273f, 0.508273f, 0.508273f, 1.0f}),
        
        /**
         * White material properties.
         */
        WHITE (
        new float[] {1.0f, 1.0f, 1.0f, 1.0f},
            new float[] {1.0f, 1.0f, 1.0f, 1.0f},
            new float[] {1.0f, 1.0f, 1.0f, 1.0f});

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
        
        /** The identifier of this robot. In the range [0,3]. */
        private int id;
        
        /** Distance that the robot travelled on the track.
         * This distance is taken in the range [0,1).
         */
        private double distance = 0;
        
        /** Current speed of the robot in units/seconds. */
        private double speed = 0;
        
        /** The last time the robot position was updated. */
        private double lastATime = 0;
        
        /** The position this robot had when last updated. */
        private Vector lastCalculatedPosition = null;
        
        /** The position tangent this robot had when last updated. */
        private Vector lastCalculatedPositionTangent = null;

        // the position of a body part is described as the front bottom
        // coordinate. Since the robot is mostly two-dimensional in starting
        // coordinates, the x is usually 0

        // general leg (box)
        // the leg height is 0.4 instead of 0.3, so the animation looks
        // better
        public final static double LEG_WIDTH = 0.1;
        public final static double LEG_DEPTH = 0.1;
        public final static double LEG_HEIGHT = 0.4;

        // general arm (box)
        // the leg height is 0.4 instead of 0.3, so the animation looks
        // better
        public final static double ARM_WIDTH = 0.1;
        public final static double ARM_DEPTH = 0.1;
        public final static double ARM_HEIGHT = 0.4;

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
        public final static double F_ARM_POS_Z = 0.6; // was 0.7 (done so for animation)

        // back arm pos (box)
        public final static double B_ARM_POS_X = 0.0;
        public final static double B_ARM_POS_Y = 1.4;
        public final static double B_ARM_POS_Z = 0.6; // was 0.7 (done so for animation)

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
        public Robot(int id, Material material) {
            this.id = id;
            this.material = material;
        }
        
        /**
         * Returns the last calculated position of this robot.
         */
        public Vector getLastCalculatedPosition() {
            return lastCalculatedPosition;
        }
        
        /**
         * Returns the last calculated position tangent of this robot.
         */
        public Vector getLastCalculatedPositionTangent() {
            return lastCalculatedPositionTangent;
        }
        
        /**
         * Calculate the new position of this robot.
         * 
         * @param aTime Time for animation and movement, in seconds
         */
        public void updatePosition(float aTime) {
            distance += (aTime-lastATime)*speed; // Increment the distance by the time passed times the speed
            distance = distance-Math.floor(distance); // Make sure the distance is still in the range [0,1)
            lastATime = aTime; // Update the last aTime
            lastCalculatedPosition = raceTrack.getPointOnCurrentCurve(distance, id+0.5); // update the position Vector object
            lastCalculatedPositionTangent = raceTrack.getTangentOnCurrentCurve(distance);

            // TODO: remove this
            //if (id == 0) {
                //lastCalculatedPosition = Vector.O;
            //}
        }
        
        /**
         * Returns the new position of this robot.
         * 
         * @param aTime Time for animation and movement, in seconds
         */
        public Vector getPosition(float aTime) {
            updatePosition(aTime);
            return lastCalculatedPosition;
        }
        
        /**
         * Returns the new position tangent of this robot.
         * 
         * @param aTime Time for animation and movement, in seconds
         */
        public Vector getPositionTangent(float aTime) {
            updatePosition(aTime);
            return lastCalculatedPositionTangent;
        }

        /**
         * Get the head position.
         *
         * @return Head position.
         */
        public Vector getHeadPosition(float aTime) {
            Vector position = getPosition(aTime);
            return position.add(new Vector(0, 0, HEAD_POS_Z + HEAD_HEIGHT / 2));
        }
        
        /**
         * Changes the speed of this robot.
         * 
         * @param aTime Time for animation and movement, in seconds
         */
        
        public void setSpeed(double speed, float aTime) {
            updatePosition(aTime); // update the position with the old speed
            this.speed = speed; // update the speed
        }

        /**
         * Draws this robot (as a {@code stickfigure} if specified).
         *
         * @param aTime Time for animation and movement, in seconds
         */
        public void draw(boolean stickFigure, float aTime) {
            
            // set the correct material properties
            material.setSurfaceColor(gl);

            // front leg
            gl.glPushMatrix();
            gl.glTranslated(0, -(LEG_DEPTH / 2), LEG_HEIGHT / 2);
            rotateBodyPart(aTime, Vector.Y, 0.15);
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
            rotateBodyPart(aTime * -1, Vector.Y, 0.15);
            drawBox(LEG_WIDTH, LEG_DEPTH, LEG_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // front arm
            gl.glPushMatrix();
            gl.glTranslated(F_ARM_POS_X,
                            -(F_ARM_POS_Y + ARM_DEPTH / 2),
                            F_ARM_POS_Z + ARM_HEIGHT / 2);
            rotateBodyPart(aTime * -1, Vector.X, -0.15);
            drawBox(ARM_WIDTH, ARM_DEPTH, ARM_HEIGHT, stickFigure);
            gl.glPopMatrix();

            // set the correct material properties
            material.setSurfaceColor(gl);

            // back arm
            gl.glPushMatrix();
            gl.glTranslated(B_ARM_POS_X,
                            -(B_ARM_POS_Y + ARM_DEPTH / 2),
                            B_ARM_POS_Z + ARM_HEIGHT / 2);
            rotateBodyPart(aTime, Vector.X, -0.15);
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
         * Robot to show in Helicopter and MotorCycle modes.
         */
        public int robotNum;

        /**
         * Last time.
         */
        private int lastTime = 0;

        /**
         * Last robot change time.
         */
        private int lastRobotTime = 0;

        /**
         * Mode.
         */
        private int mode = 0;

        /**
         * Updates the camera viewpoint and direction based on the
         * selected camera mode.
         *
         * For the camera mode, we periodically switch all robots for
         * the helicopter, motor cycle and first person mode. 
         */
        public void update(int mode) {
            setRobotNum();
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
                setAutoMode();
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
            center = gs.cnt;
            up = Vector.Z;
            // derive position of the camera eye
            // first we derive the vector V from the polar coordinates
            eye = new Vector(gs.vDist * Math.cos(gs.theta) * Math.cos(gs.phi),
                             gs.vDist * Math.sin(gs.theta) * Math.cos(gs.phi),
                             gs.vDist * Math.sin(gs.phi));
            eye.add(center);
        }

        private Vector calculateCurrentRobotPosition() {
            return robots[robotNum].getPosition(gs.tAnim);
        }
        private Vector calculateCurrentRobotPositionTangent() {
            return robots[robotNum].getPositionTangent(gs.tAnim);
        }
        private Vector calculateCurrentRobotHeadPosition() {
            return robots[robotNum].getHeadPosition(gs.tAnim);
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the helicopter mode.
         */
        private void setHelicopterMode() {
            // center is the robot position
            center = calculateCurrentRobotPosition();
            up = calculateCurrentRobotPositionTangent();

            eye = center;
            eye = eye.add(new Vector(0, 0, 50));
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the motorcycle mode.
         *
         * We take the tangent of the current robot position on the
         * track, and calculate a orthogonal vector parallel with the
         * XOY plane. On this vector, the camera eye is placed.
         */
        private void setMotorCycleMode() {
            // center is the robot position
            center = calculateCurrentRobotPosition();
            up = Vector.Z;

            // calculate the eye position
            eye = calculateCurrentRobotPositionTangent()
                .cross(Vector.Z).normalized().scale(20);
            eye = center.add(eye);
            eye = eye.add(new Vector(0, 0, 1));
        }

        /**
         * Computes {@code eye}, {@code center}, and {@code up}, based
         * on the first person mode.
         */
        private void setFirstPersonMode() {
            // eye is the robot's eye position
            eye = calculateCurrentRobotHeadPosition();
            up = Vector.Z;

            // center is in the direction of the tangent
            center = calculateCurrentRobotPositionTangent();
            center.normalized().scale(10);
            center = eye.add(center);
        }

        /**
         * Set the auto mode.
         */
        private void setAutoMode()
        {
            if (lastTime + 10 <= Math.round(gs.tAnim)) {
                lastTime = Math.round(gs.tAnim);
                mode = rand.nextInt(4);
            }
            switch (mode) {
                case 0:
                    setDefaultMode();
                    break;
                case 1:
                    setHelicopterMode();
                    break;
                case 2:
                    setMotorCycleMode();
                    break;
                case 3:
                    setFirstPersonMode();
                    break;
            }
        }

        /**
         * Set robot number.
         */
        public void setRobotNum()
        {
            if (lastRobotTime + 5 <= Math.round(gs.tAnim)) {
                lastRobotTime = Math.round(gs.tAnim);
                robotNum = rand.nextInt(4);
            }
        }
    }

    /**
     * Implementation of a race track that is made from Bezier segments.
     */
    private class RaceTrack {
        
        /** Material of tracks, from innermost to outermost. */
        private Material[] materials = new Material[] {
            Material.ORANGE,
            Material.WOOD,
            Material.SILVER,
            Material.GOLD
        };
        
        /** Material of the start line. */
        private Material startLineMaterial = Material.WHITE;
        
        /** Material of the track edge. */
        private Material trackEdgeMaterial = Material.GRAY;
        
        /** Number of display lists to create per track. */
        private int displayListPerTrackAmount = 7;

        /** Array with control points for the O-track. */
        private Vector[] controlPointsOTrack = new Vector[] {
            new Vector(-12, -12, 2),
            new Vector(-12, -4, 2),
            new Vector(-12, 4, 2),
            new Vector(-12, 12, 2),
            new Vector(-12, 24, 2),
            new Vector(12, 24, 2),
            new Vector(12, 12, 2),
            new Vector(12, 4, 2),
            new Vector(12, -4, 2),
            new Vector(12, -12, 2),
            new Vector(12, -24, 2),
            new Vector(-12, -24, 2),
            new Vector(-12, -12, 2)
        };

        /** Array with control points for the L-track. */
        private Vector[] controlPointsLTrack = new Vector[] {
            new Vector(-7.5, 7.5, 2),
            new Vector(-7.5, 12.5, 2),
            new Vector(0, 12.5, 2),
            new Vector(0, 7.5, 2),
            new Vector(0, 2.5, 2),
            new Vector(2.5, 0, 2),
            new Vector(7.5, 0, 2),
            new Vector(12.5, 0, 2),
            new Vector(12.5, -7.5, 2),
            new Vector(7.5, -7.5, 2),
            new Vector(5, -7.5, 2),
            new Vector(2.5, -7.5, 2),
            new Vector(0, -7.5, 2),
            new Vector(-7.5, -7.5, 2),
            new Vector(-7.5, -7.5, 2),
            new Vector(-7.5, 0, 2),
            new Vector(-7.5, 2.5, 2),
            new Vector(-7.5, 5, 2),
            new Vector(-7.5, 7.5, 2),
        };

        /** Array with control points for the C-track. */
        private Vector[] controlPointsCTrack = new Vector[] {
            new Vector(-7.5, 15, 2),
            new Vector(-5, 15, 2),
            new Vector(-2.5, 15, 2),
            new Vector(0, 15, 2),
            new Vector(7.5, 15, 2),
            new Vector(7.5, 7.5, 2),
            new Vector(0, 7.5, 2),
            new Vector(-7.5, 7.5, 2),
            new Vector(-7.5, -7.5, 2),
            new Vector(0, -7.5, 2),
            new Vector(7.5, -7.5, 2),
            new Vector(7.5, -15, 2),
            new Vector(0, -15, 2),
            new Vector(-2.5, -15, 2),
            new Vector(-5, -15, 2),
            new Vector(-7.5, -15, 2),
            new Vector(-10, -15, 2),
            new Vector(-15, -12.5, 2),
            new Vector(-15, -7.5, 2),
            new Vector(-15, -2.5, 2),
            new Vector(-15, 2.5, 2),
            new Vector(-15, 7.5, 2),
            new Vector(-15, 10, 2),
            new Vector(-10, 15, 2),
            new Vector(-7.5, 15, 2)
        };

        /** Array with control points for the custom track. */
        private Vector[] controlPointsCustomTrack = new Vector[] {
            new Vector(0, 15, 2),
            new Vector(2.5, 15, 2),
            new Vector(5, 15, 2),
            new Vector(7.5, 15, 2),
            new Vector(15, 15, 2),
            new Vector(15, 7.5, 2),
            new Vector(7.5, 7.5, 2),
            new Vector(0, 7.5, 2),
            new Vector(0, -7.5, 2),
            new Vector(7.5, -7.5, 2),
            new Vector(15, -7.5, 2),
            new Vector(15, -15, 2),
            new Vector(7.5, -15, 2),
            new Vector(5, -15, 2),
            new Vector(2.5, -15, 2),
            new Vector(0, -15, 2),
            new Vector(-2.5, -15, 2),
            new Vector(-5, -15, 2),
            new Vector(-7.5, -15, 2),
            new Vector(-15, -15, 2),
            new Vector(-15, -7.5, 2),
            new Vector(-7.5, -7.5, 2),
            new Vector(0, -7.5, 2),
            new Vector(0, 7.5, 2),
            new Vector(-7.5, 7.5, 2),
            new Vector(-15, 7.5, 2),
            new Vector(-15, 15, 2),
            new Vector(-7.5, 15, 2),
            new Vector(-5, 15, 2),
            new Vector(-2.5, 15, 2),
            new Vector(0, 15, 2)
        };
        
        /** Number of segments to be used to draw the race tracks. */
        private int SEGMENTS = 300;
        
        /** Display list for the test track. */
        private int displayListTestTrack = 0;
        
        /** Whether the display list for the test track was created yet. */
        private boolean displayListTestTrackSetUp = false;
        
        /** Display list for the test track. */
        private int displayListOTrack = 0;
        
        /** Whether the display list for the test track was created yet. */
        private boolean displayListOTrackSetUp = false;
        
        /** Display list for the test track. */
        private int displayListLTrack = 0;
        
        /** Whether the display list for the test track was created yet. */
        private boolean displayListLTrackSetUp = false;
        
        /** Display list for the test track. */
        private int displayListCTrack = 0;
        
        /** Whether the display list for the test track was created yet. */
        private boolean displayListCTrackSetUp = false;
        
        /** Display list for the test track. */
        private int displayListCustomTrack = 0;
        
        /** Whether the display list for the test track was created yet. */
        private boolean displayListCustomTrackSetUp = false;
        
        /** The last selected track nr. */
        private int currentTrackNr = 0;
        
        /** The last selected display list. */
        private int currentDisplayList;
        
        /** The last selected array of control points. */
        private Vector[] currentControlPoints;
        
        /** The texture for the track edges. */
        private Texture trackEdgeTexture = null;
        
        /** Whether the texture for the track edges has been set up. */
        private boolean trackEdgeTextureSetUp = false;
        
        /** The texture for the tracks. */
        private Texture trackTexture = null;
        
        /** Whether the texture for the tracks has been set up. */
        private boolean trackTextureSetUp = false;

        /**
         * Constructs the race track.
         */
        public RaceTrack() {
            
        }

        /**
         * Draws this track, based on the selected track number.
         */
        public void draw(int trackNr) {
            
            if (!trackEdgeTextureSetUp) {
                /** Loads the track edge texture **/
                trackEdgeTexture = load2DTexture("brick.png", gl);
                trackEdgeTextureSetUp = true;
            }
            
            if (!trackTextureSetUp) {
                /** Loads the track texture **/
                //trackTexture = load2DTexture("track.png", gl);
                trackTextureSetUp = true;
            }
            
            currentTrackNr = trackNr;

            // The test track is selected
            if (0 == trackNr) {
                currentDisplayList = displayListTestTrack;
                if (!displayListTestTrackSetUp) {
                    displayListTestTrack = compileCurrentDisplayList();
                    // Set the displayListTestTrackSetUp variable to true so the display lists won't be created again
                    displayListTestTrackSetUp = true;
                }
                
            
            // The O-track is selected
            } else if (1 == trackNr) {
                currentDisplayList = displayListOTrack;
                if (!displayListOTrackSetUp) {
                    displayListOTrack = compileCurrentDisplayList();
                    // Set the displayListOTrackSetUp variable to true so the display lists won't be created again
                    displayListOTrackSetUp = true;
                }
                
            
            // The L-track is selected
            } else if (2 == trackNr) {
                currentDisplayList = displayListLTrack;
                if (!displayListLTrackSetUp) {
                    displayListLTrack = compileCurrentDisplayList();
                    // Set the displayListOTrackSetUp variable to true so the display lists won't be created again
                    displayListLTrackSetUp = true;
                }

            // The C-track is selected
            } else if (3 == trackNr) {
                currentDisplayList = displayListCTrack;
                if (!displayListCTrackSetUp) {
                    displayListCTrack = compileCurrentDisplayList();
                    // Set the displayListOTrackSetUp variable to true so the display lists won't be created again
                    displayListCTrackSetUp = true;
                }

            // The custom track is selected
            } else if (4 == trackNr) {
                currentDisplayList = displayListCustomTrack;
                if (!displayListCustomTrackSetUp) {
                    displayListCustomTrack = compileCurrentDisplayList();
                    // Set the displayListOTrackSetUp variable to true so the display lists won't be created again
                    displayListCustomTrackSetUp = true;
                }

            }
            
            // Execute the display lists for the current track
            executeCurrentDisplayList();
        }
        
        public void executeCurrentDisplayList() {
            
            // Execute the display lists of the curves
                        for (int curve = 0; curve < 4; curve++) {
                            // Pass the material for this curve to OpenGL
                            materials[curve].setSurfaceColor(gl);
                            // Call the display list
                            gl.glCallList(currentDisplayList+curve);
                        }
                    // Execute the display lists of the start line
                        // Pass the material for the start line
                        startLineMaterial.setSurfaceColor(gl);
                        // Call the display list
                        gl.glCallList(currentDisplayList+4);
                    // Execute the display lists of the track edges
                        // Pass the material for the track edges
                        gl.glDisable(GL_TEXTURE_1D);
                        gl.glEnable(GL_TEXTURE_2D);
                        gl.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT );
                        gl.glTexParameterf( GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT );
                        trackEdgeTexture.bind(gl);
                        trackEdgeMaterial.setSurfaceColor(gl);
                        for (boolean insideOrOutside : new boolean [] {true, false}) {
                            // Call the display list
                            gl.glCallList(currentDisplayList+5+(insideOrOutside?1:0));
                        }
                        gl.glBindTexture(GL_TEXTURE_2D, 0);
            
        }
        
        public int compileCurrentDisplayList() {
            
                    // Reserve the indices for the display lists, one for each curve
                    currentDisplayList = gl.glGenLists(displayListPerTrackAmount);
                    // Compile the display lists for the 4 curves
                        for (int curve = 0; curve < 4; curve++) {
                            // Start compiling the display lists
                            gl.glNewList(currentDisplayList+curve, GL_COMPILE);
                            // Use a triangle strip and create a closed ring out of triangles
                            gl.glBegin(GL2.GL_TRIANGLE_STRIP);
                                // Normal is pointing up for track
                                gl.glNormal3d(0, 0, 1);
                                for (int i = 0; i < SEGMENTS; i++) {
                                    // SEGMENTS times: add a vertex describing an inner and outer point of this curve
                                    double t = i/((double) SEGMENTS);
                                    Vector inner = getPointOnCurrentCurve(t, curve);
                                    Vector outer = getPointOnCurrentCurve(t, curve+1);
                                    // Add these two vectors, that are on the same distance on the track, as vertices to the triangle strip
                                    gl.glVertex3d(inner.x(), inner.y(), inner.z());
                                    gl.glVertex3d(outer.x(), outer.y(), outer.z());
                                }
                                // Add the first inner and outer points of this curve again to close the ring
                                Vector inner = getPointOnCurrentCurve(0, curve);
                                Vector outer = getPointOnCurrentCurve(0, curve+1);
                                gl.glVertex3d(inner.x(), inner.y(), inner.z());
                                gl.glVertex3d(outer.x(), outer.y(), outer.z());
                            // Finish the triangle strip
                            gl.glEnd();
                            // Finish compiling the display list
                            gl.glEndList();
                        }
                    // Compile the display list for the start line
                        // Normal is pointing up for start line
                        gl.glNormal3d(0, 0, 1);
                        gl.glNewList(currentDisplayList+4, GL_COMPILE);
                        // Draw the start line
                            gl.glBegin(GL2.GL_TRIANGLE_STRIP);
                            {
                                // Add the first inner and outer points as points on the startline on this piece of the track
                                Vector inner = getPointOnCurrentCurve(0, 0).add(Vector.Z.scale(0.0001));
                                Vector outer = getPointOnCurrentCurve(0, 4).add(Vector.Z.scale(0.0001));
                                gl.glVertex3d(inner.x(), inner.y(), inner.z());
                                gl.glVertex3d(outer.x(), outer.y(), outer.z());
                                // Add an inner and outer point just past the initial points as the end of the startline in the triangle strip
                                inner = getPointOnCurrentCurve(0.001, 0).add(Vector.Z.scale(0.0001));
                                outer = getPointOnCurrentCurve(0.001, 4).add(Vector.Z.scale(0.0001));
                                gl.glVertex3d(inner.x(), inner.y(), inner.z());
                                gl.glVertex3d(outer.x(), outer.y(), outer.z());
                            }
                            // Finish the start line
                            gl.glEnd();
                        // Finish compiling the display list
                        gl.glEndList();
                    // Compile the display lists for the track edges
                        for (boolean insideOrOutside : new boolean[] {true, false}) {
                            gl.glNewList(currentDisplayList+5+(insideOrOutside?1:0), GL_COMPILE);
                                // Use a triangle strip and create a closed ring out of triangles
                                gl.glBegin(GL2.GL_TRIANGLE_STRIP);
                                    for (int i = 0; i < SEGMENTS; i++) {
                                        // SEGMENTS times: add a vertex describing an top and bottom point of the edge
                                        double t = i/((double) SEGMENTS);
                                        double nextT = (i+1)/((double) SEGMENTS);
                                        if (nextT >= 1) {
                                            nextT -= 1;
                                        }
                                        Vector top = getPointOnCurrentCurve(t, insideOrOutside?4:0);
                                        Vector nextTop = getPointOnCurrentCurve(nextT, insideOrOutside?4:0);
                                        Vector bottom = new Vector(top.x(), top.y(), -1);
                                        if (i == 0) {
                                            double prevT = (i-1)/((double) SEGMENTS);
                                            if (prevT < 0) {
                                                prevT += 1;
                                            }
                                            Vector prevTop = getPointOnCurrentCurve(prevT, insideOrOutside?4:0);
                                            Vector normal = top.subtract(prevTop).cross(bottom.subtract(top));
                                            if(currentTrackNr != 0)normal=normal.scale(-1);
                                            if(insideOrOutside)normal=normal.scale(-1);
                                            gl.glNormal3d(normal.x(), normal.y(), normal.z());
                                        }
                                        Vector normal = nextTop.subtract(top).cross(bottom.subtract(top));
                                        if(currentTrackNr != 0)normal=normal.scale(-1);
                                        if(insideOrOutside)normal=normal.scale(-1);
                                        // Add these two vectors, that are on the same distance on the track, as vertices to the triangle strip
                                        gl.glTexCoord2d(i/8D, 1);
                                        gl.glVertex3d(top.x(), top.y(), top.z());
                                        gl.glTexCoord2d(i/8D, 0);
                                        gl.glVertex3d(bottom.x(), bottom.y(), bottom.z());
                                        gl.glNormal3d(normal.x(), normal.y(), normal.z());
                                    }
                                    // Add the first inner and outer points of this curve again to close the ring
                                    Vector top = getPointOnCurrentCurve(0, insideOrOutside?4:0);
                                    Vector bottom = new Vector(top.x(), top.y(), -1);
                                    gl.glTexCoord2d(SEGMENTS/8D, 1);
                                    gl.glVertex3d(top.x(), top.y(), top.z());
                                    gl.glTexCoord2d(SEGMENTS/8D, 0);
                                    gl.glVertex3d(bottom.x(), bottom.y(), bottom.z());
                                // Finish the triangle strip
                                gl.glEnd();
                            // Finish compiling the display list
                            gl.glEndList();
                        }
                            
                    return currentDisplayList;
        }
        
        /**
         * Returns the position of the current curve.<vr>
         * 0 = the innermost curve
         * 5 = the outermost curve.
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnCurrentCurve(double t, double curve) {
            if (0 == currentTrackNr) {
                return getPointOnTestCurve(t, curve);
            } else if (1 == currentTrackNr) {
                return getPointOnOCurve(t, curve);
            } else if (2 == currentTrackNr) {
                return getPointOnLCurve(t, curve);
            } else if (3 == currentTrackNr) {
                return getPointOnCCurve(t, curve);
            } else if (4 == currentTrackNr) {
                return getPointOnCustomCurve(t, curve);
            }
            return null;
        }
        
        /**
         * Returns the tangent of the current curve.<vr>
         * 0 = the innermost curve
         * 5 = the outermost curve.
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getTangentOnCurrentCurve(double t) {
            if (0 == currentTrackNr) {
                return getTestTangent(t);
            } else if (1 == currentTrackNr) {
                currentControlPoints = controlPointsOTrack;
            } else if (2 == currentTrackNr) {
                currentControlPoints = controlPointsLTrack;
            } else if (3 == currentTrackNr) {
                 currentControlPoints = controlPointsCTrack;
            } else if (4 == currentTrackNr) {
                 currentControlPoints = controlPointsCustomTrack;
            }
            if (t >= 1) {
                    t -= 1;
                }
            int numberOfSegments = (currentControlPoints.length-1)/3;
            int segment = (int) Math.floor(t*numberOfSegments);
            // get Bezier points
            Vector P0 = currentControlPoints[segment*3];
            Vector P1 = currentControlPoints[segment*3+1];
            Vector P2 = currentControlPoints[segment*3+2];
            Vector P3 = currentControlPoints[segment*3+3];
            double bezierT = (t-(((double) segment)/numberOfSegments))*numberOfSegments;
            // get tangent
            Vector tangent = getCubicBezierTng(bezierT, P0, P1, P2, P3);
            return tangent;
        }
        
         /**
         * Returns the position of the {@code curve}'th outermost O curve at 0 <= {@code t} <= 1.<br>
         * 0 = the innermost curve
         * 5 = the outermost curve
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnOCurve(double t, double curve) {
            if (t >= 1) {
                t -= 1;
            }
            int numberOfSegments = (controlPointsOTrack.length-1)/3;
            int segment = (int) Math.floor(t*numberOfSegments);
            
            Vector P0 = controlPointsOTrack[segment*3];
            Vector P1 = controlPointsOTrack[segment*3+1];
            Vector P2 = controlPointsOTrack[segment*3+2];
            Vector P3 = controlPointsOTrack[segment*3+3];
            double bezierT = (t-(((double) segment)/numberOfSegments))*numberOfSegments;
            Vector point = getCubicBezierPnt(bezierT, P0, P1, P2, P3);
            if (curve == 0) {
                return point;
            }
            Vector tangent = getCubicBezierTng(bezierT, P0, P1, P2, P3).scale(-1);
            Vector normal = tangent.cross(Vector.Z).normalized();
            return point.add(normal.scale(curve));
        }
        
        /**
         * Returns the position of the {@code curve}'th outermost L curve at 0 <= {@code t} <= 1.<br>
         * 0 = the innermost curve
         * 5 = the outermost curve
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnLCurve(double t, double curve) {
            if (t >= 1) {
                t -= 1;
            }
            int numberOfSegments = (controlPointsLTrack.length-1)/3;
            int segment = (int) Math.floor(t*numberOfSegments);
            
            Vector P0 = controlPointsLTrack[segment*3];
            Vector P1 = controlPointsLTrack[segment*3+1];
            Vector P2 = controlPointsLTrack[segment*3+2];
            Vector P3 = controlPointsLTrack[segment*3+3];
            double bezierT = (t-(((double) segment)/numberOfSegments))*numberOfSegments;
            Vector point = getCubicBezierPnt(bezierT, P0, P1, P2, P3);
            if (curve == 0) {
                return point;
            }
            Vector tangent = getCubicBezierTng(bezierT, P0, P1, P2, P3).scale(-1);
            Vector normal = tangent.cross(Vector.Z).normalized();
            return point.add(normal.scale(curve));
        }
        
        /**
         * Returns the position of the {@code curve}'th outermost C curve at 0 <= {@code t} <= 1.<br>
         * 0 = the innermost curve
         * 5 = the outermost curve
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnCCurve(double t, double curve) {
            if (t >= 1) {
                t -= 1;
            }
            int numberOfSegments = (controlPointsCTrack.length-1)/3;
            int segment = (int) Math.floor(t*numberOfSegments);
            
            Vector P0 = controlPointsCTrack[segment*3];
            Vector P1 = controlPointsCTrack[segment*3+1];
            Vector P2 = controlPointsCTrack[segment*3+2];
            Vector P3 = controlPointsCTrack[segment*3+3];
            double bezierT = (t-(((double) segment)/numberOfSegments))*numberOfSegments;
            Vector point = getCubicBezierPnt(bezierT, P0, P1, P2, P3);
            if (curve == 0) {
                return point;
            }
            Vector tangent = getCubicBezierTng(bezierT, P0, P1, P2, P3).scale(-1);
            Vector normal = tangent.cross(Vector.Z).normalized();
            return point.add(normal.scale(curve));
        }
        
        /**
         * Returns the position of the {@code curve}'th outermost custom curve at 0 <= {@code t} <= 1.<br>
         * 0 = the innermost curve
         * 5 = the outermost curve
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnCustomCurve(double t, double curve) {
            if (t >= 1) {
                t -= 1;
            }
            int numberOfSegments = (controlPointsCustomTrack.length-1)/3;
            int segment = (int) Math.floor(t*numberOfSegments);
            
            Vector P0 = controlPointsCustomTrack[segment*3];
            Vector P1 = controlPointsCustomTrack[segment*3+1];
            Vector P2 = controlPointsCustomTrack[segment*3+2];
            Vector P3 = controlPointsCustomTrack[segment*3+3];
            double bezierT = (t-(((double) segment)/numberOfSegments))*numberOfSegments;
            Vector point = getCubicBezierPnt(bezierT, P0, P1, P2, P3);
            if (curve == 0) {
                return point;
            }
            Vector tangent = getCubicBezierTng(bezierT, P0, P1, P2, P3).scale(-1);
            Vector normal = tangent.cross(Vector.Z).normalized();
            return point.add(normal.scale(curve));
        }
        
        /**
         * Returns the position of the {@code curve}'th outermost test curve at 0 <= {@code t} <= 1.<br>
         * 0 = the innermost curve
         * 5 = the outermost curve
         * The curve parameter is a double to support getting the middle position of a track.
         */
        public Vector getPointOnTestCurve(double t, double curve) {
            Vector point = getTestPoint(t);
            if (curve == 0) {
                return point;
            }
            Vector tangent = getTestTangent(t);
            Vector normal = tangent.cross(Vector.Z).normalized();
            return point.add(normal.scale(curve));
        }
        
        /**
         * Returns the position of the test curve at 0 <= {@code t} <= 1.
         */
        public Vector getTestPoint(double t) {
            // / 10 * cos(2*pi*t) \
            // | 14 * sin(2*pi*t) |
            // \ 1                /

            return new Vector(10 * Math.cos(2 * Math.PI * t),
                              14 * Math.sin(2 * Math.PI * t),
                              1);
        }

        /**
         * Returns the tangent of the test curve at 0 <= {@code t} <= 1.
         */
        public Vector getTestTangent(double t) {
            // / 10 * cos(2*pi*t) \
            // | 14 * sin(2*pi*t) |
            // \ 1                /

            return new Vector(-10 * Math.sin(2 * Math.PI * t),
                              14 * Math.cos(2 * Math.PI * t),
                              0);
        }
        
        /**
         * Returns a point on a cubic Bezier segment
         */
        public Vector getCubicBezierPnt(double t, Vector P0, Vector P1, Vector P2, Vector P3) {
            /*Vector CasteljauA01 = P0.add(P1.subtract(P0).scale(t));
            Vector CasteljauA12 = P1.add(P2.subtract(P1).scale(t));
            Vector CasteljauA23 = P2.add(P3.subtract(P2).scale(t));
            Vector CasteljauB01 = CasteljauA01.add(CasteljauA12.subtract(CasteljauA01).scale(t));
            Vector CasteljauB12 = CasteljauA12.add(CasteljauA23.subtract(CasteljauA12).scale(t));
            Vector CasteljauC01 = CasteljauB01.add(CasteljauB12.subtract(CasteljauB01).scale(t));
            return CasteljauC01;*/
            return P0.scale((1-t)*(1-t)*(1-t)).add(P1.scale(3*t*(1-t)*(1-t))).add(P2.scale(3*t*t*(1-t))).add(P3.scale(t*t*t));
        }
        
        /**
         * Returns the approximate tangent vector on a cubic Bezier segment
         */
        public Vector getCubicBezierTng(double t, Vector P0, Vector P1, Vector P2, Vector P3) {
            // commented out: correct equations but prone to errors
                // standardBezier: return P0.scale((1-t)*(1-t)*(1-t)).add(P1.scale(3*t*(1-t)*(1-t))).add(P2.scale(3*t*t*(1-t))).add(P3.scale(t*t*t));
                return P1.subtract(P0).scale(3*(1-t)*(1-t)).add(P2.subtract(P1).scale(6*(1-t)*t)).add(P3.subtract(P2).scale(3*t*t));
            //return getCubicBezierPnt(t+0.001, P0, P1, P2, P3).subtract(getCubicBezierPnt(t, P0, P1, P2, P3));
        }

    }

    /**
     * Implementation of the terrain.
     */
    private class Terrain {
        
        /** Display list for the terrain. */
        private int displayListTerrain;
        
        /** Whether the display list for the terrain has been set up. */
        private boolean displayListTerrainSetUp = false;
        
        /** Number of segments to be used to draw the terrain (per dimension per direction). */
        private int SEGMENTS = 100;
        
        /** First x of the terrain. */
        private int xBegin = 0;
        
        /** Size in x of the terrain. */
        private int xSize = 40;
        
        /** First y of the terrain. */
        private int yBegin = 0;
        
        /** Size in y of the terrain. */
        private int ySize = 40;
        
        /** Returns the height of the terrain at a specific x and y. */
        private double getTerrainHeight(double x, double y) {
            return 0.6*Math.cos(0.3*x+0.2*y)+0.4*Math.cos(x-0.5*y);
        }
        
        /** The colors for the 1D texture */
        private Color[] textureColors = new Color[] {
            Color.BLUE,
            Color.YELLOW,
            Color.GREEN
        };
        
        /** The texid for the 1D texture */
        private int texture;
        
        /**
         * Constructs the terrain.
         * Terrain is in [-40,40], looks much better in camera scale.
         */
        public Terrain() {
            
        }

        /**
         * Draws the terrain.
         */
        public void draw() {
            // If the display list has not been set up yet, create it
            if (!displayListTerrainSetUp) {
                // Create the texture
                texture = create1DTexture(gl, textureColors);
                // Set up the display list
                displayListTerrain = gl.glGenLists(1);
                // Start compiling the display list
                gl.glNewList(displayListTerrain, GL_COMPILE);
                // Use a triangle list
                gl.glBegin(GL2.GL_TRIANGLES);
                    for (int xi = -SEGMENTS; xi < SEGMENTS; xi++) {
                        // Calculate the two x's
                        double x1 = xBegin+xSize*xi/((double) SEGMENTS);
                        double x2 = xBegin+xSize*(xi+1)/((double) SEGMENTS);
                        for (int yi = -SEGMENTS; yi <  SEGMENTS; yi++) {
                            // Calculate the two y's
                            double y1 = yBegin+ySize*yi/((double) SEGMENTS);
                            double y2 = yBegin+ySize*(yi+1)/((double) SEGMENTS);
                            // Calculate the heights of the terrain
                            double f11 = getTerrainHeight(x1, y1);
                            double f12 = getTerrainHeight(x1, y2);
                            double f21 = getTerrainHeight(x2, y1);
                            double f22 = getTerrainHeight(x2, y2);
                            // Draw the first triangle between (x1, y1), (x1, y2) and (x2, y2)
                            {
                                // Create vector objects for points
                                Vector point1 = new Vector(x1, y1, f11);
                                Vector point2 = new Vector(x1, y2, f12);
                                Vector point3 = new Vector(x2, y2, f22);
                                // Create normal vector
                                Vector normal = point2.subtract(point1).cross(point3.subtract(point1)).scale(-1);
                                // Set normal vector
                                gl.glNormal3d(normal.x(), normal.y(), normal.z());
                                // Draw 3 vertices with texture colors
                                gl.glTexCoord1d(getTextureCoordinateFromHeight(point1.z()));
                                gl.glVertex3d(point1.x(), point1.y(), point1.z());
                                gl.glTexCoord1d(getTextureCoordinateFromHeight(point2.z()));
                                gl.glVertex3d(point2.x(), point2.y(), point2.z());
                                gl.glTexCoord1d(getTextureCoordinateFromHeight(point3.z()));
                                gl.glVertex3d(point3.x(), point3.y(), point3.z());
                            }
                            // Draw the first triangle between (x1, y1), (x2, y1) and (x2, y2)
                            {
                               // Create vector objects for points
                               Vector point1 = new Vector(x1, y1, f11);
                               Vector point2 = new Vector(x2, y1, f21);
                               Vector point3 = new Vector(x2, y2, f22);
                               // Create normal vector
                               Vector normal = point2.subtract(point1).cross(point3.subtract(point1));
                               // Set normal vector
                               gl.glNormal3d(normal.x(), normal.y(), normal.z());
                               // Draw 3 vertices with texture colors
                               gl.glTexCoord1d(getTextureCoordinateFromHeight(point1.z()));
                               gl.glVertex3d(point1.x(), point1.y(), point1.z());
                               gl.glTexCoord1d(getTextureCoordinateFromHeight(point2.z()));
                               gl.glVertex3d(point2.x(), point2.y(), point2.z());
                               gl.glTexCoord1d(getTextureCoordinateFromHeight(point3.z()));
                               gl.glVertex3d(point3.x(), point3.y(), point3.z());
                            }
                        }
                    }
                // Finish the triangle list
                gl.glEnd();
                // Draw the gray transparent surface
                Material.WATER.setSurfaceColor(gl);
                gl.glBegin(GL_QUADS);
                    gl.glVertex3d(-40, -40, 0);
                    gl.glVertex3d(40, -40, 0);
                    gl.glVertex3d(40, 40, 0);
                    gl.glVertex3d(-40, 40, 0);
                gl.glEnd();
                // Finish compiling the display list
                gl.glEndList();
                // Set set up boolean to true
                displayListTerrainSetUp = true;
            }
            gl.glDisable(GL_TEXTURE_2D);
            gl.glEnable(GL_TEXTURE_1D);
            // Bind the terrain texture
            gl.glBindTexture(GL_TEXTURE_1D, texture);
            // Execute the display list for the terrain
            gl.glCallList(displayListTerrain);
            // Unbind the terrain texture
            gl.glBindTexture(GL_TEXTURE_1D, 0);
        }
        
        public double getTextureCoordinateFromHeight(double height) {
            height = (height+1)/4+0.25;
            height = (height<0.25)?0.25:height;
            height = (height>0.75)?0.75:height;
            return height;
        }
        
    }
    
    /**
     * Creates a new 2D texture
     * @param file
     * @param gl
     * @return 
     */
    private Texture load2DTexture(String file, GL2 gl) {
        Texture result = null;

        try {
            // Try to load from local folder.
            result = TextureIO.newTexture(new File(file), false);
        } catch (Exception e1) {
            // Try to load from /src folder instead.
            try {
                result = TextureIO.newTexture(new File("src/" + file), false);
            } catch (Exception e2) {
            }
        }

        if (result != null) {
            result.enable(gl);
        }

        return result;
    }

       /**
    * Creates a new 1D - texture.
    * @param gl
    * @param colors
    * @return the texture ID for the generated texture.
    */
    public int create1DTexture(GL2 gl, Color[] colors){
    gl.glDisable(GL_TEXTURE_2D);
    gl.glEnable(GL_TEXTURE_1D);
    int[] texid = new int[]{-1};
    gl.glGenTextures(1, texid, 0);
    ByteBuffer bb = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder());
    for (Color color : colors) {
       int pixel = color.getRGB();
       bb.put((byte) ((pixel >> 16) & 0xFF)); // Red component
       bb.put((byte) ((pixel >> 8) & 0xFF));  // Green component
       bb.put((byte) (pixel & 0xFF));         // Blue component
       bb.put((byte) ((pixel >> 24) & 0xFF)); // Alpha component
    }
    bb.flip();
    gl.glBindTexture(GL_TEXTURE_1D, texid[0]);
    gl.glTexImage1D(GL_TEXTURE_1D, 0, GL_RGBA8, colors.length, 0, GL_RGBA, GL_UNSIGNED_BYTE, bb);
    gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
    gl.glTexParameteri(GL_TEXTURE_1D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    gl.glBindTexture(GL_TEXTURE_1D, 0);
    return texid[0];
    }

    /**
     * Main program execution body, delegates to an instance of
     * the RobotRace implementation.
     */
    public static void main(String args[]) {
        RobotRace robotRace = new RobotRace();
    }

}
