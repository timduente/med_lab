package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Observer;

import javax.media.j3d.AmbientLight;
import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BoundingSphere;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.DirectionalLight;
import javax.media.j3d.IndexedTriangleArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TriangleArray;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

import misc.BitMask;
import myTestCube.OneByte;
import myTestCube.Cube;
import myTestCube.MarchingCubeLUT;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
import com.sun.j3d.utils.geometry.GeometryInfo;
import com.sun.j3d.utils.geometry.NormalGenerator;
import com.sun.j3d.utils.universe.SimpleUniverse;

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
	boolean marchingCubeTestEnabled = false;
	boolean TextureVolumeRenderingEnabled = false;
	MarchingCubeLUT marchingCube = new MarchingCubeLUT();

	public void setV2d(Viewport2d v2d) {
		this.v2d = v2d;
	}

	private OrthoViews orthoViews = new OrthoViews();
	private float n = 5;
	public int marchingCubeSize = 4;

	private Hashtable<String, Shape3D> pointCloudShapes = new Hashtable<String, Shape3D>();
	private Hashtable<String, Shape3D> marchingCubeShapes = new Hashtable<String, Shape3D>();

	MouseWheelZoom mouseWheelZoom;
	TransformGroup tGroup;
	BranchGroup bgroup;

	BranchGroup volume2DRendering;
	BranchGroup orthoNode;
	BranchGroup marchingNode;
	BranchGroup marchingCubeTestNode;
	BranchGroup pointNode;

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

			getView().setFrontClipDistance(0.01);

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
			marchingNode.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			marchingCubeTestNode = new BranchGroup();
			marchingCubeTestNode.setCapability(BranchGroup.ALLOW_DETACH);

			pointNode = new BranchGroup();
			pointNode.setCapability(BranchGroup.ALLOW_DETACH);
			pointNode.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);

			initMarchingCubeTestOnceAtStartUp();
			

			// Licht
			BoundingSphere bounds;
			bounds = new BoundingSphere(new Point3d(0.0d, 0.0d, 0.0d), Double.MAX_VALUE);
			Vector3f dir = new Vector3f(-1f, -1f, -1f);
			dir.normalize();
			// directional light
			DirectionalLight d_light = new DirectionalLight();

			d_light.setInfluencingBounds(bounds);
			d_light.setColor(new Color3f(1.0f, 1.0f, 1.0f));
			d_light.setDirection(dir);

			// ambient light
			AmbientLight a_light = new AmbientLight();
			a_light.setInfluencingBounds(bounds);
			a_light.setColor(new Color3f(0.6f, 0.6f, 0.6f));

			_scene.addChild(d_light);
			_scene.addChild(a_light);

			createScene();
		}

		public void createScene() {

			if (bgroup == null) {
				bgroup = new BranchGroup();
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				tGroup.addChild(bgroup);
			}

			if (!_scene.isCompiled()) {
				_scene.compile();
				_simple_u.addBranchGraph(_scene);
			}
		}
	}

	public void enablePointCloud(boolean enable) {

		if (enable) {
			bgroup.addChild(pointNode);
		} else {
			bgroup.removeChild(pointNode);
			pointNode.detach();
		}
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

	public void enableMarchingCubeTest(boolean enable) {
		marchingCubeTestEnabled = enable;
		if (enable) {
			bgroup.addChild(marchingCubeTestNode);
		} else {
			bgroup.removeChild(marchingCubeTestNode);
			marchingCubeTestNode.detach();
		}
		update_view();
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

	public void enableMarchingCube(boolean enable) {
		marchingCubeEnabled = enable;

		if (enable) {
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
	Shape3D points_on;
	Shape3D points_off;
	Shape3D shapppp;
	Shape3D shape;

	public void initMarchingCubeTestOnceAtStartUp() {

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

		app0.setPointAttributes(ps);
		app1.setPointAttributes(ps);

		TriangleArray tria = new TriangleArray(3, TriangleArray.COORDINATES);
		tria.setCoordinates(0, new Point3f[] { new Point3f(-1.0f, -1.0f, -1.0f), new Point3f(1.0f, -1.0f, -1.0f), new Point3f(-1.0f, 1.0f, -1.0f) });

		shapppp = new Shape3D(tria);
		shapppp.setAppearance(app1);

		shape = new Shape3D();
		shape.setAppearance(app);
		shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

		points_on = new Shape3D();
		points_on.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		points_on.setAppearance(app1);

		points_off = new Shape3D();
		points_off.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		points_off.setAppearance(app0);

		marchingCubeTestNode.addChild(shapppp);
		marchingCubeTestNode.addChild(shape);
		marchingCubeTestNode.addChild(points_on);
		marchingCubeTestNode.addChild(points_off);
	}

	public void showMarchingCubeWithNumberBin(int number) {
		Cube cubi = this.marchingCube.McLut.get(number);

		IndexedTriangleArray indtria = new IndexedTriangleArray(MarchingCubeLUT.coords.length, IndexedTriangleArray.COORDINATES, cubi.allIndices.length);
		indtria.setCoordinates(0, MarchingCubeLUT.coords);
		indtria.setCoordinateIndices(0, cubi.allIndices);

		shape.setGeometry(indtria);

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

		for (int i = 0, c_on = 0, c_off = 0; i < 8; i++) {
			if (corn[i]) { // is on point
				pointsArr_1.setCoordinate(c_on, MarchingCubeLUT.allPoints[i]);
				c_on++;
			} else {
				pointsArr_0.setCoordinate(c_off, MarchingCubeLUT.allPoints[i]);
				c_off++;
			}
		}

		points_on.setGeometry(pointsArr_1);
		points_off.setGeometry(pointsArr_0);
	}
	

	private Appearance getMCAppearnceForSeg(Segment seg) {
		int color = seg.getColor();

		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;	

		Appearance app = new Appearance();
		Material material = new Material();

		material.setDiffuseColor(red / 255.0f, green / 255.0f, blue / 255.0f);
		app.setMaterial(material);

		ColoringAttributes color_points_1 = new ColoringAttributes();
		color_points_1.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		app.setColoringAttributes(color_points_1);

		PolygonAttributes p = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_BACK, 0.0f);
		app.setPolygonAttributes(p);

		return app;
	}

	/**
	 * Real method showing segmented data as marching cubes
	 * 
	 * @param number
	 */
	private void initMarchingCubes(Segment seg) {
		System.out.println("initMarchingCubes(Segment seg) called");
		System.out.println("MarchingCubeSize = " + marchingCubeSize);
		
		Shape3D shape = null;

		if (marchingCubeShapes.containsKey(seg.getName())) {
			shape = marchingCubeShapes.get(seg.getName());
		}

		LinkedList<Point3f> allPoints = new LinkedList<Point3f>();

		// Point3f auf Index wo der Punkt in der Liste liegt.
		Hashtable<Point3f, Integer> allPointsHash = new Hashtable<>();

		LinkedList<Integer> allIndexedPlanes = new LinkedList<Integer>();

		BitMask upper_bitmask, lower_bitmask;
		OneByte onArray = new OneByte(0);

		float scaleFactor = (float) marchingCubeSize / seg.getMask(0).getWidth();
		float scaleFactorZ = (float) marchingCubeSize / seg.getMaskNum();

		for (int i = 0; i < seg.getMaskNum() - marchingCubeSize; i += marchingCubeSize) {
			upper_bitmask = seg.getMask(i);
			lower_bitmask = seg.getMask(i + marchingCubeSize);

			for (int y = 0; y < upper_bitmask.getHeight() - marchingCubeSize; y += marchingCubeSize) {
				for (int x = 0; x < upper_bitmask.getWidth() - marchingCubeSize; x += marchingCubeSize) {

					// OLD
					// onArray.set(0, lower_bitmask.get(x + marchingCubeSize,
					// y));
					// onArray.set(1, lower_bitmask.get(x + marchingCubeSize, y
					// + marchingCubeSize));
					// onArray.set(2, upper_bitmask.get(x + marchingCubeSize, y
					// + marchingCubeSize));
					// onArray.set(3, upper_bitmask.get(x + marchingCubeSize,
					// y));
					// onArray.set(4, lower_bitmask.get(x, y));
					// onArray.set(5, lower_bitmask.get(x, y +
					// marchingCubeSize));
					// onArray.set(6, upper_bitmask.get(x, y +
					// marchingCubeSize));
					// onArray.set(7, upper_bitmask.get(x, y));
					// OLD

					onArray.set(0, lower_bitmask.get(x, y + marchingCubeSize));
					onArray.set(1, lower_bitmask.get(x + marchingCubeSize, y + marchingCubeSize));
					onArray.set(4, lower_bitmask.get(x, y));
					onArray.set(5, lower_bitmask.get(x + marchingCubeSize, y));

					onArray.set(3, upper_bitmask.get(x, y + marchingCubeSize));
					onArray.set(2, upper_bitmask.get(x + marchingCubeSize, y + marchingCubeSize));
					onArray.set(6, upper_bitmask.get(x + marchingCubeSize, y));
					onArray.set(7, upper_bitmask.get(x, y));
					// if(!(onArray.getAsInt() == 0) && !(onArray.getAsInt() ==
					// 255)) {
					Cube cubi = marchingCube.McLut.get(onArray.getAsInt());
					if (cubi != null && cubi.allIndices.length != 0) {
						Point3f shift_point = new Point3f((float) x / upper_bitmask.getWidth() - 0.5f, (float) y / upper_bitmask.getHeight() - 0.5f, (float) i
								/ seg.getMaskNum() - 0.5f);

						ArrayList<Point3f> list = cubi.getAllTriangles();
						for (int tri = 0; tri < list.size(); tri++) {
							Point3f temp = list.get(tri);
							float z = temp.z;
							temp.scale(scaleFactor);
							temp.setZ(z * scaleFactorZ);
							temp.add(shift_point);
							Integer index = allPointsHash.get(temp);
							if (index == null) {
								index = allPoints.size();
								allPointsHash.put(temp, index);
								allPoints.add(temp);
							}

							allIndexedPlanes.add(index);
						}

					}
				}
			}
		}
		if (allPoints.size() == 0) {
			allPoints.add(new Point3f());
			allIndexedPlanes.add(0);
			allIndexedPlanes.add(0);
			allIndexedPlanes.add(0);
		}

		Point3f[] allPointsArray = new Point3f[allPoints.size()];
		int counter2 = 0;
		// copy
		for (Point3f pt : allPoints) {
			allPointsArray[counter2++] = pt;
		}
		System.out.println("Size of allPointsArray " + allPointsArray.length + " of " + allPoints.size());

		int[] allIndices = new int[allIndexedPlanes.size()];
		int counter = 0;
		for (int intii : allIndexedPlanes) {
			allIndices[counter++] = intii;
		}
		// copy end

		System.out.println("Greatness of POWER: " + allIndexedPlanes.size() + " and has " + allIndices.length);
		IndexedTriangleArray indtria = new IndexedTriangleArray(allPointsArray.length, IndexedTriangleArray.COORDINATES | IndexedTriangleArray.NORMALS,
				allIndices.length);
		indtria.setCoordinates(0, allPointsArray); // TODO: Program start or
													// enable marching cube
		indtria.setCoordinateIndices(0, allIndices);

		NormalGenerator ng = new NormalGenerator();
		GeometryInfo info = new GeometryInfo(indtria);
		ng.generateNormals(info);

		if (shape == null) {

			Appearance app = getMCAppearnceForSeg(seg);
			shape = new Shape3D(info.getIndexedGeometryArray(), app);
			shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			marchingCubeShapes.put(seg.getName(), shape);
			BranchGroup bgroup = new BranchGroup();
			bgroup.addChild(shape);
			marchingNode.addChild(bgroup);
		} else {
			shape.setGeometry(info.getIndexedGeometryArray());
		}

		System.out.println("initMarchingCubes(Segment seg) ended");
	}

	private boolean addPoint(int x, int y, int z) {
		return (x % n == 0 && y % n == 0 && z % n == 0);

	}

	private void addPoints(Segment seg) {
		ArrayList<Point3f> pointsToShow = new ArrayList<Point3f>();
		
		Shape3D shape = null;

		if (pointCloudShapes.containsKey(seg.getName())) {
			shape = pointCloudShapes.get(seg.getName());
		}

		float w2 = seg.getMask(0).getWidth() / 2.0f;
		float h2 = seg.getMask(0).getHeight() / 2.0f;
		float z2 = seg.getMaskNum() / 2.0f;

		for (int i = 0; i < seg.getMaskNum(); i++) {
			BitMask bitmask = seg.getMask(i);
			for (int y = 0; y < bitmask.getHeight(); y++) {
				for (int x = 0; x < bitmask.getWidth(); x++) {
					if (bitmask.get(x, y)) {
						Point3f point;
						if (addPoint(x, y, i)) {
							point = new Point3f((x - w2) / bitmask.getWidth(), (y - h2) / bitmask.getHeight(), (i - z2) / seg.getMaskNum());
							pointsToShow.add(point);
						}

					} 
				}
			}
		}

		if (pointsToShow.size() <= 0) {
			pointsToShow.add(new Point3f());
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
			pAtts.setPointSize(5.0f);
			ap.setPointAttributes(pAtts);

			color_ca.setColor(new Color3f(red / 256.0f, green / 256.0f, blue / 256.0f));
			shape = new Shape3D(points, ap);
			shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
			pointCloudShapes.put(seg.getName(), shape);
			BranchGroup bgroup = new BranchGroup();
			bgroup.addChild(shape);
			pointNode.addChild(bgroup);
		} else {
			shape.setGeometry(points);
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
				// if (pointcloudEnabled) {
				addPoints((Segment) (m._obj));
				// }
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
