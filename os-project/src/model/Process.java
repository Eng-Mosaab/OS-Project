package model;

public class Process {
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;

    private int remainingTime;
    private int completionTime;
    private int firstStartTime;
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;

    private boolean started;

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;

        this.remainingTime = burstTime;
        this.completionTime = 0;
        this.firstStartTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = 0;
        this.started = false;
    }

    public Process(Process other) {
        this.id = other.id;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.priority = other.priority;

        this.remainingTime = other.burstTime;
        this.completionTime = 0;
        this.firstStartTime = -1;
        this.waitingTime = 0;
        this.turnaroundTime = 0;
        this.responseTime = 0;
        this.started = false;
    }

    public String getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getFirstStartTime() {
        return firstStartTime;
    }

    public void setFirstStartTime(int firstStartTime) {
        this.firstStartTime = firstStartTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public boolean isStarted() {
        return started;
    }

    public void setStarted(boolean started) {
        this.started = started;
    }
}