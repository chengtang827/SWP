package lab;

import java.io.DataInputStream;

public class FrameHandler extends Thread
{
  private DataInputStream smdis = null;
  private SWE swe = null;

  public FrameHandler(DataInputStream paramDataInputStream, SWE paramSWE)
  {
    this.smdis = paramDataInputStream;
    this.swe = paramSWE;
  }

  public void run()
  {
    PFrameMsg localPFrameMsg = new PFrameMsg();
    while (true) {
      get_frame(localPFrameMsg);
      this.swe.generate_frame_arrival_event(localPFrameMsg);
      localPFrameMsg = new PFrameMsg();
    }
  }

  private void get_frame(PFrameMsg paramPFrameMsg)
  {
    try
    {
      paramPFrameMsg.receive(this.smdis);
    } catch (Exception localException) {
      System.out.println("FrameHandler: Error on receiving frame: " + localException + " quitting...");
      System.exit(0);
    }
  }
}