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
public class ToolCubeSizeSelector extends JPanel implements Observer {

	private int _size;
//	private Segment _seg;
	private JSlider _case_slider;
	private JLabel _region_grow_title, _min_label;
//	private final ImageStack slices;
	private Viewport3d v3d;

	/**
	 * Default Constructor. Creates the GUI element and connects it to a
	 * segmentation.
	 * 
	 * @param slices
	 *            the global image stack
	 * @param seg
	 *            the segmentation to be modified
	 */
	public ToolCubeSizeSelector(Viewport3d _v3d) {
		v3d = _v3d;
		Voxel.vox.addObserver(this);


		_region_grow_title = new JLabel("Cube Selector");

		int range_max = 5;
		_size = 2;
		

		_min_label = new JLabel("Case");

		_case_slider = new JSlider(1, range_max, _size);
		_case_slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_size = (int) source.getValue();
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
		this.add(_case_slider, c);

	}

	private void changeRange() {
		v3d.changeMarchingCubeSize(_size);
//		System.out.println("changeRange");

	}

	@Override
	public void update(Observable o, Object arg) {
		Message m = (Message) arg;

		if (m._type == Message.M_CUBE_SIZE_CHANGED) {
			changeRange();
		}
	}

}
