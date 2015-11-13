package misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import sun.text.IntHashtable;

/**
 * Implements the internal representation of a DICOM file. Stores all DataElements and makes them accessable via getDataElement(TagName). Also stores the pixel data & important information for displaying the contained image in seperate variables with special access functions.
 * 
 * @author Karl-Ingo Friese
 */
public class DiFile {
	private int _w;
	private int _h;
	private int _bits_stored;
	private int _bits_allocated;
	private Hashtable<Integer, DiDataElement> _data_elements;
	private int _image_number;
	String _file_name;

	/**
	 * Default Construtor - creates an empty DicomFile.
	 */
	public DiFile() {
		_w = _h = _bits_stored = _bits_allocated = _image_number = 0;
		_data_elements = new Hashtable<Integer, DiDataElement>();
		_file_name = null;
	}

	/**
	 * Initializes the DicomFile from a file. Might throw an exception (unexpected end of file, wrong data etc). This method will be implemented in exercise 1.
	 * 
	 * @param file_name
	 *            a string containing the name of a valid dicom file
	 * @throws Exception
	 */
	public void initFromFile(String file_name) throws Exception {
		// exercise 1
		System.out.println("Initializing File: " + file_name);
		_file_name = file_name;
		File f = new File(file_name);

		DiFileInputStream diFileInputStream = new DiFileInputStream(_file_name);
		if (!diFileInputStream.skipHeader()) {
			diFileInputStream.close();
			throw new IOException("False filetype");
		}

		boolean implicit = false;

		while (diFileInputStream.get_location() < f.length()) {
			DiDataElement nextEle = new DiDataElement();
			nextEle.setImplicit(implicit);
			nextEle.readNext(diFileInputStream);

			_data_elements.put(nextEle.getTag(), nextEle);
			System.out.println(nextEle.toString());
			if (DiDi.getTagDescr(nextEle.getTag()).equals("Transfer Syntax UID")) {
				if (nextEle.getValueAsString().equals("1.2.840.10008.1.2")) {
					implicit = true;
				}
			} else if (nextEle.getGroupID() == 0x0028) {
				if (nextEle.getElementID() == 0x0011) {
					_w = nextEle.getValueAsInt();
				} else if (nextEle.getElementID() == 0x0010) {
					_h = nextEle.getValueAsInt();
				} else if (nextEle.getElementID() == 0x0100) {
					_bits_allocated = nextEle.getValueAsInt();
				} else if (nextEle.getElementID() == 0x0101) {
					_bits_stored = nextEle.getValueAsInt();
				}
			} else if (nextEle.getGroupID() == 0x0020) {
				if (nextEle.getElementID() == 0x0013) {
					_image_number = nextEle.getValueAsInt();
				}
				// } else if(nextEle.getGroupID() == 0x7FE0) {
				// if(nextEle.getElementID() == 0x0010){
				// System.out.println("I am pixel data...........................");
				// }
			}
		}
		diFileInputStream.close();
		/** Apply window width/center & rescale slope/intercept on every pixel **/
		// Ternary Operators incoming: DiDataElement may be uninitialised
		DiDataElement w_center = this.getElement(0x00281050);
		int window_center = (w_center == null) ? Integer.MAX_VALUE : w_center.getValueAsInt();
		DiDataElement w_width = this.getElement(0x00281051);
		int window_width = (w_width == null) ? Integer.MAX_VALUE : w_width.getValueAsInt();
		DiDataElement r_slope = this.getElement(0x00281052);
		int rescale_slope = (r_slope == null) ? Integer.MAX_VALUE : r_slope.getValueAsInt();
		DiDataElement r_intercept = this.getElement(0x00281050);
		int rescale_intercept = (r_intercept == null) ? Integer.MAX_VALUE : r_intercept.getValueAsInt();

		if (window_center == -1 && window_width == -1 && rescale_intercept == -1 && rescale_slope == -1) {
			//Not the droids were looking for...
		} else {

			int stored_bits = this.getElement(0x00280101).getValueAsInt();
			int allocated_bytes = this.getElement(0x00280100).getValueAsInt()/8;
			byte[] picture_data = this.getElement(0x7FE00010).getValues();
			byte[] rescaled_data = new byte[picture_data.length]; 
			
			for (int i = 0; i < _w; i++) {
				for (int j = 0; j < _h; j++) {
										
					int raw = (picture_data[i * allocated_bytes + j * _w * allocated_bytes] & 0xff) | ((picture_data[i * allocated_bytes + j * _w * allocated_bytes + 1] & 0xff) << 8);
					int draw = raw >> stored_bits - 8;			
					
					/** Apply rescaling like in the appendix of excercise sheet 2 page 699 **/ 
					if(rescale_intercept != Integer.MAX_VALUE && rescale_slope != Integer.MAX_VALUE)	{
						draw = rescale_slope * draw + rescale_intercept;
					} 
					if(window_center != Integer.MAX_VALUE && window_width != Integer.MAX_VALUE)	{
						if(draw <= window_center - 0.5 - (window_width-1.0f)/2)	{	
							draw = 0;	//y_min
						} else if(draw > window_center - 0.5 + (window_width-1.0f)/2)	{
							draw = 255; 
						} else	{
							draw = (int) (((draw -(window_center - 0.5))/(window_width-1)+0.5)*(255));
						}
					} else	if(true /* Here maybe some sort of switch if auto-adpeting to 0-255 is acceptable*/){
						
					}
					//Check for consistency... May be removed anytime (after it works ;-) ) 
					if (draw > 255) {
						 System.err.println("to big");
						draw = 255;
					} else if (draw < 0) {
						draw = 0;
						 System.err.println("to small"); 
					} 
//					rescaled_data[]
					/** TODO: Translate int back to byte data... 
					 * This may be a bad ansatz. Translate draw back to byte. Append to a new byte[] and setValues OUTSIDE of the for loop... **/ 
//					byte[] the_new_pixel_data = ....; 
					rescaled_data[i * allocated_bytes + j * _w * allocated_bytes] = (byte) draw; 
				}
			}
			System.out.println("...................... Laenge neues Array: "+rescaled_data.length);
			this.getElement(0x7FE00010).setValues(rescaled_data);
		}
	}

	/**
	 * Converts a dicom file into a human readable string info. Might be long. Useful for debugging.
	 * 
	 * @return a human readable string representation
	 */
	public String toString() {
		String str = new String();

		str += _file_name + "\n";
		Enumeration<Integer> e = _data_elements.keys();
		List<String> l = new ArrayList<String>();

		while (e.hasMoreElements()) {
			Integer tag = e.nextElement();
			DiDataElement el = (DiDataElement) _data_elements.get(tag);
			l.add(el.toString());
		}

		Collections.sort(l);
		Iterator<String> it = l.iterator();
		while (it.hasNext()) {
			str += it.next();
		}

		return str;
	}

	/**
	 * Returns the number of allocated bits per pixel.
	 * 
	 * @return the number of allocated bits.
	 */
	public int getBitsAllocated() {
		return _bits_allocated;
	}

	/**
	 * Returns the number of bits per pixel that are actually used for color info.
	 * 
	 * @return the number of stored bits.
	 */
	public int getBitsStored() {
		return _bits_stored;
	}

	/**
	 * Allows access to the internal data element HashTable.
	 * 
	 * @return a reference to the data element HashTable
	 * @see IntHashtable
	 */
	public Hashtable<Integer, DiDataElement> getDataElements() {
		return _data_elements;
	}

	/**
	 * Returns the DiDataElement with the given id. Can return null.
	 * 
	 * @param id
	 * @return
	 */
	public DiDataElement getElement(int id) {
		return _data_elements.get(id);
	}

	/**
	 * Returns the image width of the contained dicom image.
	 * 
	 * @return the image width
	 */
	public int getImageWidth() {
		return _w;
	}

	/**
	 * Returns the image height of the contained dicom image.
	 * 
	 * @return the image height
	 */
	public int getImageHeight() {
		return _h;
	}

	/**
	 * Returns the file name of the current file.
	 * 
	 * @return the file name
	 */
	public String getFileName() {
		return _file_name;
	}

	/**
	 * Returns the image number in the current dicom series.
	 * 
	 * @return the image number
	 */
	public int getImageNumber() {
		return _image_number;
	}
}
