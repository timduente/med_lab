package main;

import java.util.ArrayList;
import java.util.Observable;

import misc.BitMask;

/**
 * This class represents a segment. Simply spoken, a segment has a unique name,
 * a color for displaying in the 2d/3d viewport and contains n bitmasks where n
 * is the number of images in the image stack.
 * 
 * @author Karl-Ingo Friese
 */
public class Segment extends Observable {
	private String _name; // the segment name
	private int _color; // the segment color
	private int _w; // Bitmask width
	private int _h; // Bitmask height
	private BitMask[] _layers; // each segment contains an array of n bitmasks
	private String _type; 
	
	public String get_type()	{
		return _type; 
	}
	private int _min;

	public int get_min() {
		return _min;
	}

	public int get_max() {
		return _max;
	}
	

	private int _max;

	/**
	 * Constructor for new segment objects.
	 * 
	 * @param name
	 *            the name of the new segment
	 * @param w
	 *            the width of the bitmasks
	 * @param h
	 *            the height of the bitmasks
	 * @param layer_num
	 *            the total number of bitmasks
	 */
	public Segment(String name, int w, int h, int layer_num, String type) {
		this._name = name;
		this._w = w;
		this._h = h;
		this._type= type; 
//		System.out.println("I am "+ type);

		_color = 0xff00ff;
		_layers = new BitMask[layer_num];

		for (int i = 0; i < layer_num; i++) {
			_layers[i] = new BitMask(_w, _h);
		}
	}

	/**
	 * Fill the BitMask of this segment with the min-max-segmentation
	 * 
	 * @param min
	 *            minimum value
	 * @param max
	 *            maximum value
	 * @param slices
	 *            ImageStack
	 */
	public void create_range_seg(int min, int max, ImageStack slices) {
//		System.out.println("Range Creation: Min " + min);
//		System.out.println("Range Creation: Max " + max);
		_min = min;
		_max = max;
		int pixel_value;

		for (int i = 0; i < slices.getNumberOfImages(); i++) {

			// Next line: Get pixel data of image i...
			byte[] pixel_data = slices.getDiFile(i).getElement(0x7fe00010).getValues();

			for (int h = 0; h < _h; h++) {
				for (int w = 0; w < _w; w++) {

					pixel_value = (pixel_data[w * slices.getBytesPerPixel() + h * _w * slices.getBytesPerPixel()] & 0xff)
							| ((pixel_data[w * slices.getBytesPerPixel() + h * _w * slices.getBytesPerPixel() + 1] & 0xff) << 8);

					_layers[i].set(w, h, pixel_value >= min && pixel_value <= max);

				}
			}
		}
		setChanged();
		notifyObservers(new Message(Message.M_SEG_CHANGED, this));
	}

	public void create_regionGrow_seq(int v, int x, int y, int pictureNum, ImageStack slices) {
		// x and y between 0 and 1.

		BitMask[] marked = new BitMask[_layers.length];

		for (int i = 0; i < _layers.length; i++) {
			marked[i] = new BitMask(_w, _h);
			_layers[i] = new BitMask(_w, _h);
		}

		Voxel voxel = new Voxel(x, y, pictureNum);

		System.out.println("x: " + x + " y: " + y + " z: " + pictureNum);
		int value = slices.getDiFile(pictureNum).getPixel(x, y);
		this._min = value;
		this._max = v;
		
		
		int high = (int) (value * (((v + 100)) / 100.0f));
		int low = (int) (value * (((100 - v)) / 100.0f));

		System.out.println("Value: " + value + ", high " + high + ", low: " + low);

		ArrayList<Voxel> queue = new ArrayList<Voxel>();
		queue.add(voxel);

		int voxelValue = 0;

		while (!queue.isEmpty()) {
			voxel = queue.remove(0);
			voxelValue = slices.getDiFile(voxel.z).getPixel(voxel.x, voxel.y);

			if (voxelValue >= low && voxelValue <= high) {
				_layers[voxel.z].set(voxel.x, voxel.y, true);

				if (proofMarked(voxel.x + 1, voxel.y, voxel.z, marked)) {
					marked[voxel.z].set(voxel.x + 1, voxel.y, true);
					queue.add(new Voxel(voxel.x + 1, voxel.y, voxel.z));
				}

				if (proofMarked(voxel.x, voxel.y + 1, voxel.z, marked)) {
					marked[voxel.z].set(voxel.x, voxel.y + 1, true);
					queue.add(new Voxel(voxel.x, voxel.y + 1, voxel.z));
				}

				if (proofMarked(voxel.x - 1, voxel.y, voxel.z, marked)) {
					marked[voxel.z].set(voxel.x - 1, voxel.y, true);
					queue.add(new Voxel(voxel.x - 1, voxel.y, voxel.z));
				}

				if (proofMarked(voxel.x, voxel.y, voxel.z + 1, marked)) {
					marked[voxel.z + 1].set(voxel.x, voxel.y, true);
					queue.add(new Voxel(voxel.x, voxel.y, voxel.z + 1));
				}

				if (proofMarked(voxel.x, voxel.y, voxel.z - 1, marked)) {
					marked[voxel.z - 1].set(voxel.x, voxel.y, true);
					queue.add(new Voxel(voxel.x, voxel.y, voxel.z - 1));
				}

				if (proofMarked(voxel.x, voxel.y - 1, voxel.z, marked)) {
					marked[voxel.z].set(voxel.x, voxel.y - 1, true);
					queue.add(new Voxel(voxel.x, voxel.y - 1, voxel.z));
				}

			}

		}

		// System.out.println(_layers[50].toString());

		setChanged();
		notifyObservers(new Message(Message.M_REGION_GROW_SEG_CHANGED, this));

	}

	private boolean proofMarked(int x, int y, int z, BitMask[] marked) {
		return x >= 0 && x < _w && y >= 0 && y < _h && z >= 0 && z < _layers.length && !marked[z].get(x, y);
	}

	/**
	 * Returns the number of bitmasks contained in this segment.
	 * 
	 * @return the number of layers.
	 */
	public int getMaskNum() {
		return _layers.length;
	}

	/**
	 * Returns the Bitmask of a single layer.
	 * 
	 * @param i
	 *            the layer number
	 * @return the coresponding bitmask
	 */
	public BitMask getMask(int i) {
		return _layers[i];
	}

	/**
	 * Returns the name of the segment.
	 * 
	 * @return the segment name.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the name of the segment.
	 * 
	 * @param name
	 *            the new segment name
	 */
	public void setName(String name) {
		_name = name;
	}

	/**
	 * Returns the segment color as the usual rgb int value.
	 * 
	 * @return the color
	 */
	public int getColor() {
		return _color;
	}

	/**
	 * Sets the segment color.
	 * 
	 * @param color
	 *            the segment color (used when displaying in 2d/3d viewport)
	 */
	public void setColor(int color) {
		_color = color;
	}
}
