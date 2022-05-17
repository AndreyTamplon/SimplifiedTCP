package exceptions;

public class TooBigPackageException extends Exception
{
    public TooBigPackageException(String message)
    {
        super(message);
    }

    public TooBigPackageException(String message, Throwable cause)
    {
        super(message, cause);
    }
}

