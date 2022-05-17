package simple_tcp;

import common.Packet;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.PriorityQueue;

public class PacketBuffer
{
    PriorityQueue<AbstractMap.SimpleEntry<Integer, Packet>> buffer;

    public PacketBuffer()
    {
        buffer = new PriorityQueue<>(Comparator.comparing(AbstractMap.SimpleEntry::getKey));
    }

    public void add(Integer packetNumber, Packet packet)
    {
        buffer.add(new AbstractMap.SimpleEntry<>(packetNumber, packet));
    }

    public Integer getPacketNumber()
    {
        if(buffer.peek() == null)
        {
            return -1;
        }
        return buffer.peek().getKey();
    }

    public AbstractMap.SimpleEntry<Integer, Packet> pop()
    {
        return buffer.poll();
    }

    public Packet retrievePacket()
    {
        if(buffer.peek() == null)
        {
            return null;
        }
        return buffer.poll().getValue();
    }

    public Packet getPacket()
    {
        if(buffer.peek() == null)
        {
            return null;
        }
        return buffer.peek().getValue();
    }


    public boolean isEmpty()
    {
        return buffer.isEmpty();
    }

    public int getSize()
    {
        return buffer.size();
    }

    public void removePacket(Integer number, Packet packet)
    {
        buffer.remove(new AbstractMap.SimpleEntry<>(number, packet));
    }
}
