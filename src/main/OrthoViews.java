package main;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.ColoringAttributes;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.ImageComponent2D;
import javax.media.j3d.PolygonAttributes;
import javax.media.j3d.QuadArray;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Texture2D;
import javax.media.j3d.TextureAttributes;
import javax.media.j3d.TransparencyAttributes;
import javax.vecmath.Color3f;
import javax.vecmath.Point3f;
import javax.vecmath.TexCoord2f;

public class OrthoViews {
	ImageStack _slices = ImageStack.getInstance();
	
	private QuadArray[] ortho_planes = new QuadArray[3];
	private Appearance[] app_for_ortho_planes = new Appearance[3];
	private ArrayList<Shape3D> volume_slices = new ArrayList<Shape3D>();
	
	
	public void initOrthoSlices(BranchGroup orthoNode) {
		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.COMBINE);

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(1.0f, 1.0f, 1.0f));

		PolygonAttributes polygonAttributs = new PolygonAttributes();
		polygonAttributs.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributs.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		TransparencyAttributes tpAtt = new TransparencyAttributes(TransparencyAttributes.BLENDED, .4f, TransparencyAttributes.BLEND_SRC_ALPHA,
				TransparencyAttributes.BLEND_ONE);

		Shape3D[] ortho_shapes = new Shape3D[3];

		for (int i = 0; i < ortho_planes.length; i++) {

			app_for_ortho_planes[i] = initAppearanceForOrthoPlanes(ta, color_ca, polygonAttributs, tpAtt);
			ortho_planes[i] = initQuadArray();

			ortho_shapes[i] = new Shape3D(ortho_planes[i], app_for_ortho_planes[i]);
			ortho_shapes[i].setCapability(Shape3D.ALLOW_GEOMETRY_WRITE);

			orthoNode.addChild(ortho_shapes[i]);
		}
	}
	
	public void addOrthoSlices(int lastMode, int newMode, boolean init) {
		float layer = (_slices.getActiveImageID() - _slices.getDepth() / 2.0f) / _slices.getDepth();

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

	public void initVolumeRendering(BranchGroup volume2DRendering) {

		TextureAttributes ta = new TextureAttributes();
		ta.setTextureMode(TextureAttributes.COMBINE);

		ColoringAttributes color_ca = new ColoringAttributes();
		color_ca.setColor(new Color3f(0.3f, 0.3f, 0.3f));

		PolygonAttributes polygonAttributs = new PolygonAttributes();
		polygonAttributs.setCullFace(PolygonAttributes.CULL_NONE);
		polygonAttributs.setPolygonMode(PolygonAttributes.POLYGON_FILL);
		TransparencyAttributes tpAtt = new TransparencyAttributes(TransparencyAttributes.BLENDED, .2f, TransparencyAttributes.BLEND_SRC_ALPHA,
				TransparencyAttributes.BLEND_ONE);

		float range = 0.5f;

		for (int i = 0; i < _slices.getDepth(0); i++) {
			Appearance ap = initAppearanceForOrthoPlanes(ta, color_ca, polygonAttributs, tpAtt);
			QuadArray quad = initQuadArray();
			Shape3D onePlane = new Shape3D(quad, ap);
			volume_slices.add(onePlane);
			initArea(quad, ap, range, 0, i);
			volume2DRendering.addChild(onePlane);
		}
	}

	private void initArea(QuadArray quad, Appearance ap, float range, int view_mode, int imageIndex) {
		BufferedImage img_trans;

		float layer = (imageIndex - _slices.getDepth(view_mode) / 2.0f) / _slices.getDepth(view_mode);

		Point3f[] slice = { new Point3f(range, range, layer), new Point3f(-range, range, layer), new Point3f(-range, -range, layer),
				new Point3f(range, -range, layer) };

		quad.setCoordinates(0, slice);

		img_trans = _slices.getImage(imageIndex, view_mode, 0x04000000);

		ImageComponent2D i2d = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img_trans);

		Texture2D tex = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img_trans.getWidth(), img_trans.getHeight());

		tex.setImage(0, i2d);

		ap.setTexture(tex);

	}
	
	private Appearance initAppearanceForOrthoPlanes(TextureAttributes ta, ColoringAttributes color_ca, PolygonAttributes polygonAttributs,
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
		QuadArray quadArray = new QuadArray(4, QuadArray.COORDINATES | GeometryArray.TEXTURE_COORDINATE_2);
		quadArray.setCapability(GeometryArray.ALLOW_COORDINATE_WRITE);
		quadArray.setTextureCoordinate(0, 0, new TexCoord2f(1.0f, 0.0f));
		quadArray.setTextureCoordinate(0, 1, new TexCoord2f(0.0f, 0.0f));
		quadArray.setTextureCoordinate(0, 2, new TexCoord2f(0.0f, 1.0f));
		quadArray.setTextureCoordinate(0, 3, new TexCoord2f(1.0f, 1.0f));

		return quadArray;
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
		Point3f[] sag_slice = { new Point3f((view_mode == 1) ? layer : 0.0f, range, range), new Point3f((view_mode == 1) ? layer : 0.0f, -range, range),
				new Point3f((view_mode == 1) ? layer : 0.0f, -range, -range), new Point3f((view_mode == 1) ? layer : 0.0f, range, -range) };

		ortho_planes[1].setCoordinates(0, sag_slice);

		if (view_mode == 1) {
			img_sag = _slices.getImage(_slices.getActiveImageID(1), 1, 0x80000000);
		} else {
			img_sag = _slices.getImage(_slices.getDepth(1) / 2, 1, 0x80000000);
		}

		ImageComponent2D i2d_sag = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img_sag);

		Texture2D tex_sag = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img_sag.getWidth(), img_sag.getHeight());

		tex_sag.setImage(0, i2d_sag);
		app_for_ortho_planes[1].setTexture(tex_sag);
	}

	private void initTrans(float range, int view_mode, float layer) {
		BufferedImage img_trans;

		Point3f[] trans_slice = { new Point3f(range, range, (view_mode == 0) ? layer : 0.0f), new Point3f(-range, range, (view_mode == 0) ? layer : 0.0f),
				new Point3f(-range, -range, (view_mode == 0) ? layer : 0.0f), new Point3f(range, -range, (view_mode == 0) ? layer : 0.0f) };

		ortho_planes[0].setCoordinates(0, trans_slice);

		if (view_mode == 0) {
			img_trans = _slices.getImage(_slices.getActiveImageID(), 0, 0x80000000);
		} else {
			img_trans = _slices.getImage(_slices.getNumberOfImages() / 2, 0, 0x80000000);
		}

		ImageComponent2D i2d_trans = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img_trans);

		Texture2D tex_trans = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img_trans.getWidth(), img_trans.getHeight());

		tex_trans.setImage(0, i2d_trans);

		app_for_ortho_planes[0].setTexture(tex_trans);

	}

	private void initFront(float range, int view_mode, float layer) {
		Point3f[] fron_slice = { new Point3f(range, (view_mode == 2) ? layer : 0.0f, range), new Point3f(-range, (view_mode == 2) ? layer : 0.0f, range),
				new Point3f(-range, (view_mode == 2) ? layer : 0.0f, -range), new Point3f(+range, (view_mode == 2) ? layer : 0.0f, -range) };

		ortho_planes[2].setCoordinates(0, fron_slice);

		BufferedImage img_front;

		if (view_mode == 2) {
			img_front = _slices.getImage(_slices.getActiveImageID(), 2, 0x80000000);
		} else {
			img_front = _slices.getImage(_slices.getDepth(2) / 2, 2, 0x80000000);
		}

		ImageComponent2D i2d_front = new ImageComponent2D(ImageComponent2D.FORMAT_RGBA, img_front);

		Texture2D tex_front = new Texture2D(Texture2D.BASE_LEVEL, Texture2D.RGBA, img_front.getWidth(), img_front.getHeight());

		tex_front.setImage(0, i2d_front);

		app_for_ortho_planes[2].setTexture(tex_front);
	}

}
