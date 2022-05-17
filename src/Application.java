import exceptions.ConnectionException;
import exceptions.SendException;
import exceptions.TooBigPackageException;
import simple_tcp.Client;
import simple_tcp.Server;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class Application
{
    public static void main(String[] args)
    {
        String[] messages = {
                "1) We're no strangers to love\n",
                "2) You know the rules and so do I (do I)\n",
                "3) A full commitment's what I'm thinking of\n",
                "4) You wouldn't get this from any other guy\n",
                "5) I just wanna tell you how I'm feeling\n",
                "6) Gotta make you understand\n",
                "7) Never gonna give you up\n",
                "8) Never gonna let you down\n",
                "9) Never gonna run around and desert you\n",
                "10) Never gonna make you cry",
        };
        InetAddress address;
        Server server;
        Client client;
        try
        {
            address = InetAddress.getLocalHost();
            server = new Server(777, 0.8);
            client = new Client(666);
        }
        catch (SocketException e)
        {
            System.out.println("Failed to create socket");
            e.printStackTrace();
            return;
        }
        catch (UnknownHostException e)
        {
            System.out.println("Failed to create socket");
            e.printStackTrace();
            return;
        }


        Thread clientThread = new Thread(() -> {
            try
            {
                client.connect(address, 777);
                for(String message : messages)
                {
                    byte[] messageBytes = message.getBytes();
                    client.putDataInBuffer(messageBytes);
                }
                client.startSending();
                client.closeConnection();
            }
            catch (ConnectionException | TooBigPackageException | SendException | InterruptedException e)
            {
                e.printStackTrace();
            }

        });
        Thread serverThread = new Thread(() -> {
            try
            {
                server.acceptConnection(address, 666);
                for(int i = 0; i < messages.length; ++i)
                {
                    byte[] message = server.receive();
                    System.out.print(new String(message));
                }
                server.closeConnection();
            }

            catch (ConnectionException | TooBigPackageException | SendException e)
            {
                e.printStackTrace();
            }

        });
        clientThread.start();
        serverThread.start();
    }
}