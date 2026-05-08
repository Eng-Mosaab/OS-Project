package metrics;

import model.Process;

import java.util.List;

public class MetricsCalculator {

    public static void calculateMetrics(List<Process> processes) {
        for (Process process : processes) {
            int tat = process.getCompletionTime() - process.getArrivalTime();
            int wt = tat - process.getBurstTime();
            int rt = process.getFirstStartTime() - process.getArrivalTime();

            process.setTurnaroundTime(tat);
            process.setWaitingTime(wt);
            process.setResponseTime(rt);
        }
    }

    public static double getAverageWaitingTime(List<Process> processes) {
        if (processes.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Process process : processes) {
            sum += process.getWaitingTime();
        }
        return sum / processes.size();
    }

    public static double getAverageTurnaroundTime(List<Process> processes) {
        if (processes.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Process process : processes) {
            sum += process.getTurnaroundTime();
        }
        return sum / processes.size();
    }

    public static double getAverageResponseTime(List<Process> processes) {
        if (processes.isEmpty()) {
            return 0;
        }

        double sum = 0;
        for (Process process : processes) {
            sum += process.getResponseTime();
        }
        return sum / processes.size();
    }
}