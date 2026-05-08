package util;

import model.Process;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InputValidator {

    public static String validateProcessInput(String id, String arrival, String burst, String priority, List<Process> existingProcesses) {
        if (id == null || id.trim().isEmpty()) {
            return "Process ID is required.";
        }

        if (arrival == null || arrival.trim().isEmpty() ||
            burst == null || burst.trim().isEmpty() ||
            priority == null || priority.trim().isEmpty()) {
            return "All fields are required.";
        }

        int arrivalTime;
        int burstTime;
        int priorityValue;

        try {
            arrivalTime = Integer.parseInt(arrival.trim());
            burstTime = Integer.parseInt(burst.trim());
            priorityValue = Integer.parseInt(priority.trim());
        } catch (NumberFormatException e) {
            return "Arrival Time, Burst Time, and Priority must be integers.";
        }

        if (arrivalTime < 0) {
            return "Arrival Time cannot be negative.";
        }

        if (burstTime <= 0) {
            return "Burst Time must be greater than 0.";
        }

        if (priorityValue < 0) {
            return "Priority cannot be negative.";
        }

        for (Process process : existingProcesses) {
            if (process.getId().equalsIgnoreCase(id.trim())) {
                return "Duplicate Process ID is not allowed.";
            }
        }

        return null;
    }

    public static String validateProcessList(List<Process> processes) {
        if (processes == null || processes.isEmpty()) {
            return "Please add at least one process.";
        }

        Set<String> ids = new HashSet<>();

        for (Process process : processes) {
            if (process.getArrivalTime() < 0) {
                return "Invalid arrival time found.";
            }

            if (process.getBurstTime() <= 0) {
                return "Invalid burst time found.";
            }

            if (process.getPriority() < 0) {
                return "Invalid priority value found.";
            }

            String normalizedId = process.getId().trim().toLowerCase();
            if (ids.contains(normalizedId)) {
                return "Duplicate Process IDs found.";
            }
            ids.add(normalizedId);
        }

        return null;
    }
}