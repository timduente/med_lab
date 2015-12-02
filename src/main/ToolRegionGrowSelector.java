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


		_region_grow_title = new JLabel("Region Grow Segmentation");

		int range_max = 100;
		_variance = 10;

		_min_label = new JLabel("Varianz");

		_variance_slider = new JSlider(0, range_max, _variance);
		_variance_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_variance = (int) source.getValue();
					changeRange();
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

	private void changeRange() {
		if (_seg == null) {
			_seg = slices.addRegionGrowSegmenation();
			_seg.addObserver(v2d);
		}

		System.out.println("changeRange");
		_seg.create_regionGrow_seq(_variance, Voxel.vox.x, Voxel.vox.y,
				Voxel.vox.z, slices);
	}

	@Override
	public void update(Observable o, Object arg) {
		Message m = (Message) arg;

		if (m._type == Message.M_REGION_GROW_NEW_SEED) {
			changeRange();
		}
	}

}
