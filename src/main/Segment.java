package main;

import java.util.Observable;

import com.sun.org.apache.bcel.internal.generic.NEW;

import misc.BitMask;
import misc.DiFile;

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
	public Segment(String name, int w, int h, int layer_num) {
		this._name = name;
		this._w = w;
		this._h = h;

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
		int pixel_value;

		// START Approach #2 PART 1
		// int stored_bits = slices.getBitsStored();
		// int allocated_bytes = slices.getBytesPerPixel();
		// END Approach #2 PART 1

		for (int i = 0; i < slices.getNumberOfImages(); i++) {
			// System.out.println(i);
			// Next line: Get pixel data of image i...
			byte[] pixel_data = slices.getDiFile(i).getElement(0x7fe00010)
					.getValues();
			System.out.println("segment pixel data: " + pixel_data.length);
			for (int w = 0; w < _w; w++) {
				for (int h = 0; h < _h; h++) {
					// START Approach #2 PART 2
					// int raw = pixel_data[w * allocated_bytes + h * _w *
					// allocated_bytes] & 0xff | ((pixel_data[w *
					// allocated_bytes + h * _w * allocated_bytes + 1] & 0xff)
					// << 8);
					// pixel_value = raw >> stored_bits - 8;
					// START Approach #2 PART 2
					/** Approach 1 or 2 makes no difference Oo **/
					// START Approach #1
					pixel_value = (pixel_data[w * slices.getBytesPerPixel() + h
							* _w * slices.getBytesPerPixel()] & 0xff)
							| ((pixel_data[w * slices.getBytesPerPixel() + h
									* _w * slices.getBytesPerPixel() + 1] & 0xff) << 8);

					// END Approach #1

					_layers[i].set(w, h,
							(pixel_value >= min && pixel_value <= max));
					// if(pixel_value > min && pixel_value < max) {
					// _layers[i].set(w, h, true);
					// } else {
					// _layers[i].set(w, h, false);
					// }
				}
			}
		}
		setChanged();
		notifyObservers(new Message(Message.M_SEG_CHANGED, this));
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
