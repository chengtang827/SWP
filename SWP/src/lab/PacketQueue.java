package lab;

import java.util.Vector;

public class PacketQueue
{
  @SuppressWarnings("rawtypes")
private Vector queue = new Vector(8);

  @SuppressWarnings("unchecked")
public synchronized void put(Packet paramPacket)
  {
    this.queue.addElement(paramPacket);
    super.notify();
  }

  public synchronized Packet get()
  {
    while (this.queue.isEmpty())
      try {
        super.wait();
      } catch (InterruptedException localInterruptedException) {
      }
    Packet localPacket = (Packet)this.queue.firstElement();
    this.queue.removeElementAt(0);
    return localPacket;
  }
}