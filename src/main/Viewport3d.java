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
import java.util.Observable;
import java.util.Observer;

import javax.media.j3d.Appearance;
import javax.media.j3d.BoundingBox;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
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
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import misc.BitMask;
import myTestCube.Cube;
import myTestCube.MarchingCubeLUT;

import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
import com.sun.j3d.utils.behaviors.mouse.MouseZoom;
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

	boolean orthoEnabled = false;
	boolean marchingCubeEnabled = false;
	boolean TextureVolumeRenderingEnabled = false;
	MarchingCubeLUT main = new MarchingCubeLUT();

	public void setV2d(Viewport2d v2d) {
		this.v2d = v2d;
	}

	private float n = 5;
	// Dont need them, because changing perspective resets currently active
	// image. IF this will be changed, the ansatz is here to remember the
	// current image in the views.
	// private int remember_sag = 0;
	// private int remember_trans = 0;
	// private int remember_frontal = 0;

	Hashtable<String, Shape3D> shapes = new Hashtable<String, Shape3D>();

	ArrayList<Shape3D> volume_slices = new ArrayList<Shape3D>();

	MouseWheelZoom mouseWheelZoom;
	TransformGroup tGroup;
	BranchGroup bgroup;

	BranchGroup volume2DRendering;
	BranchGroup orthoNode;
	BranchGroup marchingNode;

	QuadArray[] ortho_planes = new QuadArray[3];
	Appearance[] app_for_ortho_planes = new Appearance[3];

	// QuadArray trans_plane; 0
	// QuadArray sag_plane; 1
	// QuadArray fron_plane; 2

	// Appearance ap_trans; 0
	// Appearance ap_sag; 1
	// Appearance ap_front; 2

	/**
	 * Private class, implementing the GUI element for displaying the 3d data.
	 */
	public class Panel3d extends Canvas3D {
		public SimpleUniverse _simple_u;
		public BranchGroup _scene;

		public Panel3d(GraphicsConfiguration config) {
			super(config);

			// necessary
			BoundingBox boundBox = new BoundingBox(new Point3d(-1000, -1000,
					-1000), new Point3d(1000, 1000, 1000));

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
			initMarchingCube(0b10000000);
		}

		public void createScene() {

			if (bgroup == null) {
				bgroup = new BranchGroup();
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				tGroup.addChild(bgroup);
			}

			if (shapes.size() > 0) {
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
			bgroup.addChild(marchingNode);
			System.out.println("show marching cubes");
		} else {
			bgroup.removeChild(marchingNode);
			marchingNode.detach();
			System.out.println("dont show marching cubes");
		}
		
		update_view();
	}
	
	public void initMarchingCube(int number){
		bgroup.removeChild(marchingNode);
		marchingNode.detach();
		marchingNode = new BranchGroup();
		marchingNode.setCapability(BranchGroup.ALLOW_DETACH);
		
		
		
//		Main main = new Main();
		Cube cubi = this.main.McLut.get(number);
		System.out.println(cubi.toString());
		
		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(0.3f, 0.3f, 0.3f));
		
		Appearance app = new Appearance();
		app.setColoringAttributes(color_ca);
		
		PolygonAttributes p = new PolygonAttributes(PolygonAttributes.POLYGON_FILL, PolygonAttributes.CULL_NONE, 0.0f);
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
//		System.out.println("Size of triangle array: " + cubi.planes.length);
		for(TriangleArray tri : cubi.planes)	{
			Shape3D shape = new Shape3D(tri);
			shape.setAppearance(app);
			
			marchingNode.addChild(shape);
//			System.out.println("Point 1 coord:  " + tri.toString());
		}
		TriangleArray tria = new TriangleArray(3, TriangleArray.COORDINATES);
		tria.setCoordinates(0, new Point3f[]{ new Point3f(-1.0f, -1.0f, -1.0f), new Point3f(1.0f, -1.0f, -1.0f),new Point3f(-1.0f, 1.0f, -1.0f)});
		Shape3D shapppp = new Shape3D(tria); 
		shapppp.setAppearance(app1);
		marchingNode.addChild(shapppp);
		
		
		
		boolean[] corn = new boolean[8];
		int index = 1; 
		int on = 0; 
		int off = 0; 
		for(int i = 0; i < 8; i++)	{
			corn[7-i] = ((byte) ( cubi.corner & index))!=0; 
			index = index << 1; 
			if(corn[7-i]) on++;
			else off++; 
		}
		
		PointArray pointsArr_0 = new PointArray(off, PointArray.COORDINATES);
		PointArray pointsArr_1 = new PointArray(on, PointArray.COORDINATES);
		
		Point3f p0 = new Point3f(-1.0f, -1.0f, -1.0f); 
		Point3f p1 = new Point3f(1.0f, -1.0f, -1.0f);
		Point3f p2 = new Point3f(1.0f, -1.0f, 1.0f);
		Point3f p3 = new Point3f(-1.0f, -1.0f, 1.0f);
		Point3f p4 = new Point3f(-1.0f, 1.0f, -1.0f);
		Point3f p5 = new Point3f(1.0f, 1.0f, -1.0f);
		Point3f p6 = new Point3f(1.0f, 1.0f, 1.0f);
		Point3f p7 = new Point3f(-1.0f, 1.0f, 1.0f);
		Point3f[] allPoints = new Point3f[]{p0, p1,p2,p3,p4,p5,p6,p7};
		
		int c_on = 0; 
		int c_off = 0; 
		for (int i = 0; i < 8; i++) {
			if(corn[i])	{ //is on point
				pointsArr_1.setCoordinate(c_on, allPoints[i]);
				c_on++;
			} else{
				pointsArr_0.setCoordinate(c_off, allPoints[i]);
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
//		System.out.println("Cube plane (points, no triangle): ");
//		 
//		for (points[] pt : cubi.lPlanes) {
//			System.out.println("Values:" + pt[0] + " " + pt[1] + " " + pt[2]
//					+ " ");
//		
//		}
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

	private void initVolumeRendering() {
		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.COMBINE);

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(0.3f, 0.3f, 0.3f));

		PolygonAttributes polygonAttributs = new PolygonAttributes();
		polygonAttributs.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributs.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		TransparencyAttributes tpAtt = new TransparencyAttributes(
				TransparencyAttributes.BLENDED, .2f,
				TransparencyAttributes.BLEND_SRC_ALPHA,
				TransparencyAttributes.BLEND_ONE);

		float range = 0.5f;

		for (int i = 0; i < _slices.getDepth(0); i++) {
			Appearance ap = initAppearanceForOrthoPlanes(ta, color_ca,
					polygonAttributs, tpAtt);
			QuadArray quad = initQuadArray();
			Shape3D onePlane = new Shape3D(quad, ap);
			volume_slices.add(onePlane);
			initArea(quad, ap, range, 0, i);
			volume2DRendering.addChild(onePlane);
		}
	}

	private void initArea(QuadArray quad, Appearance ap, float range,
			int view_mode, int imageIndex) {
		BufferedImage img_trans;

		float layer = (imageIndex - _slices.getDepth(view_mode) / 2.0f)
				/ _slices.getDepth(view_mode);

		Point3f[] slice = { new Point3f(range, range, layer),
				new Point3f(-range, range, layer),
				new Point3f(-range, -range, layer),
				new Point3f(range, -range, layer) };

		quad.setCoordinates(0, slice);

		img_trans = _slices.getImage(imageIndex, view_mode, 0x04000000);

		ImageComponent2D i2d = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_trans);

		Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA,
				img_trans.getWidth(), img_trans.getHeight());

		tex.setImage(0, i2d);

		ap.setTexture(tex);

	}

	private Appearance initAppearanceForOrthoPlanes(TextureAttributes ta,
			ColoringAttributes color_ca, PolygonAttributes polygonAttributs,
			TransparencyAttributes tpAtt) {
		Appearance appearance = new Appearance();

		appearance.setTextureAttributes(ta);
		appearance.setPolygonAttributes(polygonAttributs);
		appearance.setTransparencyAttributes(tpAtt);
		appearance.setColoringAttributes(color_ca);
		appearance.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

		return appearance;
	}

	private QuadArray initQuadArray() {
		QuadArray quadArray = new QuadArray(4, QuadArray.COORDINATES
				| GeometryArray.TEXTURE_COORDINATE_2);
		quadArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		quadArray.setTextureCoordinate(0, 0, new TexCoord2f(1.0f, 0.0f));
		quadArray.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
		quadArray.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
		quadArray.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

		return quadArray;
	}

	private void initOrthoSlices() {
		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.COMBINE);

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(1.0f, 1.0f, 1.0f));

		PolygonAttributes polygonAttributs = new PolygonAttributes();
		polygonAttributs.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributs.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		TransparencyAttributes tpAtt = new TransparencyAttributes(
				TransparencyAttributes.BLENDED, .4f,
				TransparencyAttributes.BLEND_SRC_ALPHA,
				TransparencyAttributes.BLEND_ONE);

		Shape3D[] ortho_shapes = new Shape3D[3];

		for (int i = 0; i < ortho_planes.length; i++) {

			app_for_ortho_planes[i] = initAppearanceForOrthoPlanes(ta,
					color_ca, polygonAttributs, tpAtt);
			ortho_planes[i] = initQuadArray();

			ortho_shapes[i] = new Shape3D(ortho_planes[i],
					app_for_ortho_planes[i]);
			ortho_shapes[i].setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

			orthoNode.addChild(ortho_shapes[i]);
		}
	}

	private void addOrthoSlices(int lastMode, int newMode, boolean init) {
		float layer = (_slices.getActiveImageID() - _slices.getDepth() / 2.0f)
				/ _slices.getDepth();

		/**
		 * What happens next: On view mode is active: The corresponding
		 * orthoslice shall be drawn on the layer which the 2d view shows. This
		 * slice will have (layer, 0,0) OR (0,layer,0) OR (0,0,layer) in its
		 * points. Therefore: Ternary Op which view mode is active
		 * 
		 * The other 2 view modes orthoslices will be drawn thru (0,0,0).
		 * 
		 **/

		float range = 0.5f;

		if (init) {
			initFront(range, newMode, layer);
			initSag(range, newMode, layer);
			initTrans(range, newMode, layer);
			return;
		}

		initOrtho(newMode, range, newMode, layer);
		if (lastMode != newMode) {
			initOrtho(lastMode, range, newMode, layer);
		}

	}

	private void initOrtho(int ortho, float range, int view_mode, float layer) {
		if (ortho == 0) {
			initTrans(range, view_mode, layer);
		} else if (ortho == 1) {
			initSag(range, view_mode, layer);
		} else if (ortho == 2) {
			initFront(range, view_mode, layer);
		}
	}

	private void initSag(float range, int view_mode, float layer) {
		BufferedImage img_sag;
		Point3f[] sag_slice = {
				new Point3f((view_mode == 1) ? layer : 0.0f, range, range),
				new Point3f((view_mode == 1) ? layer : 0.0f, -range, range),
				new Point3f((view_mode == 1) ? layer : 0.0f, -range, -range),
				new Point3f((view_mode == 1) ? layer : 0.0f, range, -range) };

		ortho_planes[1].setCoordinates(0, sag_slice);

		if (view_mode == 1) {
			img_sag = _slices.getImage(_slices.getActiveImageID(1), 1, 0x80000000);
		} else {
			img_sag = _slices.getImage(_slices.getDepth(1) / 2, 1,0x80000000);
		}

		ImageComponent2D i2d_sag = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_sag);

		Texture2D tex_sag = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA,
				img_sag.getWidth(), img_sag.getHeight());

		tex_sag.setImage(0, i2d_sag);
		app_for_ortho_planes[1].setTexture(tex_sag);
	}

	private void initTrans(float range, int view_mode, float layer) {
		BufferedImage img_trans;

		Point3f[] trans_slice = {
				new Point3f(range, range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(-range, range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(-range, -range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(range, -range, (view_mode == 0) ? layer : 0.0f) };

		ortho_planes[0].setCoordinates(0, trans_slice);

		if (view_mode == 0) {
			img_trans = _slices.getImage(_slices.getActiveImageID(), 0, 0x80000000);
		} else {
			img_trans = _slices.getImage(_slices.getNumberOfImages() / 2, 0, 0x80000000);
		}

		ImageComponent2D i2d_trans = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_trans);

		Texture2D tex_trans = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, img_trans.getWidth(), img_trans.getHeight());

		tex_trans.setImage(0, i2d_trans);

		app_for_ortho_planes[0].setTexture(tex_trans);

	}

	private void initFront(float range, int view_mode, float layer) {
		Point3f[] fron_slice = {
				new Point3f(range, (view_mode == 2) ? layer : 0.0f, range),
				new Point3f(-range, (view_mode == 2) ? layer : 0.0f, range),
				new Point3f(-range, (view_mode == 2) ? layer : 0.0f, -range),
				new Point3f(+range, (view_mode == 2) ? layer : 0.0f, -range) };

		ortho_planes[2].setCoordinates(0, fron_slice);

		BufferedImage img_front;

		if (view_mode == 2) {
			img_front = _slices.getImage(_slices.getActiveImageID(), 2, 0x80000000);
		} else {
			img_front = _slices.getImage(_slices.getDepth(2) / 2, 2, 0x80000000);
		}

		ImageComponent2D i2d_front = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_front);

		Texture2D tex_front = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, img_front.getWidth(), img_front.getHeight());

		tex_front.setImage(0, i2d_front);

		app_for_ortho_planes[2].setTexture(tex_front);
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
						Point3f point = new Point3f((x - w2), (y - h2),
								(i - seg.getMaskNum() / 2));
						if (addPoint(point)) {
							point.set(point.x / bitmask.getWidth(), point.y
									/ bitmask.getHeight(),
									point.z / seg.getMaskNum());
							pointsToShow.add(point);
						}

					}
				}
			}
		}

		if (pointsToShow.size() <= 0) {
			return;
		}

		PointArray points = new PointArray(pointsToShow.size(),
				PointArray.COORDINATES);

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

			color_ca.setColor(new Color3f(red / 256.0f, green / 256.0f,
					blue / 256.0f));
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
		GraphicsConfiguration config = SimpleUniverse
				.getPreferredConfiguration();
		_panel3d = new Panel3d(config);
		this.add(_panel3d, BorderLayout.CENTER);

	}

	/**
	 * calculates the 3d data structurs.
	 */
	public void update_view() {
		_panel3d.createScene();
		if (orthoEnabled)
			addOrthoSlices(mode, mode, false);
	}

	public void changeN(int n) {
		this.n = (float) n;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs
				.hasMoreElements();) {
			Segment seg = segs.nextElement();
			addPoints(seg);
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

		if (m._type == Message.M_SEG_CHANGED
				|| m._type == Message.M_REGION_GROW_SEG_CHANGED) {
			String seg_name = ((Segment) (m._obj)).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {

				System.out.println("need Update");
				addPoints((Segment) (m._obj));
				update_view();
			}
		}

		if (m._type == Message.M_NEW_ACTIVE_IMAGE) {
			update_view();
		}

		if (m._type == Message.M_LOADING_IMAGES_FINISHED) {
			initOrthoSlices();
			addOrthoSlices(mode, mode, true);
			initVolumeRendering();
			
			update_view();
		}

	}

	public void setViewMode(int mode) {
		int oldMode = this.mode;
		this.mode = mode;
		addOrthoSlices(oldMode, mode, false);
	}
}
