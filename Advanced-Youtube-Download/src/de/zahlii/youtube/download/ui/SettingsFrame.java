package de.zahlii.youtube.download.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.zahlii.youtube.download.basic.ConfigManager;
import de.zahlii.youtube.download.basic.Media;
import de.zahlii.youtube.download.basic.ConfigManager.ConfigKey;

import javax.swing.JSpinner;
import javax.swing.SpinnerListModel;
import javax.swing.JCheckBox;

public class SettingsFrame extends JFrame {

	private JPanel contentPane;
	private JTextField formatPattern;
	private JSpinner spinnerAudioBitrate;
	private JLabel  lblDestination;
	private JCheckBox chkKeepVideo;
	private JCheckBox chkImprove;

	/**
	 * Create the frame.
	 */
	public SettingsFrame() {

			super("Edit Preferences");
			setIconImage(Media.ICON_DOWNLOAD.getImage());


			GridBagLayout gbc_main = new GridBagLayout();
			gbc_main.columnWidths = new int[] {
					10, 100, 200, 200,100, 10
			};
			gbc_main.rowHeights = new int[] {
					10, 30, 30, 30, 30,30,30, 10
			};

			getContentPane().setLayout(gbc_main);
			setMinimumSize(new Dimension(sum(gbc_main.columnWidths), sum(gbc_main.rowHeights)));
			setResizable(false);
			setDefaultCloseOperation(HIDE_ON_CLOSE);
			setLocationRelativeTo(null);

			JLabel lblNewLabel = new JLabel("Audio Bitrate");
			GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
			gbc_lblNewLabel.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel.gridx = 1;
			gbc_lblNewLabel.gridy = 1;
			getContentPane().add(lblNewLabel, gbc_lblNewLabel);
			
			spinnerAudioBitrate = new JSpinner();	
			spinnerAudioBitrate.setModel(new SpinnerListModel(new String[] {"128", "192", "320"}));
			

			spinnerAudioBitrate.setEditor(new JSpinner.DefaultEditor(spinnerAudioBitrate));
			
			GridBagConstraints gbc_spinnerAudioBitrate = new GridBagConstraints();
			gbc_spinnerAudioBitrate.fill = GridBagConstraints.BOTH;
			gbc_spinnerAudioBitrate.gridwidth = 2;
			gbc_spinnerAudioBitrate.insets = new Insets(0, 0, 5, 5);
			gbc_spinnerAudioBitrate.gridx = 2;
			gbc_spinnerAudioBitrate.gridy = 1;
			getContentPane().add(spinnerAudioBitrate, gbc_spinnerAudioBitrate);

			JLabel lblNewLabel_1 = new JLabel("Format Pattern");
			GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
			gbc_lblNewLabel_1.anchor = GridBagConstraints.ABOVE_BASELINE_TRAILING;
			gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_1.gridx = 1;
			gbc_lblNewLabel_1.gridy = 2;
			getContentPane().add(lblNewLabel_1, gbc_lblNewLabel_1);

			formatPattern = new JTextField();
			GridBagConstraints gbc_title = new GridBagConstraints();
			gbc_title.gridwidth = 2;
			gbc_title.insets = new Insets(0, 0, 5, 5);
			gbc_title.fill = GridBagConstraints.HORIZONTAL;
			gbc_title.gridx = 2;
			gbc_title.gridy = 2;
			getContentPane().add(formatPattern, gbc_title);
			formatPattern.setColumns(10);

			JLabel lblNewLabel_2 = new JLabel("Destination");
			GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
			gbc_lblNewLabel_2.anchor = GridBagConstraints.EAST;
			gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
			gbc_lblNewLabel_2.gridx = 1;
			gbc_lblNewLabel_2.gridy = 3;
			getContentPane().add(lblNewLabel_2, gbc_lblNewLabel_2);

			JButton btnNewButton = new JButton("Save");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					saveConfig();
				}
			});
			
			lblDestination = new JLabel("");
			GridBagConstraints gbc_lblDestination = new GridBagConstraints();
			gbc_lblDestination.fill = GridBagConstraints.BOTH;
			gbc_lblDestination.gridwidth = 2;
			gbc_lblDestination.anchor = GridBagConstraints.NORTHWEST;
			gbc_lblDestination.insets = new Insets(0, 0, 5, 5);
			gbc_lblDestination.gridx = 2;
			gbc_lblDestination.gridy = 3;
			getContentPane().add(lblDestination, gbc_lblDestination);
			
			JButton btnChange = new JButton("Change");
			btnChange.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent arg0) {
					final JFileChooser fc = new JFileChooser();
					fc.setDialogTitle("Chose Destination Folder");
					fc.setApproveButtonText("Set as Destination");
					String path = lblDestination.getText();
					fc.setCurrentDirectory(new File(path));
					fc.setMultiSelectionEnabled(false);
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

					int res = fc.showOpenDialog(null);
					if(res == JFileChooser.APPROVE_OPTION) {
						File f = fc.getSelectedFile();
						lblDestination.setText(f.getAbsolutePath());
					}
				}
				
			});
			btnChange.setIcon(Media.ICON_MUSIC);
			GridBagConstraints gbc_btnChange = new GridBagConstraints();
			gbc_btnChange.insets = new Insets(0, 0, 5, 5);
			gbc_btnChange.gridx = 4;
			gbc_btnChange.gridy = 3;
			getContentPane().add(btnChange, gbc_btnChange);
			
			JLabel lblKeepVideo = new JLabel("Keep Video");
			GridBagConstraints gbc_lblKeepVideo = new GridBagConstraints();
			gbc_lblKeepVideo.anchor = GridBagConstraints.EAST;
			gbc_lblKeepVideo.fill = GridBagConstraints.VERTICAL;
			gbc_lblKeepVideo.insets = new Insets(0, 0, 5, 5);
			gbc_lblKeepVideo.gridx = 1;
			gbc_lblKeepVideo.gridy = 4;
			getContentPane().add(lblKeepVideo, gbc_lblKeepVideo);
			
			chkKeepVideo = new JCheckBox("");
			GridBagConstraints gbc_chkKeepVideo = new GridBagConstraints();
			gbc_chkKeepVideo.anchor = GridBagConstraints.WEST;
			gbc_chkKeepVideo.insets = new Insets(0, 0, 5, 5);
			gbc_chkKeepVideo.gridx = 2;
			gbc_chkKeepVideo.gridy = 4;
			getContentPane().add(chkKeepVideo, gbc_chkKeepVideo);
			
			JLabel lblConvertAndImprove = new JLabel("Convert and Improve");
			GridBagConstraints gbc_lblConvertAndImprove = new GridBagConstraints();
			gbc_lblConvertAndImprove.anchor = GridBagConstraints.WEST;
			gbc_lblConvertAndImprove.fill = GridBagConstraints.HORIZONTAL;
			gbc_lblConvertAndImprove.insets = new Insets(0, 0, 5, 5);
			gbc_lblConvertAndImprove.gridx = 1;
			gbc_lblConvertAndImprove.gridy = 5;
			getContentPane().add(lblConvertAndImprove, gbc_lblConvertAndImprove);
			
			chkImprove = new JCheckBox("");
			GridBagConstraints gbc_checkBox = new GridBagConstraints();
			gbc_checkBox.anchor = GridBagConstraints.WEST;
			gbc_checkBox.insets = new Insets(0, 0, 5, 5);
			gbc_checkBox.gridx = 2;
			gbc_checkBox.gridy = 5;
			getContentPane().add(chkImprove, gbc_checkBox);
			btnNewButton.setIcon(Media.ICON_OK);
			GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.fill = GridBagConstraints.BOTH;
			gbc_btnNewButton.anchor = GridBagConstraints.EAST;
			gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
			gbc_btnNewButton.gridx = 2;
			gbc_btnNewButton.gridy = 6;
			getContentPane().add(btnNewButton, gbc_btnNewButton);

			JButton btnNewButton_1 = new JButton("Cancel");
			btnNewButton_1.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					setVisible(false);
				}
			});
			btnNewButton_1.setIcon(Media.ICON_CANCEL);
			GridBagConstraints gbc_btnNewButton_1 = new GridBagConstraints();
			gbc_btnNewButton_1.fill = GridBagConstraints.BOTH;
			gbc_btnNewButton_1.insets = new Insets(0, 0, 0, 5);
			gbc_btnNewButton_1.anchor = GridBagConstraints.EAST;
			gbc_btnNewButton_1.gridx = 3;
			gbc_btnNewButton_1.gridy = 6;
			getContentPane().add(btnNewButton_1, gbc_btnNewButton_1);
			
			//reloadConfig();
		}

		protected void saveConfig() {
			ConfigManager c = ConfigManager.getInstance();
			c.setConfig(ConfigKey.AUDIO_BITRATE, spinnerAudioBitrate.getValue().toString());
			c.setConfig(ConfigKey.FILENAME_CONVENTION, formatPattern.getText());
			c.setConfig(ConfigKey.DIR_TARGET,lblDestination.getText());
			c.setConfig(ConfigKey.IS_DEFAULT,"false");
			c.setConfig(ConfigKey.KEEP_VIDEO,chkKeepVideo.isSelected()+"");
			c.setConfig(ConfigKey.IMPROVE_CONVERT,chkImprove.isSelected()+"");
			setVisible(false);
		}

		private int sum(int[] array) {
			int sum = 0;
			for (int j : array)
				sum += j;
			return sum + 10 * (array.length);
		}
		
		public void reloadConfig() {
			ConfigManager c = ConfigManager.getInstance();
			
			spinnerAudioBitrate.setValue(c.getConfig(ConfigKey.AUDIO_BITRATE, "320"));
			formatPattern.setText(c.getConfig(ConfigKey.FILENAME_CONVENTION, "%artist - %title"));
			File o = new File("").getAbsoluteFile();
			while(o.getParentFile() != null) {
				o = o.getParentFile();
			}
			lblDestination.setText(c.getConfig(ConfigKey.DIR_TARGET,o.getAbsolutePath()));
			chkKeepVideo.setSelected(Boolean.valueOf(c.getConfig(ConfigKey.KEEP_VIDEO, "false")));
			chkImprove.setSelected(Boolean.valueOf(c.getConfig(ConfigKey.IMPROVE_CONVERT, "true")));
		}

}
