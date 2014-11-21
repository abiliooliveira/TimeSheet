package br.dev.controller;

import java.text.SimpleDateFormat;
import java.util.Date;

import br.dev.controller.business.DataManager;
import br.dev.controller.business.OperationCore;
import br.dev.controller.business.Util;
import br.dev.model.listener.ButtonListener;
import br.dev.view.CustomTime;
import br.dev.view.NewTimeSheet;
import br.dev.view.Prototype;

import com.sun.jmx.snmp.tasks.Task;

public class TMS implements ButtonListener {
	private Prototype prot;
	private OperationCore func;
	private DataManager dataManager;

	private static int wordPerSecond = 13;
	private static int updateSeconds = 1;
	private static int clockUpdateInterval = 5;

	private Task taskIdleTime;
	private Task taskSimpleTime;

	private Thread updateThreadSimpleTime = null;
	private Thread updateThreadIdleTime = null;

	public static void main(String[] args) {
		new TMS();
	}

	public TMS() {
		prot = new Prototype();
		prot.addListner(this);

		// TimeIdle
		taskIdleTime = new Task() {
			boolean isDone;

			@Override
			public void run() {
				isDone = false;
				while (!isDone) {
					try {
						Thread.sleep(updateSeconds * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					long value = func.updateTimeIdle(true, true, updateSeconds);
					prot.textIdle.setText(Util.printTime(value, "%sh %sm %ss"));

					if (value > func.getPredPause() && value < func.getPredPause() + 1000) {
						prot.showMessage("Hey, Mandatory brake time is over, Back to Work!!", "Time Over!", "/resources/prisoner-32.png");
					}
				}
			}

			@Override
			public void cancel() {
				isDone = true;
			}

		};

		taskSimpleTime = new Task() {
			boolean isDone;

			@Override
			public void run() {
				isDone = false;
				while (!isDone) {
					try {
						Thread.sleep(updateSeconds * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					long timeElapsed = func.updateTimeElapsed(true, updateSeconds);
					prot.textElapsed.setText(Util.printTime(timeElapsed, "%sh %sm %ss"));

					long timeRemain = func.updateTimeRemain(true, updateSeconds);
					prot.textRemain.setText(Util.printTime(timeRemain, "%sh %sm %ss"));

					if (timeRemain < 0 && timeRemain > -1000) {
						prot.showMessage("Hey, work time is Over, Enjoy your Freedom!!", "Time Over!", "/resources/running64.png");
					}
				}
			}

			@Override
			public void cancel() {
				isDone = true;
			}
		};

		// Clock
		new Thread(new Runnable() {
			@Override
			public void run() {
				SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
				while (true) {
					try {
						prot.labelHour.setText(format.format(new Date()));
						Thread.sleep(clockUpdateInterval * 1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}

			}
		}).start();

		// Text Panel
		new Thread(new Runnable() {
			@Override
			public void run() {
				do {
					try {
						Thread.sleep(1000 * 5);
						String[] text = Util.getText();
						for (int i = 0; i < text.length; i++) {
							String[] t = text[i].split("#");
							prot.lblhid.setText(t[0]);

							if (t.length == 1) {
								Thread.sleep(1000 * text[i].split("").length / wordPerSecond);
							} else {
								Thread.sleep(1000 * Integer.parseInt(t[1]));
							}
						}

						prot.lblhid.setText("");

					} catch (InterruptedException e) {
						prot.lblhid.setText("ERROR!");
					}

				} while (true);
			}
		}).start();

		dataManager = new DataManager();
		if (dataManager.hasDataInFile()) {
			if (prot.showMessageChoice("Restore previous data from DB ?", "Restore", "/resources/database-32.png") == 0) {
				dataManager.readFileInfo();
				updateTMS();
			}
		}
	}

	private void onCheckoutThreadManager() {
		try {
			if (updateThreadSimpleTime != null) {
				taskSimpleTime.cancel();
				updateThreadSimpleTime.join();
			}

			updateThreadIdleTime = new Thread(taskIdleTime);
			updateThreadIdleTime.start();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void onChekinThreadManager() {
		try {
			if (updateThreadIdleTime != null) {
				taskIdleTime.cancel();
				updateThreadIdleTime.join();
			}

			updateThreadSimpleTime = new Thread(taskSimpleTime);
			updateThreadSimpleTime.start();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private void onNewTimeSheetThreadManager() {
		if (updateThreadSimpleTime != null)
			try {
				taskSimpleTime.cancel();
				updateThreadSimpleTime.join();
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}
	}

	@Override
	public boolean onCheckout() {
		return onCheckout(false);
	}

	@Override
	public boolean onCheckout(boolean preloaded) {
		if (preloaded) {
			long startTime = func.getTempTimePack().getStart();
			prot.textStartTime.setText(func.formatDate(null, startTime));

			long predictedTime = func.getTimeSheet().getTimePredicted();
			prot.textPredicted.setText(func.formatDate(null, predictedTime));
		} else {
			CustomTime custon = new CustomTime();
			if (!custon.showDialog()) {
				return false;
			}

			Date customTime = custon.getCustomDate();

			try {
				func.setFinalTime(customTime);
			} catch (Exception e1) {
				prot.showMessage("The final time can not be longer than the initial time!", "Wow", "/resources/policeman-32.png");
				return false;
			}
		}

		onCheckoutThreadManager();

		long idleTime = func.updateTimeIdle(false, true, 0);
		prot.textIdle.setText(Util.printTime(idleTime, "%sh %sm %ss"));

		long endTime = func.getTempTimePack().getEnd();
		prot.textEndTime.setText(func.formatDate(null, endTime));

		long elapsedTime = func.updateTimeElapsed(false, 0);
		prot.textElapsed.setText(Util.printTime(elapsedTime, "%sh %sm %ss"));

		long remainTime = func.updateTimeRemain(false, 0);
		prot.textRemain.setText(Util.printTime(remainTime, "%sh %sm %ss"));

		prot.updateConsole(func.generateInfo());

		if (!preloaded)
			new DataManager(func).writeSessionPack();

		return true;
	}

	@Override
	public boolean onCheckin() {
		return onCheckin(false);
	};

	@Override
	public boolean onCheckin(boolean preLoaded) {
		Date customTime;

		if (preLoaded) {
			customTime = new Date(dataManager.getTp().getStart());

			prot.buttonInitialTime.setEnabled(false);
			prot.buttonFinalTime.setEnabled(true);
		} else {
			CustomTime custon = new CustomTime();
			if (!custon.showDialog())
				return false;

			customTime = custon.getCustomDate();
		}

		func.setInitialTime(customTime);

		onChekinThreadManager();

		long idleTime = func.updateTimeIdle(false, false, 0);
		prot.textIdle.setText(Util.printTime(idleTime, "%sh %sm %ss"));

		long time = func.getTempTimePack().getStart();
		prot.textStartTime.setText(func.formatDate(null, time));

		long predictedTime = func.getTimeSheet().getTimePredicted();
		prot.textPredicted.setText(func.formatDate(null, predictedTime));

		prot.textEndTime.setText("");
		prot.txtSession.setText("Session sequence: " + (func.getTimeSheet().getTimePacks().size() + 1));

		if (!preLoaded)
			new DataManager(func).writeCheckinInfo();

		return true;
	}

	@Override
	public boolean onNewTimeSheet() {
		return onNewTimeSheet(false);
	}

	@Override
	public boolean onNewTimeSheet(boolean preLoaded) {
		if (preLoaded) {
			func = new OperationCore();
			func.setTimePerDay(dataManager.getTimePerDay());
			func.setPredPause(dataManager.getPredPause());

			prot.buttonInitialTime.setEnabled(true);
		} else {
			NewTimeSheet timesheet = new NewTimeSheet();

			if (!timesheet.showDialog())
				return false;

			func = new OperationCore();

			updateSeconds = timesheet.getUpdateSeconds();
			clockUpdateInterval = timesheet.getClockUpdateSeconds();

			func.setTimePerDay(timesheet.getTimePerDay());
			func.setPredPause(timesheet.getPredPause());

			new DataManager(func).writeTimeSheetInfo();
		}

		onNewTimeSheetThreadManager();

		prot.textTimeBase.setText(Util.printTime(func.getTimePerDay(), "%sh %sm %ss"));
		prot.textPredPause.setText(Util.printTime(func.getPredPause(), "%sh %sm %ss"));

		return true;
	}

	private void updateTMS() {
		onNewTimeSheet(true);

		if (dataManager.getTs().getTimePacks().size() != 0) {
			func.setTimeSheet(dataManager.getTs());
			func.setTempTimePack(dataManager.getLastTP());
			onCheckout(true);
		}
		if (dataManager.getTp().getStart() != 0)
			onCheckin(true);

		prot.updateConsole(func.generateInfo());
	}

	@Override
	public void dailyBackup() {
		dataManager.dailyBackup();
	}

	@Override
	public void monthlyBackup() {
		dataManager.monthlyBackup();
	}
}
