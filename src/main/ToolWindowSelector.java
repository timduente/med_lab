package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import javax.swing.*;
import javax.swing.event.*;

/**
 * GUI for making min-max based range segmentations.
 * 
 * @author Karl-Ingo Friese
 *
 */
@SuppressWarnings("serial")
public class ToolWindowSelector extends JPanel {
	private int _window, _center;
	private JList<String> _seg_list;
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
//
//		_seg_list = new JList<String>(slices.getSegNames());
//		_seg_list.setSelectedIndex(slices.getSegNames().indexOf(seg.getName()));
//		_seg_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//
//		_seg_list.addListSelectionListener(new ListSelectionListener() {
//			public void valueChanged(ListSelectionEvent e) {
//				int seg_index = _seg_list.getSelectedIndex();
//				String name = (String) (slices.getSegNames()
//						.getElementAt(seg_index));
//				if (!_seg.getName().equals(name)) {
//					_seg = slices.getSegment(name);
//					_window_sel_title.setText("Range Selector - "
//							+ _seg.getName());
//					// ...
//				}
//			}
//		});
//
//		JScrollPane scrollPane = new JScrollPane(_seg_list);
//		scrollPane
//				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		scrollPane
//				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

//		_window_sel_title = new JLabel("Range Selector - " + _seg.getName());

		// range_max needs to be calculated from the bits_stored value
		// in the current dicom series
		int range_max = (int) Math.pow(2, slices.getBitsStored());
		_window = slices.getWindowWidth(); //(int) Math.pow(2, slices.getBitsStored() - 1);
		_center = slices.getWindowCenter(); //(int) Math.pow(2, slices.getBitsStored() - 1);

		_width_label = new JLabel("Window width:");
		_center_label = new JLabel("Window center:");

		_width_slider = new JSlider(0, range_max, _window);
		_width_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_window = (int) source.getValue();
					System.out.println("_windowWidth_slider stateChanged: " + _window);
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
					System.out.println("_center_slider stateChanged: " + _center);
					slices.setWindowCenter(_center);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2); // top,left,bottom,right
//		c.weightx = 0.1;
//		c.gridx = 0;
//		c.gridy = 0;
//		//this.add(seg_sel_title, c);
//
//		c.gridheight = 2;
//		c.gridx = 0;
//		c.gridy = 1;
//		this.add(scrollPane, c);
//		c.gridheight = 1;

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
