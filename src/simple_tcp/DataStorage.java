package simple_tcp;

import exceptions.TooBigPackageException;

import java.util.ArrayDeque;
import java.util.Deque;

public class DataStorage
{
    Deque<byte[]> data;
    private static final int MAXIMUM_SEGMENT_SIZE = 512;
    public DataStorage()
    {
        this.data = new ArrayDeque<>();
    }

    public void put(byte[] data) throws TooBigPackageException
    {
        if(data.length > MAXIMUM_SEGMENT_SIZE)
        {
            throw new TooBigPackageException(String.format("Attempt to write more than %d byte", MAXIMUM_SEGMENT_SIZE));
        }
        this.data.add(data);
    }
    public byte[] retrieveData()
    {
        return data.poll();
    }
    public boolean hasDataToSend()
    {
        return !data.isEmpty();
    }
    public int getSize()
    {
        if(data == null)
        {
            return 0;
        }
        return data.size();

    }
}
