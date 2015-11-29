package main;

import java.util.Observable;

/**
 * 
 */
public class SelectWindow extends Observable {
	private String _name; // the  name
	private int _window; // 
	private int _center; // 
	/**
	 * Constructor for new window objects.
	 * @param name the name of the new window selection
	 * @param w the width of the bitmasks
	 * @param h the height of the bitmasks
	 */
	public SelectWindow(String name, int _window, int _center) {
		this._name = name;
		this._window = _window;
		this._center = _center;
	}

	/**
	 * Returns the name of the window.
	 * 
	 * @return the window name.
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
	 * @return the _window
	 */
	public int get_window() {
		return _window;
	}

	/**
	 * @param _window the _window to set
	 */
	public void set_window(int _window) {
		this._window = _window;
	}

	/**
	 * @return the _center
	 */
	public int get_center() {
		return _center;
	}

	/**
	 * @param _center the _center to set
	 */
	public void set_center(int _center) {
		this._center = _center;
	}
	
}
