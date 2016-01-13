package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
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
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TransformGroup;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;

import misc.BitMask;

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

	private float n = 10;
	//Dont need them, because changing perspective resets currently active image. IF this will be changed, the ansatz is here to remember the current image in the views.
//	private int remember_sag = 0;
//	private int remember_trans = 0;
//	private int remember_frontal = 0;

	Hashtable<String, Shape3D> shapes = new Hashtable<String, Shape3D>();

	MouseWheelZoom mouseWheelZoom;
	TransformGroup tGroup;
	BranchGroup bgroup;

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

			createScene();

		}

		public void createScene() {

			if (bgroup != null) {
				bgroup.detach();
				tGroup.removeChild(bgroup);
				bgroup.removeAllChildren();
			}

			if (shapes.size() > 0) {

				bgroup = new BranchGroup();
				bgroup.setCapability(BranchGroup.ALLOW_DETACH);

				Enumeration<Shape3D> elements = shapes.elements();
				while (elements.hasMoreElements()) {
					Shape3D shape = elements.nextElement();
					bgroup.addChild(shape);

				}

				tGroup.addChild(bgroup);
			}

			if (!_scene.isCompiled()) {
				_scene.compile();
				_simple_u.addBranchGraph(_scene);
			}
		}
	}

	private void addOrthoSlices() {
		System.out.println("Adding orthoSlices");
		if (shapes.containsKey("OrthoSlices")) {
			shapes.remove("OrthoSlices");
		}

		ArrayList<Point3f> pointsToShow = new ArrayList<Point3f>();
		int activeImageID =- _slices.getDepth()/2-_slices.getActiveImageID(); 
		// How do i get the mode? How do i get the other modes active images? Grab it! And save it! Else use default 0! Problem solved - wub wub!
		int img_width = _slices.getImageWidth();
		int img_height = _slices.getImageHeight();
		int view_mode = _slices.getMode();
		float range = (img_width < img_height) ? img_width / 2 : img_height / 2;

		for (float i = -range; i < range; i++) {
			for (float j = -range; j < range; j++) {
				// Trans = 0
				Point3f pointT = new Point3f(i/range, j/range, (view_mode == 0) ? activeImageID/range : 0);
				// Sagi = 1
				Point3f pointS = new Point3f((view_mode == 1) ? activeImageID/range : 0, i/range, j/range);
				// Front = 2  i/range, (view_mode == 1) ? activeImageID/range : 0, 
				Point3f pointF = new Point3f(i/range, (view_mode == 2) ? activeImageID/range : 0, j/range);
				pointsToShow.add(pointT);
				pointsToShow.add(pointS);
				pointsToShow.add(pointF);
			}
		}

		// for (int ix = 0; ix < 400 / 4; ix++) {
		// for (int iy = 0; iy < 400 / 4; iy++) {
		// Point3f point = new Point3f(ix, iy, 0);
		// pointsToShow.add(point);
		// }
		// }

		if (pointsToShow.size() <= 0) {
			System.out.println("empty");
			return;
		} else {
			System.out.println("I have " + pointsToShow.size() + " points to draw");
		}

		int color = 0x888888;

		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;

		ColoringAttributes color_ca = new ColoringAttributes();
		// color_ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);

		Appearance ap = new Appearance();
		ap.setColoringAttributes(color_ca);
		PointAttributes pAtts = new PointAttributes();
		pAtts.setPointSize(1.0f);
		ap.setPointAttributes(pAtts);
		ap.setTransparencyAttributes(new TransparencyAttributes(TransparencyAttributes.BLENDED, 0.8f));

		color_ca.setColor(new Color3f(red / 256.0f, green / 256.0f, blue / 256.0f));

		PointArray points = new PointArray(pointsToShow.size(), PointArray.COORDINATES);

		for (int i = 0; i < pointsToShow.size(); i++) {
			points.setCoordinate(i, pointsToShow.get(i));
		}

		shapes.put("OrthoSlice", new Shape3D(points, ap));

	}

	private boolean addPoint(Point3f point) {
		return (point.x % n == 0 && point.y % n == 0 && point.z % n == 0);

	}

	private void addPoints(Segment seg) {
		ArrayList<Point3f> pointsToShow = new ArrayList<Point3f>();

		if (shapes.containsKey(seg.getName())) {
			shapes.remove(seg.getName());
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

		int color = seg.getColor();

		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;

		ColoringAttributes color_ca = new ColoringAttributes();
		// color_ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);

		Appearance ap = new Appearance();
		ap.setColoringAttributes(color_ca);
		PointAttributes pAtts = new PointAttributes();
		pAtts.setPointSize(1.0f);
		ap.setPointAttributes(pAtts);

		color_ca.setColor(new Color3f(red / 256.0f, green / 256.0f, blue / 256.0f));

		PointArray points = new PointArray(pointsToShow.size(), PointArray.COORDINATES);

		for (int i = 0; i < pointsToShow.size(); i++) {
			points.setCoordinate(i, pointsToShow.get(i));
		}

		shapes.put(seg.getName(), new Shape3D(points, ap));

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
	}

	public void changeN(int n) {
		this.n = (float) n;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs.hasMoreElements();) {
			Segment seg = segs.nextElement();
			addPoints(seg);
		}
		/* TODO: Temporary: */
		changeSlices();
		// END TEMP

		update_view();
	}

	// TODO: Search for a meaningfull place to be called
	public void changeSlices() {
		this.addOrthoSlices();
	}

	/**
	 * Implements the observer function update. Updates can be triggered by the global image stack.
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

		if (m._type == Message.M_SEG_CHANGED || m._type == Message.M_REGION_GROW_SEG_CHANGED) {
			String seg_name = ((Segment) (m._obj)).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {

				System.out.println("need Update");
				addPoints((Segment) (m._obj));
				update_view();
			}
		}
		
		if (m._type == Message.M_NEW_ACTIVE_IMAGE) {
			changeSlices();
			update_view();
		}
	}
}
