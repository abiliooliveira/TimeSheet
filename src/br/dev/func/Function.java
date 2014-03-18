package br.dev.func;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class Function {
	
	private long timePerDay;
			
	private TimeSheet timeSheet;
	private TimePack tempTimePack;
		
	public long getTimePerDay() {
		return timePerDay;
	}

	public void setTimePerDay(long timePerDay) {
		this.timePerDay = timePerDay;
	}

	public TimeSheet getTimeSheet() {
		return timeSheet;
	}

	public TimePack getTempTimePack() {
		return tempTimePack;
	}

	public Function() {
		timePerDay  = Util.toHours(8);
		timeSheet = TimeSheet.getInstance(timePerDay);
		tempTimePack = new TimePack();		
	}
	
	public void setInitialTime(Date date){
		tempTimePack = new TimePack();
		
		if(date == null)
			tempTimePack.setStart(Util.getTime().getTime());
		else
			tempTimePack.setStart(date.getTime());
		
		long predicted = tempTimePack.getStart() + timeSheet.getTimeRemain();
		
		timeSheet.setTimePredicted(predicted);
	}
	
	public void setFinalTime(Date date){
		if(date ==  null)
			tempTimePack.setEnd(Util.getTime().getTime());
		else
			tempTimePack.setEnd(date.getTime());
	}	
	
	
	public void updateTimeElapsed(boolean update, int updateFrequence){
		long diff = 0;
		if(update)
			diff = getTimeSheet().getTimeElapsed() + Util.toSeconds(updateFrequence);
		else{
			for (TimePack tp : getTimeSheet().getTimePacks()) {
				diff += tp.getInterval();				
			}
		}
		
		timeSheet.setTimeElapsed(diff);
	}
	
	public void updateTimeRemain(boolean update, int updateFrequence){
		long diff;
		if(update)
			diff = getTimeSheet().getTimeRemain() - Util.toSeconds(updateFrequence);
		else 
			diff = timePerDay - timeSheet.getTimeElapsed();
		
		timeSheet.setTimeRemain(diff);
	}
			
		
	public Date toDate(long value){
		return new Date(value);
	}

	public String generateInfo() {
		StringBuffer sb = new StringBuffer();
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");
		
		List<TimePack> tps = getTimeSheet().getTimePacks();
		
		
		for (int i = tps.size()-1; i >= 0; i--) {
			TimePack tp = tps.get(i);
			
			sb.append("\n");
			sb.append("Session "+(i+1));
			sb.append("   ------------------------------------------- \n");
			sb.append("\tStart :"+ sdf.format(new Date(tp.getStart())));
			sb.append("\n");
			sb.append("\tEnd  :"+ sdf.format(new Date(tp.getEnd())));
			sb.append("\n");
			sb.append("\tInterval "+ Util.printTime(tp.getInterval(), "%sh %sm %ss"));
			
		}		
		
		return sb.toString();
	}
	
}
