package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.event.*;

@SuppressWarnings("serial")
public class ToolWindowSelector extends JPanel {
	private int _window, _center;
	private JSlider _width_slider, _center_slider;
	private JLabel _window_sel_title, _width_label, _center_label;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices
	 *            the global image stack
	 * @param seg
	 *            the segmentation to be modified
	 */
	public ToolWindowSelector(SelectWindow sel_win) {
		//
		final ImageStack slices = ImageStack.getInstance();
		_window_sel_title = new JLabel("Edit window parameters");

		int range_max = (int) Math.pow(2, slices.getBitsStored());
		_window = slices.getWindowWidth(); 
		_center = slices.getWindowCenter(); 
		_width_label = new JLabel("Window width:");
		_center_label = new JLabel("Window center:");

		_width_slider = new JSlider(0, range_max, _window);
		_width_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_window = (int) source.getValue();
					System.out.println("_windowWidth_slider stateChanged: "
							+ _window);
					slices.setWindowWidth(_window);
				}
			}
		});

		_center_slider = new JSlider(0, range_max, _center);
		_center_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_center = (int) source.getValue();
					System.out.println("_center_slider stateChanged: "
							+ _center);
					slices.setWindowCenter(_center);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2); // top,left,bottom,right


		c.weightx = 0.9;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		this.add(_window_sel_title, c);
		c.gridwidth = 1;

		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 1;
		this.add(_width_label, c);
		c.gridx = 1;
		c.gridy = 2;
		this.add(_center_label, c);
		c.gridx = 2;
		c.gridy = 1;
		this.add(_width_slider, c);
		c.gridx = 2;
		c.gridy = 2;
		this.add(_center_slider, c);
	}
}
