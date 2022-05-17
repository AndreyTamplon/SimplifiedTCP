package simple_tcp;
import common.Packet;
import common.Socket;
import exceptions.ConnectionException;
import exceptions.SendException;
import exceptions.TooBigPackageException;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Client extends Socket
{
    private InetAddress address;
    private int base;
    private int port;
    private final DatagramSocket socket;
    private int nextSequenceNumber;
    private int acknowledgementNumber;
    TCPTimer timer;
    PacketBuffer aheadSchedulePackets;
    DataStorage receiveBuffer;
    PacketBuffer notYetAckedPackets;
    DataStorage sendBuffer;
    public Client(int port) throws UnknownHostException, SocketException
    {
        nextSequenceNumber = 1;
        acknowledgementNumber = 0;
        base = 0;
        socket = new DatagramSocket(port, InetAddress.getLocalHost());
        timer = new TCPTimer();
        receiveBuffer = new DataStorage();
        sendBuffer = new DataStorage();
        notYetAckedPackets = new PacketBuffer();
        aheadSchedulePackets = new PacketBuffer();
    }

    public void connect(InetAddress address, int port) throws ConnectionException
    {
        this.address = address;
        this.port = port;
        Packet connectionRequest = new Packet(acknowledgementNumber, nextSequenceNumber, false, true, false);
        try
        {
            sendPacket(connectionRequest, port, address, socket);
            socket.setSoTimeout(timer.getSocketTimeout());
            Packet connectionResponse = receivePacket(socket);
            if (connectionResponse == null
                    || !connectionResponse.isSYNACK()
                    || connectionResponse.getAcknowledgmentNumber() != nextSequenceNumber)
            {
                throw new ConnectionException("Failed to set up connection");
            }
            acknowledgementNumber = connectionResponse.getSequenceNumber();
            nextSequenceNumber++;
            sendPacket(new Packet(acknowledgementNumber, nextSequenceNumber,
                    true, false, false), port, address, socket);
            nextSequenceNumber++;
        }
        catch (SendException | IOException e)
        {
            throw new ConnectionException("Failed to set up connection", e);
        }
    }


    public void closeConnection() throws ConnectionException
    {
        try
        {
            sendPacket(new Packet(acknowledgementNumber, nextSequenceNumber,
                    false, false, true), port, address, socket);
            socket.setSoTimeout(timer.getSocketTimeout());
            Packet FINACK = receivePacket(socket);
            if (FINACK == null
                    || !FINACK.isFINACK()
                    || FINACK.getAcknowledgmentNumber() != nextSequenceNumber)
            {
                throw new ConnectionException("Failed to end connection");
            }
            acknowledgementNumber = FINACK.getSequenceNumber();
            nextSequenceNumber++;
            sendPacket(new Packet(acknowledgementNumber, nextSequenceNumber,
                    true, false, false), port, address, socket);
            socket.close();
        }
        catch (SendException | IOException e)
        {
            throw new ConnectionException("Failed to end connection", e);
        }


    }
    public void putDataInBuffer(byte[] data) throws TooBigPackageException
    {
        sendBuffer.put(data);
    }

    public void startSending() throws InterruptedException, SendException
    {
        sender();
    }

    private void receiver()
    {
        Packet receivedPacket;
        while(!notYetAckedPackets.isEmpty() || sendBuffer.hasDataToSend())
        {
            try
            {
                receivedPacket = receivePacket(socket);
            }
            catch (IOException e)
            {
                continue;
            }
            int receivedAcknowledgementNumber = receivedPacket.getAcknowledgmentNumber();
            if (receivedAcknowledgementNumber > base)
            {
                while(notYetAckedPackets.getPacketNumber() <= receivedAcknowledgementNumber && notYetAckedPackets.getPacketNumber() >= 0)
                {
                    notYetAckedPackets.pop();
                }
                base = receivedAcknowledgementNumber;
                if (base == nextSequenceNumber - 1)
                {
                    timer.startTimer();
                }
            }
        }
    }

    private void sender() throws SendException, InterruptedException
    {
        Thread receiveThread = null;
        while(sendBuffer.hasDataToSend() || !notYetAckedPackets.isEmpty())
        {
            if (receiveThread == null || receiveThread.getState() == Thread.State.TERMINATED)
            {
                receiveThread = new Thread(this::receiver);
                receiveThread.start();
            }
            if(!timer.isRunning())
            {
                timer.startTimer();
            }
            if(sendBuffer.hasDataToSend())
            {
                Packet dataPacket = new Packet(
                        acknowledgementNumber,
                        nextSequenceNumber,
                        true,
                        false,
                        false, sendBuffer.retrieveData());
                sendPacket(dataPacket, port, address, socket);
                notYetAckedPackets.add(nextSequenceNumber, dataPacket);
                nextSequenceNumber++;
            }
            if(timer.timeoutOccurred())
            {
                Packet packetToRetransmission = notYetAckedPackets.getPacket();
                if(packetToRetransmission != null)
                {
                    sendPacket(packetToRetransmission, port, address, socket);
                }
                timer.startTimer();
            }
        }
        receiveThread.join();
    }

}
