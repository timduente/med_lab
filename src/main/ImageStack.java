package main;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JProgressBar;

import misc.DiFile;
import misc.DiFileInputStream;

/**
 * The ImageStack class represents all DicomFiles of a series and its segments.
 * It is the global data structure in YaDiV. This class is implemented as
 * singleton, meaning the constructor is private. Use getInstance() instead.
 * 
 * @author Karl-Ingo Friese
 */
public class ImageStack extends Observable {
	private static ImageStack _instance = null;
	private Vector<DiFile> _dicom_files;
	private DefaultListModel<String> _seg_names;
	private Hashtable<String, Segment> _segment;
	private DefaultListModel<String> _window_names;
	private Hashtable<String, SelectWindow> _windows;
	private String _dir_name;
	private int _w, _h, _active;
	private String pixelDataFormat;

//	private boolean loadFinished = false;

	private int bytesPerPixel, bitsStored;

	public int getBytesPerPixel() {
		return bytesPerPixel;
	}

	public int getBitsStored() {
		return bitsStored;
	}

	private int mode;

	private int windowWidth;

	public int getWindowWidth() {
		return windowWidth;
	}

	public void setWindowWidth(int windowWidth) {
		this.windowWidth = windowWidth;
		rescalePictureData();
		setChanged();
		notifyObservers(new Message(Message.M_WINDOW_CHANGED));
	}

	public int getWindowCenter() {
		return windowCenter;
	}

	public void setWindowCenter(int windowCenter) {
		this.windowCenter = windowCenter;
		rescalePictureData();
		setChanged();
		notifyObservers(new Message(Message.M_WINDOW_CHANGED));
	}

	private void rescalePictureData() {
		for (int i = 0; i < _dicom_files.size(); i++) {
			_dicom_files.get(i).rescale(windowWidth, windowCenter);
		}

	}

	private int windowCenter;

	/**
	 * Default Constructor.
	 */
	private ImageStack() {
		_dicom_files = new Vector<DiFile>();
		_segment = new Hashtable<String, Segment>();
		_seg_names = new DefaultListModel<String>();
		_windows = new Hashtable<String, SelectWindow>();
		_window_names = new DefaultListModel<String>();
		_dir_name = new String();
		_active = 0;
		mode = 0;
	}

	public static ImageStack getInstance() {
		if (_instance == null) {
			_instance = new ImageStack();
		}
		return _instance;
	}

//	public boolean loadingFinished() {
//		return loadFinished;
//	}

	/**
	 * Reads all DICOM files from the given directory. All files are checked for
	 * correctness before loading. The load process is implemented as a thread.
	 * 
	 * @param dir_name
	 *            string containing the directory name.
	 */
	public void initFromDirectory(String dir_name) {
		_dir_name = dir_name;
		_w = 0;
		_h = 0;

		// loading thread
		Thread t = new Thread() {
			JProgressBar progress_bar;

			// returns the image number of a dicom file or -1 if something wents
			// wrong
			private int check_file(File file) {
				int result = -1;

				if (!file.isDirectory()) {
					try {
						DiFileInputStream candidate = new DiFileInputStream(
								file);

						if (candidate.skipHeader()) {
							result = candidate.quickscan_for_image_number();
						}
						candidate.close();
					} catch (Exception ex) {
						ex.printStackTrace();
						// System.out.println("this will work after exercise 1");
						result = -1;
					}
				}

				return result;
			}

			// checks the DICOM files, retrieves their image number and loads
			// them in the right order.
			public void run() {
				Hashtable<Integer, String> map_number_to_difile_name = new Hashtable<Integer, String>();
				DiFile df;

				setChanged();
				notifyObservers(new Message(Message.M_CLEAR));

				JFrame progress_win = new JFrame("checking ...");
				progress_win.setResizable(false);
				progress_win.setAlwaysOnTop(true);

				File dir = new File(_dir_name);
				File[] files_unchecked = dir.listFiles();

				progress_bar = new JProgressBar(0, files_unchecked.length);
				progress_bar.setValue(0);
				progress_bar.setStringPainted(true);
				progress_win.add(progress_bar);
				progress_win.pack();
				// progress_bar.setIndeterminate(true);
				int main_width = (int) (LabMed.get_window().getSize()
						.getWidth());
				int main_height = (int) (LabMed.get_window().getSize()
						.getHeight());
				progress_win.setLocation(
						(main_width - progress_win.getSize().width) / 2,
						(main_height - progress_win.getSize().height) / 2);
				progress_win.setVisible(true);

				for (int i = 0; i < files_unchecked.length; i++) {
					int num = check_file(files_unchecked[i]);
					if (num >= 0) {
						map_number_to_difile_name.put(new Integer(num),
								files_unchecked[i].getAbsolutePath());
					}
					progress_bar.setValue(i + 1);
				}

				progress_win.setTitle("loading ...");

				Enumeration<Integer> e = map_number_to_difile_name.keys();
				List<Integer> l = new ArrayList<Integer>();
				while (e.hasMoreElements()) {
					l.add((Integer) e.nextElement());
				}

				String[] file_names = new String[l.size()];
				Collections.sort(l);
				Iterator<Integer> it = l.iterator();
				int file_counter = 0;
				while (it.hasNext()) {
					file_names[file_counter++] = map_number_to_difile_name
							.get(it.next());
				}

				progress_bar.setMaximum(file_names.length);
				progress_bar.setValue(0);

				_dicom_files.clear();
				_dicom_files.setSize(file_names.length);

				for (int i = 0; i < file_names.length; i++) {
					df = new DiFile();
					try {
						df.initFromFile(file_names[i]);

					} catch (Exception ex) {
						System.out.println(getClass()
								+ "::initFromDirectory -> failed to open "
								+ file_names[i]);
						System.out.println(ex);
						System.exit(0);
					}
					progress_bar.setValue(i + 1);
					_dicom_files.set(i, df);

					// initialize default image width and heigth from the first
					// image read
					// if (_w == 0)
					_w = df.getImageWidth();
					// if (_h == 0)
					_h = df.getImageHeight();

					bitsStored = df.getBitsStored();
					bytesPerPixel = df.getBitsAllocated() / 8;
					pixelDataFormat = df.getElement(0x00280004)
							.getValueAsString().trim();
					windowCenter = df.getWindow_center();
					windowWidth = df.getImageWidth();
					setChanged();
					notifyObservers(new Message(Message.M_NEW_IMAGE_LOADED));

				}

				progress_win.setVisible(false);
				
				setChanged();
				notifyObservers(new Message(Message.M_LOADING_IMAGES_FINISHED));
			}
		};

		t.start();
	}

	public String getPixelDataFormat() {
		return pixelDataFormat;
	}

	/**
	 * Adds a new segment with the given name.
	 * 
	 * @param name
	 *            the name of the new segment (must be unique)
	 * @return the new segment or null if the name was not unique
	 */
	public Segment addSegment(String name) {
		Segment seg;

		if (_segment.containsKey(name)) {
			seg = null;
		} else {
			int[] def_colors = { 0xff0000, 0x00ff00, 0x0000ff };
			seg = new Segment(name, _w, _h, _dicom_files.size(), "range");
			seg.setColor(def_colors[_segment.size()]);
			_segment.put(name, seg);
			_seg_names.addElement(name);
		}

		return seg;
	}

	public Segment addRegionGrowSegmenation(String name) {
		Segment seg;
		if (_segment.containsKey(name)) {
			seg = _segment.get(name);
		} else {
			seg = new Segment(name, _w, _h, _dicom_files.size(), "region");
			seg.setColor(0x91219E);
			_segment.put(name, seg);
			_seg_names.addElement(name);
		}

		return seg;
	}

	public SelectWindow addSelectWindow(String name) {
		SelectWindow sel_win;

		if (_windows.containsKey(name)) {
			sel_win = null;
		} else {
			sel_win = new SelectWindow(name, 0, 255);
			_windows.put(name, sel_win);
			_window_names.addElement(name);
		}

		return sel_win;
	}

	/**
	 * Returns the DicomFile from the series with image number i;
	 * 
	 * @param i
	 *            image number
	 * @return the DIOCM file
	 */
	public DiFile getDiFile(int i) {
		if (i < _dicom_files.size()) {
			return (DiFile) (_dicom_files.get(i));
		}

		return null;
	}

	/**
	 * Returns the segment with the given name.
	 * 
	 * @param name
	 *            the name of a segment
	 * @return the segment
	 */
	public Segment getSegment(String name) {
		return (Segment) (_segment.get(name));
	}

	/**
	 * Returns the number of segments.
	 * 
	 * @return the number of segments
	 */
	public int getSegmentNumber() {
		return _segment.size();
	}

	/**
	 * Returns the Number of DicomFiles in the ImageStack.
	 * 
	 * @return the number of files
	 */
	public int getNumberOfImages() {
		return _dicom_files.size();
	}

	public int getDepth(int mode) {
		if (mode == 0) {
			// System.out.println("Files " + _dicom_files.size());
			return _dicom_files.size();
		} else if (mode == 1) {
			// System.out.println("Files " + _h);
			return _w;
		} else if (mode == 2) {
			// System.out.println("Files " + _w);
			return _h;
		}
		return -1;
	}

	public int getDepth() {
		return getDepth(mode);
	}

	/**
	 * Returns the DefaultListModel containing the segment names.
	 * 
	 * @return guess what
	 */
	public DefaultListModel<String> getSegNames() {
		return _seg_names;
	}

	/**
	 * Returns the width of the images in the image stack.
	 * 
	 * @return the image width
	 */
	public int getImageWidth() {
		return getImageWidth(mode);
	}

	public int getImageWidth(int mode) {
		if (mode == 0) {
			return _w;
		} else if (mode == 1) {
			return _h;
		} else if (mode == 2) {
			return _w;
		}
		return -1;
	}

	/**
	 * Returns the height of the images in the image stack.
	 * 
	 * @return the image height
	 */
	public int getImageHeight() {
		return getImageHeight(mode);
	}

	public int getImageHeight(int mode) {

		if (mode == 0) {
			return _h;
		} else if (mode == 1) {
			return _dicom_files.size();
		} else if (mode == 2) {
			return _dicom_files.size();
		}
		return -1;
	}

	/**
	 * Returns the currently active image.
	 * 
	 * @return the currently active image
	 */
	public int getActiveImageID() {
		return _active;
	}

	/**
	 * Sets the currently active image in the viewmode.
	 * 
	 * @param i
	 *            the active image
	 */
	public void setActiveImage(int i) {
		_active = i;

		setChanged();
		notifyObservers(new Message(Message.M_NEW_ACTIVE_IMAGE, new Integer(i)));
	}

	public void setMode(int mode) {
		this.mode = mode;
		_active = 0;

		setChanged();
		if (mode == 0) {
			notifyObservers(new Message(Message.M_NEW_IMAGE_LOADED,
					new Integer(_dicom_files.size())));
		} else if (mode == 1) {
			notifyObservers(new Message(Message.M_NEW_IMAGE_LOADED,
					new Integer(_w)));
		} else if (mode == 2) {
			notifyObservers(new Message(Message.M_NEW_IMAGE_LOADED,
					new Integer(_h)));
		}

	}

	public int getMode() {
		return this.mode;
	}

	public byte[] getPictureData(int activeImage, int mode) {

		if (_dicom_files.size() == 0) {
			return null;
		}

		if (mode == 0) {
			return _dicom_files.get(activeImage).get_scaled_data();
		} else if (mode == 1) {
			byte[] sagitalPictureData = new byte[_h * _dicom_files.size()];

			for (int i = 0; i < _dicom_files.size(); i++) {
				DiFile diFile = _dicom_files.get(i);
				byte[] pictureData = diFile.get_scaled_data();

				for (int j = 0; j < _h; j++) {

					byte b = pictureData[j * _w + activeImage];
					sagitalPictureData[j + i * _h] = b;
				}

			}
			return sagitalPictureData;
		} else if (mode == 2) {
			byte[] frontalPictureData = new byte[_w * _dicom_files.size()];
			for (int i = 0; i < _dicom_files.size(); i++) {
				DiFile diFile = _dicom_files.get(i);
				byte[] pictureData = diFile.get_scaled_data();
				for (int j = 0; j < _h; j++) {
					frontalPictureData[j + i * _h] = pictureData[activeImage
							* _w + j];
				}

			}
			return frontalPictureData;
		}

		return new byte[10];
	}

	public BufferedImage getImage(int number, int mode) {
		byte[] pixData = getPictureData(number, mode);

		int max = 1024; 
		BufferedImage buf = new BufferedImage(max, max,
				BufferedImage.TYPE_INT_ARGB);

		int x, y, pixel, index;
		byte data;

		for (int i = 0; i < max; i++) {
			y = (int) (i * (getImageHeight(mode) / (double) max));
			for (int j = 0; j < max; j++) {
				x = (int) (j * getImageWidth(mode) / (double) max);
				 index = y * getImageWidth(mode) + x;

				if (index >= pixData.length) {
					index = pixData.length - 1;
				}

				data = pixData[index];

				pixel = 0x80000000|((data & 0xff) | 0xff000000
						| (data & 0xff) << 8 | (data & 0xff) << 16);
				buf.setRGB(j, i, pixel);
			}
		}

		return buf;
	}
}
