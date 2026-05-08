package scheduler;

import model.GanttEntry;
import model.Process;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PreemptivePriorityScheduler {

    public static class ScheduleResult {
        private List<Process> processes;
        private List<GanttEntry> ganttChart;

        public ScheduleResult(List<Process> processes, List<GanttEntry> ganttChart) {
            this.processes = processes;
            this.ganttChart = ganttChart;
        }

        public List<Process> getProcesses() {
            return processes;
        }

        public List<GanttEntry> getGanttChart() {
            return ganttChart;
        }
    }

    public ScheduleResult schedule(List<Process> originalProcesses) {
        List<Process> processes = new ArrayList<>();
        for (Process p : originalProcesses) {
            processes.add(new Process(p));
        }

        List<GanttEntry> ganttChart = new ArrayList<>();

        int time = 0;
        int completed = 0;
        int n = processes.size();
        String currentLabel = null;

        while (completed < n) {
            Process selected = getHighestPriorityProcess(processes, time);

            if (selected == null) {
                addOrExtendGanttEntry(ganttChart, "Idle", time, time + 1, currentLabel);
                currentLabel = "Idle";
                time++;
                continue;
            }

            if (!selected.isStarted()) {
                selected.setStarted(true);
                selected.setFirstStartTime(time);
            }

            addOrExtendGanttEntry(ganttChart, selected.getId(), time, time + 1, currentLabel);
            currentLabel = selected.getId();

            selected.setRemainingTime(selected.getRemainingTime() - 1);
            time++;

            if (selected.getRemainingTime() == 0) {
                selected.setCompletionTime(time);
                completed++;
            }
        }

        return new ScheduleResult(processes, ganttChart);
    }

    private Process getHighestPriorityProcess(List<Process> processes, int currentTime) {
        return processes.stream()
                .filter(p -> p.getArrivalTime() <= currentTime && p.getRemainingTime() > 0)
                .min(Comparator
                        .comparingInt(Process::getPriority)
                        .thenComparingInt(Process::getArrivalTime)
                        .thenComparing(Process::getId))
                .orElse(null);
    }

    private void addOrExtendGanttEntry(List<GanttEntry> ganttChart, String label, int start, int end, String currentLabel) {
        if (currentLabel == null || !currentLabel.equals(label)) {
            ganttChart.add(new GanttEntry(label, start, end));
        } else {
            GanttEntry lastEntry = ganttChart.get(ganttChart.size() - 1);
            lastEntry.setEndTime(end);
        }
    }
}