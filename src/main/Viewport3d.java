package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GraphicsConfiguration;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.Canvas3D;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.PointArray;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;

import misc.BitMask;

import com.sun.j3d.utils.geometry.ColorCube;
import com.sun.j3d.utils.universe.SimpleUniverse;

/**
 * Three dimensional viewport for viewing the dicom images + segmentations.
 * 
 * @author Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class Viewport3d extends Viewport implements Observer {

	private double rot = Math.PI / 4.0;

	private float n = 10;

	private boolean cube = false;
	private boolean test = true;

	private ArrayList<Point3f> pointsToShow;

	
	
	ColoringAttributes color_ca;

	/**
	 * Private class, implementing the GUI element for displaying the 3d data.
	 */
	public class Panel3d extends Canvas3D {
		public SimpleUniverse _simple_u;
		public BranchGroup _scene;

		public Panel3d(GraphicsConfiguration config) {
			super(config);

			pointsToShow = new ArrayList<Point3f>();

			setMinimumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setMaximumSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setPreferredSize(new Dimension(DEF_WIDTH, DEF_HEIGHT));
			setBackground(Color.black);

			_simple_u = new SimpleUniverse(this);
			_simple_u.getViewingPlatform().setNominalViewingTransform();
			_scene = null;
			createScene();

			if (cube) {
				new Thread(new Runnable() {
					public void run() {
						while (true) {
							rot = rot + 0.02;
							update_view();
							try {
								Thread.sleep(50);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

						}
					}

				}).start();

			}
			
			 color_ca = new ColoringAttributes();
			 color_ca.setCapability(ColoringAttributes.ALLOW_COLOR_WRITE);
		}

		public void createScene() {
			if (_scene != null) {
				_scene.detach();
			}
			_scene = new BranchGroup();
			_scene.setCapability(BranchGroup.ALLOW_DETACH);

			if (cube) {
				Transform3D rotate = new Transform3D();
				rotate.rotY(rot);
				Transform3D rot2 = new Transform3D();
				rot2.rotX(rot);
				TransformGroup rotate_group = new TransformGroup(rotate);
				TransformGroup rotate_group2 = new TransformGroup(rot2);
				rotate_group.addChild(rotate_group2);
				_scene.addChild(rotate_group);
				rotate_group2.addChild(new ColorCube(0.4));
			} else {

				// TODO: für jede Segmentierung ColorAttribut ändern.
				Appearance ap = new Appearance();
				ap.setColoringAttributes(color_ca);
				PointAttributes pAtts = new PointAttributes();
				pAtts.setPointSize(1.0f);
				ap.setPointAttributes(pAtts);

				// pointsToShow.clear();

				// Point3f p0 = new Point3f(0.1f, 0.1f, 0.2f);
				// Point3f p1 = new Point3f(0.5f, 0.1f, 0.2f);
				// Point3f p2 = new Point3f(0.3f, 0.2f, 0.4f);
				// Point3f p3 = new Point3f(0.0f, 0.0f, 0.0f);
				//
				// addPoint(p0);
				// addPoint(p1);
				// addPoint(p2);
				// addPoint(p3);

				System.out.println("Size: " + pointsToShow.size());

				if (pointsToShow.size() > 0) {

					PointArray points = new PointArray(pointsToShow.size(),
							PointArray.COORDINATES);

					for (int i = 0; i < pointsToShow.size(); i++) {
						points.setCoordinate(i, pointsToShow.get(i));
					}

					Shape3D three_points_shape = new Shape3D(points, ap);

					Transform3D scaleTransform = new Transform3D();
					scaleTransform.setScale(0.005);
					TransformGroup tGroup = new TransformGroup(scaleTransform);

					tGroup.addChild(three_points_shape);

					_scene.addChild(tGroup);
				}
			}

			_scene.compile();
			_simple_u.addBranchGraph(_scene);

		}

	}

	private void addPoint(Point3f point) {
		if ((point.x % n == 0 && point.y % n == 0 && point.z % n == 0)) {
			pointsToShow.add(point);
		}

	}

	private void addPoints(Segment seg) {
		pointsToShow.clear();
		int w2 = seg.getMask(0).getWidth() / 2;
		int h2 = seg.getMask(0).getHeight() / 2;
		for (int i = 0; i < seg.getMaskNum(); i++) {
			BitMask bitmask = seg.getMask(i);
			for (int y = 0; y < bitmask.getHeight(); y++) {
				for (int x = 0; x < bitmask.getWidth(); x++) {
					if (bitmask.get(x, y)) {
						addPoint(new Point3f((x - w2)
								, (y - h2)
								, (i)
								));
					}
				}
			}
		}
		int color = seg.getColor();

		int red = (color >> 16) & 0xff;
		int green = (color >> 8) & 0xff;
		int blue = color & 0xff;

		color_ca.setColor(new Color3f(red/256.0f, green/256.0f, blue/256.0f));
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
	}

	public void changeN(int n) {
		this.n = (float) n ;
		for (Enumeration<Segment> segs = _map_name_to_seg.elements(); segs.hasMoreElements();){
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

		if (m._type == Message.M_SEG_CHANGED) {
			String seg_name = ((Segment) (m._obj)).getName();
			boolean update_needed = _map_name_to_seg.containsKey(seg_name);
			if (update_needed) {

				System.out.println("need Update");
				addPoints((Segment) (m._obj));
				update_view();
			}
		}
	}
}
