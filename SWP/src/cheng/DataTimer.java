package cheng;

import java.util.Timer;


public abstract class DataTimer extends Timer{
	// this class is a Timer class with an additional seq variable
	
	public int seq;  //for tracing this timer

	abstract public void run();

}
