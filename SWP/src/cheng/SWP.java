package cheng;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import lab.*;
/*===============================================================*
 *  File: SWP.java                                               *
 *                                                               *
 *  This class implements the sliding window protocol            *
 *  Used by VMach class					         *
 *  Uses the following classes: SWE, Packet, PFrame, PEvent,     *
 *                                                               *
 *  Author: Professor SUN Chengzheng                             *
 *          School of Computer Engineering                       *
 *          Nanyang Technological University                     *
 *          Singapore 639798                                     *
 *===============================================================*/

public class SWP {

	/*========================================================================*
 the following are provided, do not change them!!
	 *========================================================================*/
	//the following are protocol constants.
	public static final int MAX_SEQ = 7; 
	public static final int NR_BUFS = (MAX_SEQ + 1)/2;

	// the following are protocol variables
	private int oldest_frame = 0;
	private PEvent event = new PEvent();  
	private Packet out_buf[] = new Packet[NR_BUFS];

	//added variables
	private boolean no_nak=true;
	private ArrayList<DataTimer> timerList=new ArrayList<DataTimer>();// to keep all the timers
	private Timer ackTimer=null;
	private final long TIMEOUT=500;
	private boolean[] arrived=new boolean[NR_BUFS];
	private Packet in_buf[]=new Packet[NR_BUFS];

	//the following are used for simulation purpose only
	private SWE swe = null;
	@SuppressWarnings("unused")
	private String sid = null;  

	//Constructor
	public SWP(SWE sw, String s){
		swe = sw;
		sid = s;
	}

	//the following methods are all protocol related
	private void init(){
		for (int i = 0; i < NR_BUFS; i++){
			out_buf[i] = new Packet();
			//added
			arrived[i]=false;
		}
	}

	private void wait_for_event(PEvent e){
		swe.wait_for_event(e); //may be blocked
		oldest_frame = e.seq;  //set timeout frame seq
	}

	private void enable_network_layer(int nr_of_bufs) {
		//network layer is permitted to send if credit is available
		swe.grant_credit(nr_of_bufs);
	}

	private void from_network_layer(Packet p) {
		swe.from_network_layer(p);
	}

	private void to_network_layer(Packet packet) {
		swe.to_network_layer(packet);
	}

	private void to_physical_layer(PFrame fm)  {
		System.out.println("SWP: Sending frame: seq = " + fm.seq + 
				" ack = " + fm.ack + " kind = " + 
				PFrame.KIND[fm.kind] + " info = " + fm.info.data );
		System.out.flush();
		swe.to_physical_layer(fm);
	}

	private void from_physical_layer(PFrame fm) {
		PFrame fm1 = swe.from_physical_layer(); 
		fm.kind = fm1.kind;
		fm.seq = fm1.seq; 
		fm.ack = fm1.ack;
		fm.info = fm1.info;
	}


	/*===========================================================================*
 	implement your Protocol Variables and Methods below: 
	 *==========================================================================*/



	public void protocol6() {
		//added variables
		int credit=0;             //to record how many ack arrives
		int next_frame_to_send=0;
		int ack_expected=0;
		int frame_expected=0;
		int too_far=NR_BUFS;
		PFrame r;


		init();
		enable_network_layer(NR_BUFS);

		while(true) {
			enable_network_layer(credit); 
			credit=0;                     

			wait_for_event(event);

			switch(event.type) {
			case (PEvent.NETWORK_LAYER_READY):

				from_network_layer(out_buf[next_frame_to_send%NR_BUFS]);
				send_frame(PFrame.DATA,next_frame_to_send,frame_expected,out_buf);
				next_frame_to_send=inc(next_frame_to_send);	    	  
				break; 
				
			case (PEvent.FRAME_ARRIVAL ):

				r=new PFrame();
				from_physical_layer(r);
				
				
				if(r.kind==PFrame.DATA){// if data arrives

					if((r.seq!=frame_expected)&&no_nak){ // if the frame is not expected and haven't send NAK yet
						send_frame(PFrame.NAK,0,frame_expected,out_buf);
					}
					else{
						start_ack_timer();          //this packet will be received, and need a ask timer for it
					}
	
	
					if(between(frame_expected,r.seq,too_far)&&(arrived[r.seq%NR_BUFS]==false)){ // if this frame is in the receiving window and not buffered yet
						arrived[r.seq%NR_BUFS]=true;
						in_buf[r.seq%NR_BUFS]=r.info;
	
						while(arrived[frame_expected%NR_BUFS]){   //to feed in all the in-order frames up to date
	
							to_network_layer(in_buf[frame_expected%NR_BUFS]);
							no_nak=true;
							arrived[frame_expected%NR_BUFS]=false;
							frame_expected=inc(frame_expected);
							too_far=inc(too_far);
							start_ack_timer();
						}
	
					}
				}

				if((r.kind==PFrame.NAK)&&between(ack_expected,(r.ack+1)%(MAX_SEQ+1),next_frame_to_send)){// if it is an NAK, and the miss frame is within sending window
					send_frame(PFrame.DATA,(r.ack+1)%(MAX_SEQ+1),frame_expected,out_buf); //send the missing frame
				}
				while(between(ack_expected,r.ack,next_frame_to_send)){// update the sending window according to ack
					credit+=1;              
					stop_timer(ack_expected);
					ack_expected=inc(ack_expected);				
				}							
				break;	   
			case (PEvent.CKSUM_ERR):

				if(no_nak){
					send_frame(PFrame.NAK,0,frame_expected,out_buf);
				}
				break;  
			case (PEvent.TIMEOUT): 

				send_frame(PFrame.DATA,oldest_frame,frame_expected,out_buf);
				break; 
			case (PEvent.ACK_TIMEOUT): 

				send_frame(PFrame.ACK,0,frame_expected,out_buf);
				break; 
			default: 
				System.out.println("SWP: undefined event type = "+ event.type); 
				System.out.flush();
			}




		}      
	}

	/* Note: when start_timer() and stop_timer() are called, 
    the "seq" parameter must be the sequence number, rather 
    than the index of the timer array, 
    of the frame associated with this timer, 
	 */

	private void start_timer(int seq) {
		//all the timers are kept in an arraylist for easier retrace
		DataTimer timer=findTimerBySeq(seq);
		if(timer==null){
			//create a timer that will timeout in TIMEOUT milliseconds
			timer=new DataTimer(){
				public void run() {
					schedule(new TimerTask(){
						public void run(){
							swe.generate_timeout_event(seq);
						}
					},TIMEOUT);

				} 
			};
			timer.seq=seq;
			timerList.add(timer);
			timer.run();
		}
		else{
			//remove this timer
			timer.cancel();
			timerList.remove(timer);
			//start again
			timer=new DataTimer(){
				public void run() {
					schedule(new TimerTask(){
						public void run(){
							swe.generate_timeout_event(seq);
						}
					},TIMEOUT);

				} 
			};
			timer.seq=seq;
			timerList.add(timer);
			timer.run();
		}
	}

	private void stop_timer(int seq) {

		DataTimer timer;
		while((timer=findTimerBySeq(seq))!=null){//there may exist some multi-threading issue on the timerList
			timer.cancel();						 // just ensure this timer is clearly removed by a while loop						
			timerList.remove(timer);
		}


	}

	private void start_ack_timer() {    // similar to normal timers
		ackTimer=new Timer();
		ackTimer.schedule(new TimerTask(){
			public void run(){
				swe.generate_acktimeout_event();
			}
		}, TIMEOUT);
	}

	private void stop_ack_timer() {
		if(ackTimer!=null){
			ackTimer.cancel();
			ackTimer=null;
		}
	}


	//added methods
	private boolean between(int a,int b,int c){
		return ((a<=b)&&(b<c))||((c<a)&&(a<=b))||((b<c)&&(c<a));
	}



	private DataTimer findTimerBySeq(int seq){
		for(DataTimer timer : timerList){
			if(timer.seq==seq){
				return timer;
			}
		}
		return null;
	}
	private int inc(int temp){
		return (temp+1)%(MAX_SEQ+1);
	}

	private void send_frame(int kind, int frame_nr, int frame_expected,Packet[] buffer){



		PFrame s=new PFrame();
		s.kind=kind;
		if(kind==PFrame.DATA){
			s.info=buffer[frame_nr%NR_BUFS];
		}
		s.seq=frame_nr;
		s.ack=(frame_expected+MAX_SEQ)%(MAX_SEQ+1);
		if(kind==PFrame.NAK){
			no_nak=false;
		}

		to_physical_layer(s);

		if(kind==PFrame.DATA){
			start_timer(frame_nr);
		}
		stop_ack_timer();                    //because the ack is piggtbacked on this frame, so we don't need a separate frame
	}

}//End of class

/* Note: In class SWE, the following two public methods are available:
   . generate_acktimeout_event() and
   . generate_timeout_event(seqnr).

   To call these two methods (for implementing timers),
   the "swe" object should be referred as follows:
     swe.generate_acktimeout_event(), or
     swe.generate_timeout_event(seqnr).
 */


