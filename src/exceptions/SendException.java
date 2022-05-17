package exceptions;

public class SendException extends Exception
{
    public SendException(String message)
    {
        super(message);
    }

    public SendException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
