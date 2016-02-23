package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.*;
import javax.swing.event.*;

/**
 * GUI for making min-max based range segmentations.
 * 
 * @author Karl-Ingo Friese
 * 
 */
@SuppressWarnings("serial")
public class ToolRangeSelector extends JPanel implements Observer {
	private static final int START_VARIANCE = 10;
	private int _min, _max, _variance;
	private Segment _seg;
	private JList<String> _seg_list;
	private JSlider _min_slider, _max_slider, _variance_slider;
	private JLabel _range_sel_title, _min_label, _max_label;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices
	 *            the global image stack
	 * @param seg
	 *            the segmentation to be modified
	 */
	public ToolRangeSelector(Segment seg) {
		_seg = seg;

		Voxel.vox.addObserver(this);

		_variance = START_VARIANCE;

		final ImageStack slices = ImageStack.getInstance();
		JLabel seg_sel_title = new JLabel("Edit Segmentation");

		_seg_list = new JList<String>(slices.getSegNames());
		_seg_list.setSelectedIndex(slices.getSegNames().indexOf(seg.getName()));
		_seg_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		_seg_list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				int seg_index = _seg_list.getSelectedIndex();
				String name = (String) (slices.getSegNames()
						.getElementAt(seg_index));
				// TODO: Here needs to be set if variance or minmax slider
				// should be put!
				if (!_seg.getName().equals(name)) {
					_seg = slices.getSegment(name);

					if (_seg.get_type().equals("range")) {
						_range_sel_title.setText("Range Selector - "
								+ _seg.getName());
						int range_max = (int) Math.pow(2,
								slices.getBitsStored());
						_min = (int) Math.pow(2, slices.getBitsStored() - 1);
						_min_slider.setMaximum((int) Math.pow(2,
								slices.getBitsStored()));
						_min_slider.setMinimum(0);
						_min_slider.setValue(_seg.get_min());
						_max_slider.setVisible(true);
						_max_slider.setValue(_seg.get_max());
						_min_label.setText("Min: ");
						_max_label.setVisible(true);
					} else if (_seg.get_type().equals("region")) {

						_range_sel_title.setText("Region Selector - "
								+ _seg.getName());
						_min_slider.setMaximum(100);
						_min_slider.setMinimum(0);
						// System.out.println(_seg.get_min());
						_min_slider.setValue((int) (_seg.get_min() * 1.0f / 100)); // Region
																					// Segment
						// has its
						// variance in
						// _min
						
						_max_slider.setVisible(false);
						_min_label.setText("Variance: ");
						_max_label.setVisible(false);
						System.out.println("Min: " + _min_slider.getMinimum());
						System.out.println("Max: " + _min_slider.getMaximum());
						System.out.println("Val: " + _min_slider.getValue());
					}
					// _min_slider.setValue(_seg.get_min());
					// _max_slider.setValue(_seg.get_max());

				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(_seg_list);
		scrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		_range_sel_title = new JLabel("Range Selector - " + _seg.getName());

		// range_max needs to be calculated from the bits_stored value
		// in the current dicom series

		// if (_seg.get_type().equals("range")) {
		int range_max = (int) Math.pow(2, slices.getBitsStored());
		_min = (int) Math.pow(2, slices.getBitsStored() - 1);
		_max = (int) Math.pow(2, slices.getBitsStored() - 1);

		_min_label = new JLabel("Min:");
		_max_label = new JLabel("Max:");

		_min_slider = new JSlider(0, range_max, _min);
		_min_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_min = (int) source.getValue();

					if (_seg.get_type().equals("range")) {
						changeRange(_min, _max, slices, _seg);
					} else {
						_variance = _min;
						changeRange(_min, 0, slices, _seg);
					}
				}
			}
		});

		_max_slider = new JSlider(0, range_max, _max);
		_max_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_max = (int) source.getValue();
					System.out.println("_max_slider stateChanged: " + _max);
					changeRange(_min, _max, slices, _seg);
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

		c.gridheight = 2;
		c.gridx = 0;
		c.gridy = 1;
		this.add(scrollPane, c);
		c.gridheight = 1;

		c.weightx = 0.9;
		c.gridwidth = 2;
		c.gridx = 1;
		c.gridy = 0;
		this.add(_range_sel_title, c);
		c.gridwidth = 1;

		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 1;
		this.add(_min_label, c);
		c.gridx = 1;
		c.gridy = 2;
		this.add(_max_label, c);
		c.gridx = 2;
		c.gridy = 1;
		this.add(_min_slider, c);
		c.gridx = 2;
		c.gridy = 2;
		this.add(_max_slider, c);

		/** Ugly Hack following... **/
		if (_seg.get_type().equals("region")) {

			_range_sel_title.setText("Region Selector - " + _seg.getName());
			_min_slider.setMaximum(100);
			_min_slider.setMinimum(0);
			_min_slider.setValue(START_VARIANCE); // Region Segment has its
													// variance in _min
			_max_slider.setVisible(false);
			_min_label.setText("Variance");
			_max_label.setVisible(false);
		}
	}

	private void changeRange(int min, int max, ImageStack slices, Segment seg) {
		if (seg.get_type().equals("range")) {
			seg.create_range_seg(min, max, slices);
		} else {
			_seg.create_regionGrow_seq(min, Voxel.vox.x, Voxel.vox.y,
					Voxel.vox.z, slices);
		}
	}

	private void changeRange(ImageStack slices) {
		// if (_seg == null) {
		// _seg = slices.addRegionGrowSegmenation();
		// _seg.addObserver(v2d);
		// }

		System.out.println("changeRange");
		_seg.create_regionGrow_seq(_variance, Voxel.vox.x, Voxel.vox.y,
				Voxel.vox.z, slices);
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		Message m = (Message) arg1;

		if (m._type == Message.M_REGION_GROW_NEW_SEED) {
			System.out.println("new seed");
			changeRange(ImageStack.getInstance());
		}
	}
}
