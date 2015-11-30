package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ToolRegionGrowSelector extends JPanel {
	
	private int _variance;
	private Segment _seg;
	private JSlider _variance_slider;
	private JLabel _region_grow_title, _min_label;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices
	 *            the global image stack
	 * @param seg
	 *            the segmentation to be modified
	 */
	public ToolRegionGrowSelector(SelectWindow sel_win) {
//		_seg = seg;

		final ImageStack slices = ImageStack.getInstance();
		JLabel seg_sel_title = new JLabel("Edit Segmentation");

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
//					_range_sel_title.setText("Range Selector - "
//							+ _seg.getName());
//					
//					_min_slider.setValue(_seg.get_min());
//					_max_slider.setValue(_seg.get_max());
//					
//
//				}
//			}
//		});

//		JScrollPane scrollPane = new JScrollPane(_seg_list);
//		scrollPane
//				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
//		scrollPane
//				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		_region_grow_title = new JLabel("Region Grow Segmentation");

		// range_max needs to be calculated from the bits_stored value
		// in the current dicom series
		int range_max = 100;
		_variance = 10;

		_min_label = new JLabel("Varianz");

		_variance_slider = new JSlider(0, range_max, _variance);
		_variance_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_variance = (int) source.getValue();
					//System.out.println("_min_slider stateChanged: " + _min);
					changeRange(_variance, slices, _seg);
				}
			}
		});


		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2); // top,left,bottom,right
		c.weightx = 0.1;
		c.gridx = 0;
		c.gridy = 0;
		this.add(seg_sel_title, c);

		c.gridheight = 1;

		c.weightx = 0.9;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		this.add(_region_grow_title, c);
		c.gridwidth = 1;

		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 1;
		this.add(_min_label, c);
		
		c.gridx = 2;
		c.gridy = 1;
		this.add(_variance_slider, c);
		

	}

	private void changeRange(int variance, ImageStack slices, Segment seg) {
		//TODO:
	}

}
