package main;

import java.awt.event.*;
import javax.swing.*;

import misc.DiFileFilter;
import java.io.*;

/**
 * This class represents the main menu of YaDiV (lab version).
 * 
 * @author Karl-Ingo Friese
 */
@SuppressWarnings("serial")
public class MenuBar extends JMenuBar {
	private JMenu _menuFile;
	private JMenu _menu2d;
	private JMenu _menu3d;
	private JMenu _menuTools;
	private JMenuItem _no_entries2d;
	private JMenuItem _no_entries3d;
	private InfoWindow _info_frame;
	private ToolPane _tools;

	private Viewport2d _v2d;
	private Viewport3d _v3d;
	private MainWindow _win;

	private ToolRangeSelector toolRangeSelector;
	private ToolWindowSelector toolWindowSelector;
	private ToolRegionGrowSelector regionGrowSelector;
	private ToolChangeN changeN;
	

	// private JMenuItem item;

	private JRadioButtonMenuItem rbMenuItemT;
	private JRadioButtonMenuItem rbMenuItemS;
	private JRadioButtonMenuItem rbMenuItemF;

	/**
	 * Constructor. Needs many references, since the MenuBar has to trigger a
	 * lot of functions.
	 * 
	 * @param slices
	 *            the global image stack reference
	 * @param v2d
	 *            the Viewport2d reference
	 * @param v3d
	 *            the Viewport3d reference
	 * @param tools
	 *            the ToolPane reference
	 */
	public MenuBar(Viewport2d v2d, Viewport3d v3d, ToolPane tools) {
		JMenuItem item;

		_v2d = v2d;
		_v3d = v3d;
		_tools = tools;

		_menuFile = new JMenu("File");
		_menu2d = new JMenu("2D View");
		_menu3d = new JMenu("3D View");
		_menuTools = new JMenu("Tools");
		_info_frame = null;

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("Load"), 'L');
		item.addActionListener(loadListener);
		_menuFile.add(item);

		item = new JMenuItem(new String("Save"));
		item.addActionListener(saveListener);
		item.setEnabled(false);
		_menuFile.add(item);

		item = new JMenuItem(new String("Save as ..."));
		item.addActionListener(saveAsListener);
		item.setEnabled(false);
		_menuFile.add(item);

		item = new JMenuItem(new String("Quit"), 'Q');
		item.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				System.exit(0);
			}
		});
		_menuFile.add(item);

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("DICOM Info"));
		item.addActionListener(showInfoListener);
		_menu2d.add(item);

		_menu2d.addSeparator();
		item = new JCheckBoxMenuItem(new String("Show original data"), true);
		item.addActionListener(toggleBGListener2d);
		_menu2d.add(item);

		ButtonGroup group = new ButtonGroup();

		rbMenuItemT = new JRadioButtonMenuItem("Transversal");
		rbMenuItemT.addActionListener(setViewModeListener);
		group.add(rbMenuItemT);
		_menu2d.add(rbMenuItemT);
		rbMenuItemT.setSelected(true);

		rbMenuItemS = new JRadioButtonMenuItem("Sagittal");
		rbMenuItemS.addActionListener(setViewModeListener);
		group.add(rbMenuItemS);
		_menu2d.add(rbMenuItemS);

		rbMenuItemF = new JRadioButtonMenuItem("Frontal");
		rbMenuItemF.addActionListener(setViewModeListener);
		group.add(rbMenuItemF);
		_menu2d.add(rbMenuItemF);

		_menu2d.addSeparator();

		_no_entries2d = new JMenuItem(new String("no segmentations yet"));
		_no_entries2d.setEnabled(false);
		_menu2d.add(_no_entries2d);

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("3d Item 1"));
		// item.addActionListener(...);
		item = new JCheckBoxMenuItem(new String("Show original Data"), false);
		item.addActionListener(toggleBGListener3d);
		_menu3d.add(item);

		_menu3d.addSeparator();

		_no_entries3d = new JMenuItem(new String("no segmentations yet"));
		_no_entries3d.setEnabled(false);
		_menu3d.add(_no_entries3d);

		// -------------------------------------------------------------------------------------

		item = new JMenuItem(new String("Neue Range Segmentierung"));
		item.addActionListener(newSegmentListener);
		_menuTools.add(item);

		item = new JMenuItem(new String("Neue Region Grow Segmentierung"));
		item.addActionListener(regionGrowListener);
		_menuTools.add(item);

		item = new JMenuItem(new String("Zeige Segmentierungen"));
		// item.setEnabled(false);
		item.addActionListener(showSegmentListener);
		_menuTools.add(item);

		item = new JMenuItem(new String("Ändere Skalierung"));
		item.addActionListener(newWindowListener);
		_menuTools.add(item);
		
		item = new JMenuItem(new String("Ändere Raumgitter"));
		item.addActionListener(changeNListener);
		_menuTools.add(item);

		// -------------------------------------------------------------------------------------

		add(_menuFile);
		add(_menu2d);
		add(_menu3d);
		add(_menuTools);
	}

	/**
	 * This function is called when someone chooses to load DICOM series.
	 */
	ActionListener loadListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			openDialog(false);
		}
	};

	/**
	 * This function is called when someone chooses to save the current project.
	 */
	ActionListener saveListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			// saveFile(...);
		}
	};

	/**
	 * This function is called when someone chooses to save the current project
	 * under a new name.
	 */
	ActionListener saveAsListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			openDialog(true);
		}
	};

	/**
	 * Opens a file chooser dialog with a DICOM file filter and directory.
	 * 
	 * @param save
	 *            true if the dialog should be a save file dialog, false if not
	 */
	private void openDialog(boolean save) {
		int returnVal;
		File file;
		JFileChooser chooser;
		String default_dir = new String("");

		if (new File(default_dir).exists()) {
			chooser = new JFileChooser(default_dir);
		} else {
			chooser = new JFileChooser();
		}

		DiFileFilter filter = new DiFileFilter();
		filter.setDescription("Dicom Image Files");
		chooser.setFileFilter(filter);

		if (save) {
			returnVal = chooser.showSaveDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				if (!file.canWrite()) {
					System.out.println("could not read!");
					return;
				}
				// saveFile(file);
			}
		} else {
			returnVal = chooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = chooser.getSelectedFile();
				if (!file.canRead()) {
					System.out.println("could not read!");
					return;
				}
				System.out.println(file.getParent());
				LabMed.get_is().initFromDirectory(file.getParent());
			}
		}

	}

	/**
	 * This function is called when someone wants to see the dicome file info
	 * window.
	 */
	ActionListener showInfoListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			if (_v2d.currentFile() == null) {
				JOptionPane.showMessageDialog(null, "Fehler: Keine DICOM Datei geÃ¶ffnet", "Inane error", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (_info_frame == null) {
				_info_frame = new InfoWindow();
				LabMed.get_is().addObserver(_info_frame);
			}

			if (!_info_frame.isVisible()) {
				_info_frame.setVisible(true);
			}

			_info_frame.showInfo(_v2d.currentFile());

		}
	};

	/**
	 * Actionlistener for changing the 2d viewmode.
	 */
	ActionListener setViewModeListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			int mode = 0;
			if (name.equals("Transversal")) {
				mode = 0;
			} else if (name.equals("Sagittal")) {
				mode = 1;
			} else if (name.equals("Frontal")) {
				mode = 2;
			}
			if (!_v2d.setViewMode(mode)) {
				rbMenuItemF.setSelected(false);
				rbMenuItemS.setSelected(false);
				rbMenuItemT.setSelected(true);
			}

		}
	};

	/**
	 * ActionListener for toggling the 2d background image.
	 */
	ActionListener toggleBGListener2d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			_v2d.toggleBG();
		}
	};

	/**
	 * ActionListener for toggling the 3d background image.
	 */
	ActionListener toggleBGListener3d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			_v3d.toggleBG();
		}
	};

	/**
	 * ActionListener for toggling a segmentation in the 2d viewport.
	 */
	ActionListener toggleSegListener2d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			_v2d.toggleSeg(LabMed.get_is().getSegment(name));
		}
	};

	/**
	 * ActionListener for toggling a segmentation in the 3d viewport.
	 */
	ActionListener toggleSegListener3d = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			String name = event.getActionCommand();
			_v3d.toggleSeg(LabMed.get_is().getSegment(name));
		}
	};

	ActionListener showSegmentListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages() == 0) {
				JOptionPane.showMessageDialog(_win, "Segmentierung ohne geÃ¶ffneten DICOM Datensatz nicht mÃ¶glich.", "Inane error", JOptionPane.ERROR_MESSAGE);
			} else {
				if (is.getSegmentNumber() > 0)
					_tools.showTool(toolRangeSelector);
			}

		}

	};

	/**
	 * ActionListener for adding a new segmentation to the global image stack.
	 */
	ActionListener newSegmentListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages() == 0) {
				JOptionPane.showMessageDialog(_win, "Segmentierung ohne geÃ¶ffneten DICOM Datensatz nicht möglich.", "Inane error", JOptionPane.ERROR_MESSAGE);
			} else if (is.getSegmentNumber() == 3) {
				JOptionPane.showMessageDialog(_win, "In der Laborversion werden nicht mehr als drei Segmentierungen benötigt.", "Inane error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				String name = JOptionPane.showInputDialog(_win, "Name der Segmentierung");
				if (name != null) {
					_no_entries2d.setVisible(false);
					_no_entries3d.setVisible(false);
					Segment seg = is.addSegment(name);
					seg.addObserver(_v2d);
					seg.addObserver(_v3d);
					_v2d.toggleSeg(seg);
					_v3d.toggleSeg(seg);
					JMenuItem item = new JCheckBoxMenuItem(name, true);
					item.addActionListener(toggleSegListener2d);
					_menu2d.add(item);
					item = new JCheckBoxMenuItem(name, false);
					item.addActionListener(toggleSegListener3d);
					_menu3d.add(item);

					// if(toolRangeSelector == null){
					toolRangeSelector = new ToolRangeSelector(seg);
					// }else{
					// toolRangeSelector
					// }

					_tools.showTool(toolRangeSelector);
				}
			}
		}
	};
	
	/**
	 * ActionListener for adding a new segmentation to the global image stack.
	 */
	ActionListener regionGrowListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages() == 0) {
				JOptionPane.showMessageDialog(_win, "Segmentierung ohne geÃ¶ffneten DICOM Datensatz nicht möglich.", "Inane error", JOptionPane.ERROR_MESSAGE);
			} else if (is.getSegmentNumber() == 3) {
				JOptionPane.showMessageDialog(_win, "In der Laborversion werden nicht mehr als drei Segmentierungen benötigt.", "Inane error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				String name = JOptionPane.showInputDialog(_win, "Name der Segmentierung");
				if (name != null) {
					_no_entries2d.setVisible(false);
					_no_entries3d.setVisible(false);
					Segment seg = is.addRegionGrowSegmenation(name);
					seg.addObserver(_v2d);
					_v2d.toggleSeg(seg);
					JMenuItem item = new JCheckBoxMenuItem(name, true);
					item.addActionListener(toggleSegListener2d);
					_menu2d.add(item);
					item = new JCheckBoxMenuItem(name, false);
					item.addActionListener(toggleSegListener3d);
					_menu3d.add(item);

					// if(toolRangeSelector == null){
					toolRangeSelector = new ToolRangeSelector(seg);
					// }else{
					// toolRangeSelector
					// }

					_tools.showTool(toolRangeSelector);
				}
			}
		}
	};
//	ActionListener regionGrowListener = new ActionListener() {
//		public void actionPerformed(ActionEvent event) {
//			ImageStack is = LabMed.get_is();
//			if (is.getNumberOfImages() == 0) {
//				JOptionPane.showMessageDialog(_win, "Änderung der Ansicht ohne geöffneten DICOM Datensatz nicht möglich.", "Inane error",
//						JOptionPane.ERROR_MESSAGE);
//			} else {
//
//				SelectWindow sel_win = is.addSelectWindow("RegionGrow");
//
//				if (regionGrowSelector == null) {
//					sel_win.addObserver(_v2d);
//					regionGrowSelector = new ToolRegionGrowSelector(sel_win, _v2d);
//				}
//				_tools.showTool(regionGrowSelector);
//			}
//		}
//	};
	
	/**
	 * ActionListener for toggling a segmentation in the 3d viewport.
	 */
	ActionListener newWindowListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages() == 0) {
				JOptionPane.showMessageDialog(_win, "Änderung der Ansicht ohne geöffneten DICOM Datensatz nicht möglich.", "Inane error",
						JOptionPane.ERROR_MESSAGE);
			} else {
				SelectWindow sel_win = is.addSelectWindow("Window_Selection");

				if (toolWindowSelector == null) {
					sel_win.addObserver(_v2d);
					toolWindowSelector = new ToolWindowSelector(sel_win);
				}
				_tools.showTool(toolWindowSelector);
			}
		}
	};
	
	ActionListener changeNListener = new ActionListener() {
		public void actionPerformed(ActionEvent event) {
			ImageStack is = LabMed.get_is();
			if (is.getNumberOfImages() == 0) {
				JOptionPane.showMessageDialog(_win, "Änderung der Ansicht ohne geöffneten DICOM Datensatz nicht möglich.", "Inane error",
						JOptionPane.ERROR_MESSAGE);
			} else {
//				SelectWindow sel_win = is.addSelectWindow("Window_Selection");

				if (changeN == null) {
//					sel_win.addObserver(_v2d);
					changeN = new ToolChangeN(_v3d);
				}
				_tools.showTool(changeN);
			}
		}
	};

	
}
