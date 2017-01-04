package epmc.ic3.util;

import java.util.concurrent.TimeUnit;

public class Timer {
	
	private long timer ;
	
	public void startTimer() {
		this.timer = time(); 
	}
	
	public long endTimer() {
		return toSeconds((time() - this.timer));
	}
	
	private long toSeconds(long time) {
		return TimeUnit.MILLISECONDS.toSeconds(time);
	}
	
	public void stop() {
		this.timer = 0;
	}
	private long time() {
		return System.currentTimeMillis();
	}
	

}
