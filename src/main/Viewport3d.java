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
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

import misc.BitMask;

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

	Viewport2d v2d;

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
			initOrthoSlices();

			createScene();

		}

		public void createScene() {

			if (bgroup == null) {
				bgroup = new BranchGroup();
				bgroup.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
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

	QuadArray trans_plane;
	QuadArray sag_plane;
	QuadArray fron_plane;

	Appearance ap_trans;
	Appearance ap_sag;
	Appearance ap_front;

	private void initOrthoSlices() {
		trans_plane = new QuadArray(4, QuadArray.COORDINATES
				| GeometryArray.TEXTURE_COORDINATE_2);
		sag_plane = new QuadArray(4, QuadArray.COORDINATES
				| GeometryArray.TEXTURE_COORDINATE_2);
		fron_plane = new QuadArray(4, QuadArray.COORDINATES
				| GeometryArray.TEXTURE_COORDINATE_2);

		trans_plane.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		sag_plane.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		fron_plane.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);

		trans_plane.setTextureCoordinate(0, 0, new TexCoord2f(1.0f, 0.0f));
		trans_plane.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
		trans_plane.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
		trans_plane.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

		sag_plane.setTextureCoordinate(0, 0, new TexCoord2f(1.0f, 0.0f));
		sag_plane.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
		sag_plane.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
		sag_plane.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

		fron_plane.setTextureCoordinate(0, 0, new TexCoord2f(1.0f, 0.0f));
		fron_plane.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
		fron_plane.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
		fron_plane.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

		ap_trans = new Appearance();
		ap_sag = new Appearance();
		ap_front = new Appearance();

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

		ap_trans.setTextureAttributes(ta);
		ap_trans.setPolygonAttributes(polygonAttributs);
		ap_trans.setTransparencyAttributes(tpAtt);
		ap_trans.setColoringAttributes(color_ca);
		ap_trans.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

		ap_sag.setPolygonAttributes(polygonAttributs);
		ap_sag.setTextureAttributes(ta);
		ap_sag.setTransparencyAttributes(tpAtt);
		ap_sag.setColoringAttributes(color_ca);
		ap_sag.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

		ap_front.setPolygonAttributes(polygonAttributs);
		ap_front.setTextureAttributes(ta);
		ap_front.setTransparencyAttributes(tpAtt);
		ap_front.setColoringAttributes(color_ca);
		ap_front.setCapability(Appearance.ALLOW_TEXTURE_WRITE);

		Shape3D sag_shape = new Shape3D(sag_plane, ap_sag);
		sag_shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		
		Shape3D fron_shape = new Shape3D(fron_plane, ap_front);
		fron_shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);
		
		Shape3D trans_shape = new Shape3D(trans_plane, ap_trans);
		trans_shape.setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

		shapes.put("Orthoslices_sag", sag_shape);
		shapes.put("Orthoslices_fron", fron_shape);
		shapes.put("Orthoslices_trans", trans_shape);
	}

	private void addOrthoSlices() {

		int view_mode = _slices.getMode();

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

		// System.out.println("I am in "
		// + ((view_mode == 0) ? "transversal"
		// : (view_mode == 1) ? "sagital" : "frontal")
		// + " viewmode and the active image is: "
		// + _slices.getActiveImageID() + " while the layer is: " + layer);

		Point3f[] trans_slice = {
				new Point3f(range, range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(-range, range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(-range, -range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(range, -range, (view_mode == 0) ? layer : 0.0f) };
		Point3f[] sag_slice = {
				new Point3f((view_mode == 1) ? layer : 0.0f, range, range),
				new Point3f((view_mode == 1) ? layer : 0.0f, -range, range),
				new Point3f((view_mode == 1) ? layer : 0.0f, -range, -range),
				new Point3f((view_mode == 1) ? layer : 0.0f, +range, -range) };
		Point3f[] fron_slice = {
				new Point3f(range, (view_mode == 2) ? layer : 0.0f, range),
				new Point3f(-range, (view_mode == 2) ? layer : 0.0f, range),
				new Point3f(-range, (view_mode == 2) ? layer : 0.0f, -range),
				new Point3f(+range, (view_mode == 2) ? layer : 0.0f, -range) };

		trans_plane.setCoordinates(0, trans_slice);
		sag_plane.setCoordinates(0, sag_slice);
		fron_plane.setCoordinates(0, fron_slice);

		BufferedImage img_trans;
		BufferedImage img_sag;
		BufferedImage img_front;

		if (view_mode == 0) {
			img_trans = _slices.getImage(_slices.getActiveImageID(), 0);
		} else {
			img_trans = _slices.getImage(_slices.getNumberOfImages() / 2, 0);
		}

		ImageComponent2D i2d_trans = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_trans);

		Texture2D tex_trans = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, img_trans.getWidth(), img_trans.getHeight());

		tex_trans.setImage(0, i2d_trans);

		if (view_mode == 1) {
			img_sag = _slices.getImage(_slices.getActiveImageID(), 1);
		} else {
			img_sag = _slices.getImage(_slices.getDepth(1) / 2, 1);
		}

		ImageComponent2D i2d_sag = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_sag);

		Texture2D tex_sag = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA,
				img_sag.getWidth(), img_sag.getHeight());

		tex_sag.setImage(0, i2d_sag);

		if (view_mode == 2) {
			img_front = _slices.getImage(_slices.getActiveImageID(), 2);
		} else {
			img_front = _slices.getImage(_slices.getDepth(2) / 2, 2);
		}

		ImageComponent2D i2d_front = new ImageComponent2D(
				ImageComponent2D.FORMAT_RGBA, img_front);

		Texture2D tex_front = new Texture2D(Texture2D.BASE_LEVEL,
				Texture2D.RGBA, img_front.getWidth(), img_front.getHeight());

		tex_front.setImage(0, i2d_front);

		ap_trans.setTexture(tex_trans);
		ap_sag.setTexture(tex_sag);
		ap_front.setTexture(tex_front);

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
		if (_slices.loadingFinished())
			this.addOrthoSlices();
	}

	public void changeN(int n) {
		this.n = (float) n;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs
				.hasMoreElements();) {
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
			// changeSlices();
			update_view();
		}
	}
}
