package exceptions;

public class ReceiveException extends Exception
{
    public ReceiveException(String message)
    {
        super(message);
    }

    public ReceiveException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
