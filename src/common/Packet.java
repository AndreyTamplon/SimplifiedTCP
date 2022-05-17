package common;

import java.nio.ByteBuffer;

public class Packet
{
    private final int acknowledgmentNumber;
    private final int sequenceNumber;
    private static final int headerLength = 15;
    private final boolean ACK;
    private final boolean SYN;
    private final boolean FIN;
    private byte[] data;

    public Packet(int acknowledgmentNumber, int sequenceNumber, boolean ACK, boolean SYN, boolean FIN)
    {
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.sequenceNumber = sequenceNumber;
        this.ACK = ACK;
        this.SYN = SYN;
        this.FIN = FIN;
    }

    public Packet(int acknowledgmentNumber, int sequenceNumber, boolean ACK, boolean SYN, boolean FIN, byte[] data)
    {
        this.acknowledgmentNumber = acknowledgmentNumber;
        this.sequenceNumber = sequenceNumber;
        this.ACK = ACK;
        this.SYN = SYN;
        this.FIN = FIN;
        this.data = data;
    }


    public byte[] toByteArray()
    {
        int dataLength = 0;
        if(data != null)
        {
            dataLength = data.length;
        }
        ByteBuffer buffer = ByteBuffer.allocate(dataLength + headerLength);
        buffer.putInt(acknowledgmentNumber);
        buffer.putInt(sequenceNumber);
        buffer.put((byte) (ACK ? 1 : 0));
        buffer.put((byte) (SYN ? 1 : 0));
        buffer.put((byte) (FIN ? 1 : 0));
        buffer.putInt(dataLength);
        if(data != null)
        {
            buffer.put(data);
        }
        return buffer.array();
    }

    public int getAcknowledgmentNumber()
    {
        return acknowledgmentNumber;
    }

    public int getSequenceNumber()
    {
        return sequenceNumber;
    }

    public boolean getACK()
    {
        return ACK;
    }

    public boolean getSYN()
    {
        return SYN;
    }

    public boolean getFIN()
    {
        return FIN;
    }

    public boolean isSYNACK()
    {
        return ACK && SYN && !FIN;
    }

    public boolean isACK()
    {
        return ACK && !SYN && !FIN;
    }

    public boolean isSYN()
    {
        return !ACK && SYN && !FIN;
    }

    public boolean isFIN()
    {
        return !ACK && !SYN && FIN;
    }

    public boolean isFINACK()
    {
        return ACK && !SYN && FIN;
    }


    public byte[] getData()
    {
        return data;
    }
}
