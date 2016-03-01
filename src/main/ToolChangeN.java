package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ToolChangeN extends JPanel{

	private int _n = 5;
	private JSlider _n_slider;
	private JLabel _tool_title, _n_label;
	private Viewport3d _v3d;
	private JLabel actualValue;


	public ToolChangeN(Viewport3d v3d) {
		_v3d = v3d;

		_tool_title = new JLabel("Edit Raumgitter");
		actualValue = new JLabel("Value: " + _n);

		_n_label = new JLabel("N");

		_n_slider = new JSlider(0, 20, _n);
		_n_slider.addChangeListener(new ChangeListener() {
			
			public void stateChanged(ChangeEvent e) {
				JSlider source = (JSlider) e.getSource();
				if (source.getValueIsAdjusting()) {
					_n = (int) source.getValue();
					
					_v3d.changeN(_n);
					actualValue.setText("Value: " + _n);
				}
			}
		});

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.weighty = 0.3;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(2, 2, 2, 2); // top,left,bottom,right

		c.weightx = 0;
		c.gridwidth = 1;
		c.gridx = 1;
		c.gridy = 0;
		this.add(_tool_title, c);
		c.weightx = 1;
		c.gridx = 2;
		this.add(actualValue);
		
		c.gridwidth = 1;

		c.weightx = 0;
		c.gridx = 1;
		c.gridy = 1;
		this.add(_n_label, c);
		c.weightx = 1;
		c.gridx = 2;
		c.gridy = 1;
		this.add(_n_slider, c);

	
	}

}
