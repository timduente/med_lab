package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.media.j3d.TriangleArray;
import javax.print.attribute.standard.MediaSize.Other;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;
import javax.vecmath.Tuple3f;

import misc.BitMask;
import myTestCube.BennyOneByte;
import myTestCube.Cube;
import myTestCube.MarchingCubeLUT;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.universe.SimpleUniverse;
import com.sun.prism.image.Coords;

/**
 * Three dimensional viewport for viewing the dicom images + segmentations.
 * 
 * @author Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class Viewport3d extends Viewport implements Observer {

	int mode = 0;

	Viewport2d v2d;

	boolean pointcloudEnabled = false;
	boolean orthoEnabled = false;
	boolean marchingCubeEnabled = false;
	boolean TextureVolumeRenderingEnabled = false;
	MarchingCubeLUT marchingCube = new MarchingCubeLUT();

	public void setV2d(Viewport2d v2d) {
		this.v2d = v2d;
	}

	
	private OrthoViews orthoViews = new OrthoViews();
	private float n = 5;
	private int marchingCubeSize = 1;
	// Dont need them, because changing perspective resets currently active
	// image. IF this will be changed, the ansatz is here to remember the
	// current image in the views.
	// private int remember_sag = 0;
	// private int remember_trans = 0;
	// private int remember_frontal = 0;

	Hashtable<String, Shape3D> shapes = new Hashtable<String, Shape3D>();

//	ArrayList<Shape3D> volume_slices = new ArrayList<Shape3D>();

	MouseWheelZoom mouseWheelZoom;
	TransformGroup tGroup;
	BranchGroup bgroup;

	BranchGroup volume2DRendering;
	BranchGroup orthoNode;
	BranchGroup marchingNode;


	/**
	 * Private class, implementing the GUI element for displaying the 3d data.
	 */
	public class Panel3d extends Canvas3D {
		public SimpleUniverse _simple_u;
		public BranchGroup _scene;

		public Panel3d(GraphicsConfiguration config) {
			super(config);

			// necessary
			BoundingBox boundBox = new BoundingBox(new Point3d(-1000, -1000, -1000), new Point3d(1000, 1000, 1000));

			setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setMaximumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setBackground(Color.black);

			_simple_u = new SimpleUniverse(this);
			_simple_u.getViewingPlatform().setNominalViewingTransform();
			_scene = new BranchGroup();

			tGroup = new TransformGroup();

			tGroup.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
			tGroup.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
			tGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);

			MouseRotate behavior = new MouseRotate(tGroup);
			behavior.setTransformGroup(tGroup);
			behavior.setSchedulingBounds(boundBox);
			tGroup.addChild(behavior);

			mouseWheelZoom = new MouseWheelZoom(tGroup);
			mouseWheelZoom.setSchedulingBounds(boundBox);
			tGroup.addChild(mouseWheelZoom);

			MouseZoom mouseBeh2 = new MouseZoom(tGroup);
			mouseBeh2.setSchedulingBounds(boundBox);
			tGroup.addChild(mouseBeh2);

			_scene.addChild(tGroup);

			volume2DRendering = new BranchGroup();
			volume2DRendering.setCapability(BranchGroup.ALLOW_DETACH);

			orthoNode = new BranchGroup();
			orthoNode.setCapability(BranchGroup.ALLOW_DETACH);

			marchingNode = new BranchGroup();
			marchingNode.setCapability(BranchGroup.ALLOW_DETACH);

			createScene();
			// initMarchingCube(0b10000000);
		}

		public void createScene() {

			if (bgroup == null) {
				bgroup = new BranchGroup();
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				tGroup.addChild(bgroup);
			}

			if (shapes.size() > 0 && pointcloudEnabled) {
				Enumeration<Shape3D> elements = shapes.elements();
				while (elements.hasMoreElements()) {
					Shape3D shape = elements.nextElement();

					if (shape.getParent() == null) {
						BranchGroup b = new BranchGroup();
						b.addChild(shape);
						bgroup.addChild(b);
					}
				}
			}

			if (!_scene.isCompiled()) {
				_scene.compile();
				_simple_u.addBranchGraph(_scene);
			}
		}
	}

	public void enablePointCloud(boolean enable) {
		pointcloudEnabled = enable;
		update_view();
	}

	public void enableOrthoslices(boolean enable) {
		orthoEnabled = enable;
		if (enable) {
			bgroup.addChild(orthoNode);
		} else {
			bgroup.removeChild(orthoNode);
			orthoNode.detach();
		}
		update_view();
	}

	public void enableMarchingCube(boolean enable) {
		marchingCubeEnabled = enable;

		if (enable) {
			bgroup.removeChild(marchingNode);
			marchingNode.detach();
			bgroup.addChild(marchingNode);
			System.out.println("enableMarchingCube(boolean enable) called");
		} else {
			bgroup.removeChild(marchingNode);
			marchingNode.detach();
			System.out.println("dont show marching cubes");
		}

		update_view();
	}

	/**
	 * FOR TEST USES ONLY
	 * 
	 * @param number
	 */
	public void initMarchingCube(int number) {
		bgroup.removeChild(marchingNode);
		marchingNode.detach();
		marchingNode = new BranchGroup();
		marchingNode.setCapability(BranchGroup.ALLOW_DETACH);

		// Main main = new Main();
		Cube cubi = this.marchingCube.McLut.get(number);
		System.out.println(cubi.toString());
		System.out.println("Rotated entries: \n");
		System.out.println("X: " + Integer.toBinaryString(cubi.debug_rotateX(1) & 0xFF) + " & Y: " + Integer.toBinaryString(cubi.debug_rotateY(1)) + " & Z: "
				+ Integer.toBinaryString(cubi.debug_rotateZ(1)));

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(0.3f, 0.3f, 0.3f));

		Appearance app = new Appearance();
		app.setColoringAttributes(color_ca);

		PolygonAttributes p = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK, 0.0f);
		app.setPolygonAttributes(p);
		PointAttributes ps = new PointAttributes();
		ps.setPointSize(10);

		ColoringAttributes color_points_0 = new ColoringAttributes();
		color_points_0.setColor(new Color3f(0.0f, 0.0f, 1.0f));

		ColoringAttributes color_points_1 = new ColoringAttributes();
		color_points_1.setColor(new Color3f(1.0f, 0.0f, 0.0f));
		Appearance app0 = new Appearance();
		app0.setColoringAttributes(color_points_0);
		Appearance app1 = new Appearance();
		app1.setColoringAttributes(color_points_1);
		// System.out.println("Size of triangle array: " + cubi.planes.length);

		IndexedTriangleArray indtria = new IndexedTriangleArray(marchingCube.coords.length, IndexedTriangleArray.COORDINATES, cubi.allIndices.length);
		indtria.setCoordinates(0, marchingCube.coords);
		indtria.setCoordinateIndices(0, cubi.allIndices);

		Shape3D shape = new Shape3D(indtria);
		shape.setAppearance(app);

		marchingNode.addChild(shape);
		// System.out.println("Point 1 coord:  " + tri.toString());

		TriangleArray tria = new TriangleArray(3, TriangleArray.COORDINATES);
		tria.setCoordinates(0, new Point3f[] { new Point3f(-1.0f, -1.0f, -1.0f), new Point3f(1.0f, -1.0f, -1.0f), new Point3f(-1.0f, 1.0f, -1.0f) });
		Shape3D shapppp = new Shape3D(tria);
		shapppp.setAppearance(app1);
		marchingNode.addChild(shapppp);

		boolean[] corn = new boolean[8];
		int index = 1;
		int on = 0;
		int off = 0;
		for (int i = 0; i < 8; i++) {
			corn[7 - i] = ((byte) (cubi.corner & index)) != 0;
			index = index << 1;
			if (corn[7 - i])
				on++;
			else
				off++;
		}

		PointArray pointsArr_0 = new PointArray(off, PointArray.COORDINATES);
		PointArray pointsArr_1 = new PointArray(on, PointArray.COORDINATES);

	

		int c_on = 0;
		int c_off = 0;
		for (int i = 0; i < 8; i++) {
			if (corn[i]) { // is on point
				pointsArr_1.setCoordinate(c_on, MarchingCubeLUT.allPoints[i]);
				c_on++;
			} else {
				pointsArr_0.setCoordinate(c_off, MarchingCubeLUT.allPoints[i]);
				c_off++;
			}
		}

		app0.setPointAttributes(ps);
		app1.setPointAttributes(ps);
		Shape3D points_on = new Shape3D(pointsArr_1, app1);
		Shape3D points_off = new Shape3D(pointsArr_0, app0);

		marchingNode.addChild(points_on);
		marchingNode.addChild(points_off);
		bgroup.addChild(marchingNode);

		/** DEBUG **/
	}

	/**
	 * Real method showing segmented data as marching cubes
	 * 
	 * @param number
	 */
	private void initMarchingCubes(Segment seg) {
		System.out.println("initMarchingCubes(Segment seg) called");
		bgroup.removeChild(marchingNode);
		marchingNode.detach();
		marchingNode = new BranchGroup();
		marchingNode.setCapability(BranchGroup.ALLOW_DETACH);

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(0.3f, 0.3f, 0.3f));
		ColoringAttributes color_points_0 = new ColoringAttributes();
		color_points_0.setColor(new Color3f(0.0f, 0.0f, 1.0f));
		ColoringAttributes color_points_1 = new ColoringAttributes();
		color_points_1.setColor(new Color3f(1.0f, 0.0f, 0.0f));

		Appearance app = new Appearance();
		app.setColoringAttributes(color_points_1);
		Appearance app0 = new Appearance();
		app0.setColoringAttributes(color_points_0);
		Appearance app1 = new Appearance();
		app1.setColoringAttributes(color_points_1);

		PolygonAttributes p = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0.0f);
		app.setPolygonAttributes(p);
		PointAttributes ps = new PointAttributes();
		ps.setPointSize(10);

		if (shapes.containsKey(seg.getName())) {
			shapes.remove(seg.getName());
		}

		

		float w2 = seg.getMask(0).getWidth() / 2;
		float h2 = seg.getMask(0).getHeight() / 2;
		
		LinkedList<Point3f> allPoints = new LinkedList<Point3f>();
		LinkedList<Integer> allIndexedPlanes = new LinkedList<Integer>();
		
		for (int i = 0; i < seg.getMaskNum() - marchingCubeSize; i += marchingCubeSize) {
			BitMask upper_bitmask = seg.getMask(i);
			BitMask lower_bitmask = seg.getMask(i + marchingCubeSize);
			for (int y = 0; y < upper_bitmask.getHeight() - marchingCubeSize; y += marchingCubeSize) {
				for (int x = 0; x < upper_bitmask.getWidth() - marchingCubeSize; x += marchingCubeSize) {					
					BennyOneByte onArray = new BennyOneByte(0);
					onArray.set(0, lower_bitmask.get(x + marchingCubeSize, y));
					onArray.set(1, lower_bitmask.get(x + marchingCubeSize, y + marchingCubeSize));
					onArray.set(2, upper_bitmask.get(x + marchingCubeSize, y + marchingCubeSize));
					onArray.set(3, upper_bitmask.get(x + marchingCubeSize, y));
					onArray.set(4, lower_bitmask.get(x, y));
					onArray.set(5, lower_bitmask.get(x + marchingCubeSize, y));
					onArray.set(6, upper_bitmask.get(x + marchingCubeSize, y));
					onArray.set(7, upper_bitmask.get(x, y));
					int cornerVal = onArray.getAsInt();

					Cube cubi = marchingCube.McLut.get(cornerVal & 0xFF);
					if (cubi.allIndices.length != 0) {
						Point3f save_point = new Point3f((x - w2 + marchingCubeSize /2.0f) / upper_bitmask.getWidth(), (y - h2 + marchingCubeSize /2.0f) / upper_bitmask.getHeight(),
								(marchingCubeSize /2.0f + i - seg.getMaskNum() / 2.0f) / seg.getMaskNum());
						allPoints.add(save_point);
						
						Point3f shift_point = new Point3f((x - w2) / upper_bitmask.getWidth(), (y - h2) / upper_bitmask.getHeight(),
								(i - seg.getMaskNum() / 2.0f) / seg.getMaskNum());

						
						Point3f[] cubeCoords = new Point3f[12];
						for (int j = 0; j < 12; j++) {
							cubeCoords[j] = new Point3f(marchingCube.coords[j]);
							cubeCoords[j].scale(marchingCubeSize * 1.0f / seg.getMask(0).getWidth());
							cubeCoords[j].add(shift_point);
						}
						for(int[] indexCubiPlane : cubi.lPlanes)	{	// 3 4 5 => p12, p15, p37
							allIndexedPlanes.push(getIndexOfLinkedList(allPoints, cubeCoords[indexCubiPlane[0]]));
							allIndexedPlanes.push(getIndexOfLinkedList(allPoints, cubeCoords[indexCubiPlane[1]]));
							allIndexedPlanes.push(getIndexOfLinkedList(allPoints, cubeCoords[indexCubiPlane[2]]));
						}
					}
				}
			}
		}		
		Point3f[] allPointsArray = new Point3f[allPoints.size()];
		int counter2 = 0; 
		for(Point3f pt : allPoints)	{
			allPointsArray[counter2++] = pt; 
		}
		System.out.println("Size of allPointsArray " + allPointsArray.length + " of " + allPoints.size());
		
		int[] allIndices = new int[allIndexedPlanes.size()];
		int counter = 0; 
		for(int intii : allIndexedPlanes)	{
			allIndices[counter++] = intii; 
		}

		System.out.println("Greatness of POWER: " + allIndexedPlanes.size() + " and has " + allIndices.length);
		IndexedTriangleArray indtria = new IndexedTriangleArray(allPointsArray.length, IndexedTriangleArray.COORDINATES,allIndices.length );
		indtria.setCoordinates(0, allPointsArray); // TODO: Program start or  enable marching cube
		indtria.setCoordinateIndices(0, allIndices);
		Shape3D shapp = new Shape3D(indtria, app);
		marchingNode.addChild(shapp);
		bgroup.addChild(marchingNode);
		System.out.println("initMarchingCubes(Segment seg) ended");
	}
	
	private int getIndexOfLinkedList(LinkedList<Point3f> llp3f, Point3f ptsearched)	{
		int counter = 0; 
		for(Point3f pt : llp3f)	{
			if(pt.epsilonEquals(ptsearched, 0.1f))	{
				return counter; 
			}
			counter++; 			
		}	
		System.out.println("BAD Error");
		return -1; 
	}

	public void enable2DTextureVolumeRendering(boolean enable) {
		TextureVolumeRenderingEnabled = enable;
		if (enable) {
			bgroup.addChild(volume2DRendering);
		} else {
			bgroup.removeChild(volume2DRendering);
			volume2DRendering.detach();
		}
		update_view();
	}

	private boolean addPoint(Point3f point) {
		return (point.x % n == 0 && point.y % n == 0 && point.z % n == 0);

	}

	private void addPoints(Segment seg) {
		ArrayList<Point3f> pointsToShow = new ArrayList<Point3f>();
		Shape3D shape = null;

		if (shapes.containsKey(seg.getName())) {
			shape = shapes.remove(seg.getName());
		}

		int w2 = seg.getMask(0).getWidth() / 2;
		int h2 = seg.getMask(0).getHeight() / 2;
		for (int i = 0; i < seg.getMaskNum(); i++) {
			BitMask bitmask = seg.getMask(i);
			for (int y = 0; y < bitmask.getHeight(); y++) {
				for (int x = 0; x < bitmask.getWidth(); x++) {
					if (bitmask.get(x, y)) {
						Point3f point = new Point3f((x - w2), (y - h2), (i - seg.getMaskNum() / 2));
						if (addPoint(point)) {
							point.set(point.x / bitmask.getWidth(), point.y / bitmask.getHeight(), point.z / seg.getMaskNum());
							pointsToShow.add(point);
						}

					}
				}
			}
		}

		if (pointsToShow.size() <= 0) {
			return;
		}

		PointArray points = new PointArray(pointsToShow.size(), PointArray.COORDINATES);

		for (int i = 0; i < pointsToShow.size(); i++) {
			points.setCoordinate(i, pointsToShow.get(i));
		}

		if (shape == null) {
			int color = seg.getColor();

			int red = (color >> 16) & 0xff;
			int green = (color >> 8) & 0xff;
			int blue = color & 0xff;

			ColoringAttributes color_ca = new ColoringAttributes();

			Appearance ap = new Appearance();
			ap.setColoringAttributes(color_ca);
			PointAttributes pAtts = new PointAttributes();
			pAtts.setPointSize(1.0f);
			ap.setPointAttributes(pAtts);

			color_ca.setColor(new Color3f(red / 256.0f, green / 256.0f, blue / 256.0f));
			shape = new Shape3D(points, ap);
			shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			shapes.put(seg.getName(), shape);
		} else {
			shape.setGeometry(points);
			shapes.put(seg.getName(), shape);
		}

	}

	private Panel3d _panel3d;

	/**
	 * Constructor, with a reference to the global image stack as argument.
	 * 
	 * @param slices
	 *            a reference to the global image stack
	 */
	public Viewport3d() {
		super();

		this.setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
		this.setLayout(new BorderLayout());
		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
		_panel3d = new Panel3d(config);
		this.add(_panel3d, BorderLayout.CENTER);

	}

	/**
	 * calculates the 3d data structurs.
	 */
	public void update_view() {
		_panel3d.createScene();
		if (orthoEnabled)
			orthoViews.addOrthoSlices(mode, mode, false);
	}

	public void changeN(int n) {
		this.n = (float) n;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs.hasMoreElements();) {
			Segment seg = segs.nextElement();
			addPoints(seg);
		}
		update_view();
	}

	public void changeMarchingCubeSize(int n) {
		this.marchingCubeSize = n;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs.hasMoreElements();) {
			Segment seg = segs.nextElement();
			initMarchingCubes(seg);
		}
		update_view();
	}

	/**
	 * Implements the observer function update. Updates can be triggered by the
	 * global image stack.
	 */
	public void update(final Observable o, final Object obj) {
		if (!EventQueue.isDispatchThread()) {
			// all swing thingies must be done in the AWT-EventQueue
			EventQueue.invokeLater(new Runnable() {
				public void run() {
					update(o, obj);
				}
			});
			return;
		}

		// boolean update_needed = false;
		Message m = (Message) obj;

		if (m._type == Message.M_SEG_CHANGED || m._type == Message.M_REGION_GROW_SEG_CHANGED || m._type == Message.M_CUBE_SIZE_CHANGED) {
			String seg_name = ((Segment) (m._obj)).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {

				System.out.println("need Update");
				if (pointcloudEnabled) {
					addPoints((Segment) (m._obj));
				}
				if (marchingCubeEnabled) {
					initMarchingCubes((Segment) (m._obj));
				}
				update_view();
			}
		}

		if (m._type == Message.M_NEW_ACTIVE_IMAGE) {
			update_view();
		}

		if (m._type == Message.M_LOADING_IMAGES_FINISHED) {
			orthoViews.initOrthoSlices(orthoNode);
			orthoViews.addOrthoSlices(mode, mode, true);
			orthoViews.initVolumeRendering(volume2DRendering);

			update_view();
		}

	}

	public void setViewMode(int mode) {
		int oldMode = this.mode;
		this.mode = mode;
		orthoViews.addOrthoSlices(oldMode, mode, false);
	}
}
