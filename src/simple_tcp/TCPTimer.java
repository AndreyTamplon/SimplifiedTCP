package simple_tcp;

public class TCPTimer
{
    private int socketTimeout = 12000;
    private int senderTimeout = 12;
    private long timerStartTime;
    private enum TimerStates
    {
        RUNNING,
        STOPPED
    }
    TimerStates timerState;
    public TCPTimer()
    {
        timerState = TimerStates.STOPPED;
    }

    public void startTimer()
    {
        timerStartTime = System.currentTimeMillis();
        timerState = TimerStates.RUNNING;
    }

    public void stopTimer()
    {
        timerState = TimerStates.STOPPED;
    }

    public long getTimerStartTime()
    {
        return timerStartTime;
    }

    public void setTimerState(TimerStates timerState)
    {
        this.timerState = timerState;
    }

    public int getSocketTimeout()
    {
        return socketTimeout;
    }

    public boolean isRunning()
    {
        return timerState == TimerStates.RUNNING;
    }

    public void setSocketTimeout(int socketTimeout)
    {
        this.socketTimeout = socketTimeout;
    }

    public int getSenderTimeout()
    {
        return senderTimeout;
    }

    public void setSenderTimeout(int senderTimeout)
    {
        this.senderTimeout = senderTimeout;
    }

    public boolean timeoutOccurred()
    {

        return (System.currentTimeMillis() - timerStartTime) >= senderTimeout;
    }
}