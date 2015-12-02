package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

@SuppressWarnings("serial")
public class ToolRegionGrowSelector extends JPanel implements Observer {

	private int _variance;
	private Segment _seg;
	private JSlider _variance_slider;
	private JLabel _region_grow_title, _min_label;
	private final ImageStack slices;
	private Viewport2d v2d;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices
	 *            the global image stack
	 * @param seg
	 *            the segmentation to be modified
	 */
	public ToolRegionGrowSelector(SelectWindow sel_win, Viewport2d _v2d) {
		slices = ImageStack.getInstance();
		v2d = _v2d;
		Voxel.vox.addObserver(this);

		// _seg_list = new JList<String>(slices.getSegNames());
		// _seg_list.setSelectedIndex(slices.getSegNames().indexOf(seg.getName()));
		// _seg_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		//
		// _seg_list.addListSelectionListener(new ListSelectionListener() {
		// public void valueChanged(ListSelectionEvent e) {
		// int seg_index = _seg_list.getSelectedIndex();
		// String name = (String) (slices.getSegNames()
		// .getElementAt(seg_index));
		// if (!_seg.getName().equals(name)) {
		// _seg = slices.getSegment(name);
		// _range_sel_title.setText("Range Selector - "
		// + _seg.getName());
		//
		// _min_slider.setValue(_seg.get_min());
		// _max_slider.setValue(_seg.get_max());
		//
		//
		// }
		// }
		// });

		// JScrollPane scrollPane = new JScrollPane(_seg_list);
		// scrollPane
		// .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		// scrollPane
		// .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

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
					changeRange(_variance, _seg);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2); // top,left,bottom,right

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

	private void changeRange(int variance, Segment seg) {
		if (seg == null) {
//			_seg = slices.addRegionGrowSegmenation();
			_seg = slices.addSegment("regionGrow");
			_seg.addObserver(v2d);
		}

		System.out.println("changeRange");
		_seg.create_regionGrow_seq(variance,(int)Voxel.vox.x, (int)Voxel.vox.y, (int)Voxel.vox.z, slices);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (_seg == null) {
//			_seg = slices.addRegionGrowSegmenation();
			_seg = slices.addSegment("regionGrow");
			_seg.addObserver(v2d);
		}
		System.out.println("Hello got an update!");
		_seg.create_regionGrow_seq(_variance, (int)Voxel.vox.x,
				(int)Voxel.vox.y,(int)Voxel.vox.z, slices);
	}

}
