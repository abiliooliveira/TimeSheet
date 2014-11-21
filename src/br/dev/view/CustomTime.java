package br.dev.view;

import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import br.dev.controller.business.Util;
import br.dev.model.JTextFieldLimit;
import br.dev.model.KeyListenerLimit;

public class CustomTime {

	private Date customDate;
	private boolean isDone;
	JDialog dialog;
	private JTextField textMinute;
	private JTextField textSecond;
	private JTextField textHour;

	/**
	 * Create the application.
	 */

	public Date getCustomDate() {
		return customDate;
	}

	public CustomTime() {
	}

	/**
	 * Initialize the contents of the frame.
	 *
	 * @wbp.parser.entryPoint
	 */
	@SuppressWarnings("serial")
	public boolean showDialog() {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");

		Date date = new Date();
		Date dateZero = null;

		try {
			dateZero = sdf.parse("00:00:00");
		} catch (ParseException e1) {
			e1.printStackTrace();
		}

		long time = date.getTime() - dateZero.getTime();

		dialog = new JDialog(null, JDialog.ModalityType.APPLICATION_MODAL);
		dialog.setTitle("Custom Time");
		dialog.setIconImage(Toolkit.getDefaultToolkit().getImage(CustomTime.class.getResource("/resources/calendar-16.png")));
		dialog.setResizable(false);
		dialog.setBounds(100, 100, 326, 170);
		dialog.getContentPane().setLayout(null);
		dialog.setLocationRelativeTo(null);

		final KeyStroke escapeStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
		final String dispatchWindowClosingActionMapKey = "com.spodding.tackline.dispatch:WINDOW_CLOSING";
		dialog.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeStroke, dispatchWindowClosingActionMapKey);

		Action dispatchClosing = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dialog.dispatchEvent(new WindowEvent(dialog, WindowEvent.WINDOW_CLOSING));
			}
		};

		dialog.getRootPane().getActionMap().put(dispatchWindowClosingActionMapKey, dispatchClosing);

		textSecond = new JTextField();
		textSecond.setColumns(2);
		textSecond.setBounds(190, 52, 34, 20);
		textSecond.setDocument(new JTextFieldLimit(2));
		// textSecond.setText(String.valueOf(Util.getSeconds(time)));
		textSecond.setText("00");
		textSecond.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textSecond.selectAll();
					}
				});
			}
		});
		dialog.getContentPane().add(textSecond);

		textMinute = new JTextField();
		textMinute.setBounds(105, 52, 34, 20);
		textMinute.setDocument(new JTextFieldLimit(2));
		textMinute.setText(String.valueOf(Util.getMinuts(time)));

		dialog.getContentPane().add(textMinute);

		textMinute.addKeyListener(new KeyListenerLimit(2, textSecond));
		textMinute.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textMinute.selectAll();
					}
				});
			}
		});
		textMinute.setColumns(2);

		textHour = new JTextField();
		textHour.setColumns(2);
		textHour.setBounds(23, 52, 34, 20);
		textHour.setDocument(new JTextFieldLimit(2));
		textHour.setText(String.valueOf(Util.getHours(time)));
		textHour.addKeyListener(new KeyListenerLimit(2, textMinute));
		// text will be selected when field gains focus
		textHour.addFocusListener(new java.awt.event.FocusAdapter() {
			@Override
			public void focusGained(FocusEvent evt) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						textHour.selectAll();
					}
				});
			}
		});
		dialog.getContentPane().add(textHour);

		JButton btnOk = new JButton("Ok");
		btnOk.setIcon(new ImageIcon(CustomTime.class.getResource("/resources/checkmark-24-16.png")));
		dialog.getRootPane().setDefaultButton(btnOk);

		btnOk.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				Calendar cal = Calendar.getInstance();

				cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(textHour.getText()));
				cal.set(Calendar.MINUTE, Integer.parseInt(textMinute.getText()));
				cal.set(Calendar.SECOND, Integer.parseInt(textSecond.getText()));

				customDate = cal.getTime();

				if (customDate.getTime() > new Date().getTime()) {
					JOptionPane.showMessageDialog(null, "The final time can not be longer than the initial time!", "Wow",
							JOptionPane.WARNING_MESSAGE);
				} else {
					dialog.setVisible(false);
					isDone = true;
				}
			}
		});
		btnOk.setBounds(115, 107, 89, 23);
		dialog.getContentPane().add(btnOk);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setIcon(new ImageIcon(CustomTime.class.getResource("/resources/cancel-16.png")));
		btnCancel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dialog.setVisible(false);
			}
		});
		btnCancel.setBounds(210, 107, 89, 23);
		dialog.getContentPane().add(btnCancel);

		JLabel lblHour = new JLabel("Hour");
		lblHour.setBounds(67, 55, 46, 14);
		dialog.getContentPane().add(lblHour);

		JLabel lblMinute = new JLabel("Minute");
		lblMinute.setBounds(149, 55, 46, 14);
		dialog.getContentPane().add(lblMinute);

		JLabel lblSecond = new JLabel("Second");
		lblSecond.setBounds(231, 55, 46, 14);
		dialog.getContentPane().add(lblSecond);

		JLabel lblSetCustonTime = new JLabel("Set custom Time");
		lblSetCustonTime.setFont(new Font("Tahoma", Font.BOLD, 11));
		lblSetCustonTime.setHorizontalAlignment(SwingConstants.CENTER);
		lblSetCustonTime.setBounds(23, 11, 254, 14);
		dialog.getContentPane().add(lblSetCustonTime);

		JLabel lblHoursClock = new JLabel("Hour of the day (24h)");
		lblHoursClock.setBounds(23, 29, 116, 14);
		dialog.getContentPane().add(lblHoursClock);

		JSeparator separator = new JSeparator();
		separator.setBounds(23, 90, 273, 2);
		dialog.getContentPane().add(separator);
		dialog.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
			}
		});

		dialog.setVisible(true);
		return isDone;
	}
}
