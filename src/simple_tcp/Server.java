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
import java.util.Random;

public class Server extends Socket
{
    private InetAddress address;
    private int port;
    private final DatagramSocket socket;
    private int nextSequenceNumber;
    private int acknowledgementNumber;
    private final Random lossGenerator = new Random();
    private final double lossProbability;
    TCPTimer timer;
    PacketBuffer aheadSchedulePackets;
    DataStorage receiveBuffer;

    public Server(int port, double lossProbability) throws UnknownHostException, SocketException
    {
        nextSequenceNumber = 1;
        acknowledgementNumber = 0;
        socket = new DatagramSocket(port, InetAddress.getLocalHost());
        timer = new TCPTimer();
        receiveBuffer = new DataStorage();
        this.lossProbability = lossProbability;
        aheadSchedulePackets = new PacketBuffer();
    }
    public void acceptConnection(InetAddress address, int port) throws ConnectionException
    {
        this.address = address;
        this.port = port;
        try
        {
            Packet senderRequest = receivePacket(socket);
            if (senderRequest != null && senderRequest.isSYN())
            {
                acknowledgementNumber = senderRequest.getSequenceNumber();
                Packet receiverResponse = new Packet(acknowledgementNumber, nextSequenceNumber, true, true, false);
                sendPacket(receiverResponse, port, address, socket);
                socket.setSoTimeout(timer.getSocketTimeout());
                Packet senderResponse = receivePacket(socket);
                if (senderResponse == null
                        || !senderResponse.isACK()
                        || senderResponse.getAcknowledgmentNumber() != nextSequenceNumber)
                {
                    throw new ConnectionException("Failed to set up connection");
                }
                nextSequenceNumber++;
                acknowledgementNumber++;
                socket.setSoTimeout(0);
            }
            else
            {
                throw new ConnectionException("Failed to set up connection");
            }
        }
        catch (SendException | IOException e)
        {
            throw new ConnectionException("Failed to set up connection", e);
        }
    }

    public byte[] receive() throws TooBigPackageException, SendException
    {
        if(receiveBuffer.getSize() == 0)
        {
            receiver();
        }
        return receiveBuffer.retrieveData();
    }

    private boolean interferenceOccurred()
    {
        return (lossGenerator.nextInt(10) < lossProbability * 10);
    }

    public void closeConnection() throws ConnectionException
    {
        try
        {
            Packet FIN = receivePacket(socket);
            if(FIN != null && FIN.isFIN())
            {
                acknowledgementNumber = FIN.getSequenceNumber();
                Packet FINACK = new Packet(acknowledgementNumber, nextSequenceNumber, true, false, true);
                sendPacket(FINACK, port, address, socket);
                socket.setSoTimeout(timer.getSocketTimeout());
                Packet senderResponse = receivePacket(socket);
                if (senderResponse == null
                        || !senderResponse.isACK()
                        || senderResponse.getAcknowledgmentNumber() != nextSequenceNumber)
                {
                    throw new ConnectionException("Failed to end connection");
                }
                socket.close();
            }
            else
            {
                throw new ConnectionException("Failed to end connection");
            }
        }
        catch (SendException | IOException e)
        {
            throw new ConnectionException("Failed to end connection", e);
        }
    }

    private void receiver() throws TooBigPackageException, SendException
    {
        Packet receivedPacket;
        while (true)
        {
            try
            {
                receivedPacket = receivePacket(socket);
                if(interferenceOccurred())
                {
                    continue;
                }

            }
            catch (IOException e)
            {
                continue;
            }
            int receivedPackedSequenceNumber = receivedPacket.getSequenceNumber();
            if(receivedPackedSequenceNumber == acknowledgementNumber + 1)
            {
                Packet packetToWrite = receivedPacket;
                while(true)
                {
                    receiveBuffer.put(packetToWrite.getData());
                    acknowledgementNumber++;
                    int packetNumber = aheadSchedulePackets.getPacketNumber();
                    if(packetNumber != acknowledgementNumber + 1)
                    {
                        break;
                    }
                    packetToWrite = aheadSchedulePackets.retrievePacket();
                }
                sendACK(acknowledgementNumber, nextSequenceNumber, port, address, socket);
                break;
            }
            else if(receivedPackedSequenceNumber > acknowledgementNumber + 1)
            {
                aheadSchedulePackets.add(receivedPackedSequenceNumber, receivedPacket);
            }
            sendACK(acknowledgementNumber, nextSequenceNumber, port, address, socket);
        }
    }
}
