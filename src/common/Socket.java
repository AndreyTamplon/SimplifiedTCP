package common;

import exceptions.SendException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

public class Socket
{

    public void sendACK(int acknowledgementNumber, int nextSequenceNumber, int port, InetAddress address, DatagramSocket socket) throws SendException
    {
        Packet ACK = new Packet(
                acknowledgementNumber,
                nextSequenceNumber,
                true,
                false,
                false
        );
        sendPacket(ACK, port, address, socket);
    }
    public void sendPacket(Packet packet, int port, InetAddress address, DatagramSocket socket) throws SendException
    {
        try
        {
            byte[] bytePacket = packet.toByteArray();
            DatagramPacket sendPacket = new DatagramPacket(bytePacket, bytePacket.length, address, port);
            socket.send(sendPacket);
        }
        catch (IOException e)
        {
            throw new SendException("Failed to send packet");
        }
    }

    public Packet receivePacket(int bufferSize, DatagramSocket socket) throws IOException
    {
        try
        {
            byte[] buffer = new byte[bufferSize];
            DatagramPacket datagram = new DatagramPacket(buffer, buffer.length);
            socket.receive(datagram);
            return unpackData(datagram.getData());
        }
        catch (SocketTimeoutException e)
        {
            return null;
        }
    }

    public Packet receivePacket(DatagramSocket socket) throws IOException
    {
        return receivePacket(512, socket);
    }

    protected Packet unpackData(byte[] segment)
    {
        ByteBuffer buffer = ByteBuffer.wrap(segment);
        int acknowledgmentNumber = buffer.getInt();
        int sequenceNumber = buffer.getInt();
        byte ACK = buffer.get();
        byte SYN = buffer.get();
        byte FIN = buffer.get();
        int dataLength = buffer.getInt();
        byte[] data = new byte[dataLength];
        buffer.get(data, 0, dataLength);
        return new Packet(acknowledgmentNumber, sequenceNumber, ACK == 1, SYN == 1, FIN == 1, data);
    }
}
