package gui;

import metrics.MetricsCalculator;
import model.GanttEntry;
import model.Process;
import scheduler.PreemptivePriorityScheduler;
import scheduler.PreemptiveSJFScheduler;
import util.InputValidator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private JTextField idField;
    private JTextField arrivalField;
    private JTextField burstField;
    private JTextField priorityField;

    private JButton addButton;
    private JButton runButton;
    private JButton deleteButton;
    private JButton clearButton;
    private JButton exportButton;

    private JComboBox<String> scenarioComboBox;
    private JButton loadScenarioButton;

    private JTable processTable;
    private JTable sjfTable;
    private JTable priorityTable;

    private DefaultTableModel tableModel;
    private DefaultTableModel sjfTableModel;
    private DefaultTableModel priorityTableModel;

    private JTextArea summaryArea;
    private JTextArea conclusionArea;

    private JLabel statusLabel;
    private JLabel priorityRuleLabel;

    private GanttChartPanel sjfGanttPanel;
    private GanttChartPanel priorityGanttPanel;

    private List<Process> processList;

    private PreemptiveSJFScheduler.ScheduleResult lastSjfResult;
    private PreemptivePriorityScheduler.ScheduleResult lastPriorityResult;

    private double lastSjfAvgWT;
    private double lastSjfAvgTAT;
    private double lastSjfAvgRT;

    private double lastPriorityAvgWT;
    private double lastPriorityAvgTAT;
    private double lastPriorityAvgRT;

    private String lastWtWinner = "";
    private String lastTatWinner = "";
    private String lastRtWinner = "";

    private final Color bgColor = new Color(245, 247, 250);
    private final Color panelColor = Color.WHITE;
    private final Color primaryColor = new Color(52, 120, 246);
    private final Color dangerColor = new Color(220, 53, 69);
    private final Color successColor = new Color(40, 167, 69);
    private final Color warningColor = new Color(255, 193, 7);
    private final Color textColor = new Color(33, 37, 41);

    public MainFrame() {
        processList = new ArrayList<>();

        setTitle("SJF vs Priority Scheduler");
        setSize(1360, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().setBackground(bgColor);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout(12, 12));

        JPanel mainPanel = new JPanel(new BorderLayout(12, 12));
        mainPanel.setBackground(bgColor);
        mainPanel.setBorder(new EmptyBorder(12, 12, 12, 12));
        setContentPane(mainPanel);

        JPanel northContainer = new JPanel(new BorderLayout(10, 10));
        northContainer.setBackground(bgColor);

        priorityRuleLabel = new JLabel("Priority rule: Smaller number means higher priority.");
        priorityRuleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        priorityRuleLabel.setForeground(primaryColor);
        priorityRuleLabel.setBorder(new EmptyBorder(0, 4, 0, 4));
        northContainer.add(priorityRuleLabel, BorderLayout.NORTH);

        JPanel inputPanel = new JPanel(new GridLayout(2, 8, 10, 10));
        inputPanel.setBackground(panelColor);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Input Panel"),
                new EmptyBorder(10, 10, 10, 10)
        ));

        idField = new JTextField();
        arrivalField = new JTextField();
        burstField = new JTextField();
        priorityField = new JTextField();

        styleTextField(idField);
        styleTextField(arrivalField);
        styleTextField(burstField);
        styleTextField(priorityField);

        addButton = new JButton("Add");
        runButton = new JButton("Compare");
        deleteButton = new JButton("Delete");
        clearButton = new JButton("Clear");
        exportButton = new JButton("Export CSV");

        styleButton(addButton, successColor, Color.WHITE);
        styleButton(runButton, primaryColor, Color.WHITE);
        styleButton(deleteButton, dangerColor, Color.WHITE);
        styleButton(clearButton, warningColor, Color.BLACK);
        styleButton(exportButton, primaryColor, Color.WHITE);

        inputPanel.add(createStyledLabel("Process ID"));
        inputPanel.add(createStyledLabel("Arrival Time"));
        inputPanel.add(createStyledLabel("Burst Time"));
        inputPanel.add(createStyledLabel("Priority"));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));
        inputPanel.add(new JLabel(""));

        inputPanel.add(idField);
        inputPanel.add(arrivalField);
        inputPanel.add(burstField);
        inputPanel.add(priorityField);
        inputPanel.add(addButton);
        inputPanel.add(runButton);
        inputPanel.add(deleteButton);
        inputPanel.add(clearButton);

        northContainer.add(inputPanel, BorderLayout.CENTER);

        JPanel scenarioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        scenarioPanel.setBackground(bgColor);
        scenarioPanel.setBorder(new EmptyBorder(4, 0, 0, 0));

        JLabel scenarioLabel = new JLabel("Scenario:");
        scenarioLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        scenarioLabel.setForeground(textColor);

        scenarioComboBox = new JComboBox<>(new String[]{
                "Basic",
                "Conflict",
                "Starvation Sensitive",
                "Validation Demo"
        });
        scenarioComboBox.setFont(new Font("SansSerif", Font.PLAIN, 13));

        loadScenarioButton = new JButton("Load Scenario");
        styleButton(loadScenarioButton, primaryColor, Color.WHITE);

        scenarioPanel.add(scenarioLabel);
        scenarioPanel.add(scenarioComboBox);
        scenarioPanel.add(loadScenarioButton);
        scenarioPanel.add(exportButton);

        northContainer.add(scenarioPanel, BorderLayout.SOUTH);
        add(northContainer, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Process ID", "Arrival Time", "Burst Time", "Priority"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        processTable = new JTable(tableModel);
        styleTable(processTable);
        JScrollPane processScrollPane = new JScrollPane(processTable);
        processScrollPane.setBorder(BorderFactory.createTitledBorder("Process Table"));

        sjfTableModel = new DefaultTableModel(
                new Object[]{"ID", "Arrival", "Burst", "Priority", "CT", "TAT", "WT", "RT"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sjfTable = new JTable(sjfTableModel);
        styleTable(sjfTable);
        JScrollPane sjfScrollPane = new JScrollPane(sjfTable);

        priorityTableModel = new DefaultTableModel(
                new Object[]{"ID", "Arrival", "Burst", "Priority", "CT", "TAT", "WT", "RT"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        priorityTable = new JTable(priorityTableModel);
        styleTable(priorityTable);
        JScrollPane priorityScrollPane = new JScrollPane(priorityTable);

        sjfGanttPanel = new GanttChartPanel("SJF Gantt Chart");
        priorityGanttPanel = new GanttChartPanel("Priority Gantt Chart");

        JPanel sjfTabPanel = new JPanel(new BorderLayout(10, 10));
        sjfTabPanel.setBackground(bgColor);
        sjfTabPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        sjfTabPanel.add(sjfScrollPane, BorderLayout.CENTER);
        sjfTabPanel.add(sjfGanttPanel, BorderLayout.SOUTH);

        JPanel priorityTabPanel = new JPanel(new BorderLayout(10, 10));
        priorityTabPanel.setBackground(bgColor);
        priorityTabPanel.setBorder(new EmptyBorder(8, 8, 8, 8));
        priorityTabPanel.add(priorityScrollPane, BorderLayout.CENTER);
        priorityTabPanel.add(priorityGanttPanel, BorderLayout.SOUTH);

        JTabbedPane resultTabs = new JTabbedPane();
        resultTabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        resultTabs.addTab("SJF Results", sjfTabPanel);
        resultTabs.addTab("Priority Results", priorityTabPanel);

        summaryArea = new JTextArea();
        summaryArea.setEditable(false);
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        summaryArea.setBackground(panelColor);
        summaryArea.setForeground(textColor);
        summaryArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane summaryScrollPane = new JScrollPane(summaryArea);
        summaryScrollPane.setBorder(BorderFactory.createTitledBorder("Comparison Summary"));

        conclusionArea = new JTextArea(6, 20);
        conclusionArea.setEditable(false);
        conclusionArea.setFont(new Font("SansSerif", Font.BOLD, 13));
        conclusionArea.setBackground(new Color(248, 249, 250));
        conclusionArea.setForeground(textColor);
        conclusionArea.setMargin(new Insets(10, 10, 10, 10));
        conclusionArea.setLineWrap(true);
        conclusionArea.setWrapStyleWord(true);
        JScrollPane conclusionScrollPane = new JScrollPane(conclusionArea);
        conclusionScrollPane.setBorder(BorderFactory.createTitledBorder("Final Conclusion"));

        JSplitPane lowerTextSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, summaryScrollPane, conclusionScrollPane);
        lowerTextSplit.setDividerLocation(220);
        lowerTextSplit.setResizeWeight(0.65);

        JSplitPane upperSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, processScrollPane, resultTabs);
        upperSplit.setDividerLocation(210);
        upperSplit.setResizeWeight(0.28);

        JSplitPane finalSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, upperSplit, lowerTextSplit);
        finalSplit.setDividerLocation(640);
        finalSplit.setResizeWeight(0.75);

        add(finalSplit, BorderLayout.CENTER);

        statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(textColor);
        statusLabel.setBorder(new EmptyBorder(6, 4, 2, 4));
        add(statusLabel, BorderLayout.SOUTH);

        addButton.addActionListener(e -> addProcess());
        runButton.addActionListener(e -> runComparison());
        deleteButton.addActionListener(e -> deleteSelectedProcess());
        clearButton.addActionListener(e -> clearAllProcesses());
        exportButton.addActionListener(e -> exportResults());
        loadScenarioButton.addActionListener(e -> loadSelectedScenario());

        setupEnterNavigation();
    }

    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 13));
        label.setForeground(textColor);
        return label;
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setPreferredSize(new Dimension(120, 36));
    }

    private void styleButton(JButton button, Color bg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(150, 38));
    }

    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("SansSerif", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(220, 235, 252));
        table.setSelectionForeground(Color.BLACK);
        table.setGridColor(new Color(230, 230, 230));
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setFillsViewportHeight(true);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setBackground(new Color(230, 238, 250));
        header.setForeground(textColor);
        header.setReorderingAllowed(false);

        DefaultTableCellRenderer headerRenderer = (DefaultTableCellRenderer) header.getDefaultRenderer();
        headerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
    }

    private void setupEnterNavigation() {
        idField.addActionListener(e -> arrivalField.requestFocus());
        arrivalField.addActionListener(e -> burstField.requestFocus());
        burstField.addActionListener(e -> priorityField.requestFocus());
        priorityField.addActionListener(e -> addButton.doClick());
    }

    private void addProcess() {
        String id = idField.getText();
        String arrival = arrivalField.getText();
        String burst = burstField.getText();
        String priority = priorityField.getText();

        String validationMessage = InputValidator.validateProcessInput(id, arrival, burst, priority, processList);

        if (validationMessage != null) {
            statusLabel.setText("Input error: " + validationMessage);
            JOptionPane.showMessageDialog(this, validationMessage, "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Process process = new Process(
                id.trim(),
                Integer.parseInt(arrival.trim()),
                Integer.parseInt(burst.trim()),
                Integer.parseInt(priority.trim())
        );

        processList.add(process);

        tableModel.addRow(new Object[]{
                process.getId(),
                process.getArrivalTime(),
                process.getBurstTime(),
                process.getPriority()
        });

        clearInputFields();
        statusLabel.setText(processList.size() + " processes loaded");
    }

    private void runComparison() {
        String validationMessage = InputValidator.validateProcessList(processList);

        if (validationMessage != null) {
            statusLabel.setText("Run error: " + validationMessage);
            JOptionPane.showMessageDialog(this, validationMessage, "Run Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        PreemptiveSJFScheduler sjfScheduler = new PreemptiveSJFScheduler();
        PreemptiveSJFScheduler.ScheduleResult sjfResult = sjfScheduler.schedule(processList);
        MetricsCalculator.calculateMetrics(sjfResult.getProcesses());

        PreemptivePriorityScheduler priorityScheduler = new PreemptivePriorityScheduler();
        PreemptivePriorityScheduler.ScheduleResult priorityResult = priorityScheduler.schedule(processList);
        MetricsCalculator.calculateMetrics(priorityResult.getProcesses());

        lastSjfResult = sjfResult;
        lastPriorityResult = priorityResult;

        sjfTableModel.setRowCount(0);
        priorityTableModel.setRowCount(0);

        fillResultTable(sjfTableModel, sjfResult.getProcesses());
        fillResultTable(priorityTableModel, priorityResult.getProcesses());

        sjfGanttPanel.setGanttEntries(sjfResult.getGanttChart());
        priorityGanttPanel.setGanttEntries(priorityResult.getGanttChart());

        lastSjfAvgWT = MetricsCalculator.getAverageWaitingTime(sjfResult.getProcesses());
        lastSjfAvgTAT = MetricsCalculator.getAverageTurnaroundTime(sjfResult.getProcesses());
        lastSjfAvgRT = MetricsCalculator.getAverageResponseTime(sjfResult.getProcesses());

        lastPriorityAvgWT = MetricsCalculator.getAverageWaitingTime(priorityResult.getProcesses());
        lastPriorityAvgTAT = MetricsCalculator.getAverageTurnaroundTime(priorityResult.getProcesses());
        lastPriorityAvgRT = MetricsCalculator.getAverageResponseTime(priorityResult.getProcesses());

        StringBuilder sb = new StringBuilder();

        sb.append("========== COMPARISON SUMMARY ==========\n\n");
        sb.append(String.format("Lower Average WT: %s\n", lastSjfAvgWT < lastPriorityAvgWT ? "SJF" : "Priority"));
        sb.append(String.format("Lower Average TAT: %s\n", lastSjfAvgTAT < lastPriorityAvgTAT ? "SJF" : "Priority"));
        sb.append(String.format("Lower Average RT: %s\n\n", lastSjfAvgRT < lastPriorityAvgRT ? "SJF" : "Priority"));

        sb.append("========== PREEMPTIVE SJF ==========\n");
        sb.append(String.format("Average WT: %.2f\n", lastSjfAvgWT));
        sb.append(String.format("Average TAT: %.2f\n", lastSjfAvgTAT));
        sb.append(String.format("Average RT: %.2f\n\n", lastSjfAvgRT));

        sb.append("========== PREEMPTIVE PRIORITY ==========\n");
        sb.append(String.format("Average WT: %.2f\n", lastPriorityAvgWT));
        sb.append(String.format("Average TAT: %.2f\n", lastPriorityAvgTAT));
        sb.append(String.format("Average RT: %.2f\n\n", lastPriorityAvgRT));

        sb.append("Priority rule: Smaller number means higher priority.\n");
        summaryArea.setText(sb.toString());

        lastWtWinner = lastSjfAvgWT < lastPriorityAvgWT ? "SJF" : "Priority";
        lastTatWinner = lastSjfAvgTAT < lastPriorityAvgTAT ? "SJF" : "Priority";
        lastRtWinner = lastSjfAvgRT < lastPriorityAvgRT ? "SJF" : "Priority";

        StringBuilder conclusion = new StringBuilder();
        conclusion.append("Final Conclusion\n\n");
        conclusion.append("Better in Waiting Time: ").append(lastWtWinner).append("\n");
        conclusion.append("Better in Turnaround Time: ").append(lastTatWinner).append("\n");
        conclusion.append("Better in Response Time: ").append(lastRtWinner).append("\n\n");
        conclusion.append("SJF is stronger in efficiency and favors shorter jobs.\n");
        conclusion.append("Priority is stronger in urgent-process handling and favors higher-priority jobs.\n");
        conclusion.append("Trade-off: efficiency versus urgency.\n");
        conclusion.append("Use SJF when shorter remaining jobs should finish faster.\n");
        conclusion.append("Use Priority when urgent tasks must get CPU earlier.");
        conclusionArea.setText(conclusion.toString());

        statusLabel.setText("Comparison completed successfully");
    }

    private String buildExportCsv() {
        StringBuilder sb = new StringBuilder();

        sb.append("Section,Algorithm,ID,Arrival,Burst,Priority,CT,TAT,WT,RT,Extra\n");

        for (Process p : lastSjfResult.getProcesses()) {
            sb.append("Process Results,SJF,")
                    .append(p.getId()).append(",")
                    .append(p.getArrivalTime()).append(",")
                    .append(p.getBurstTime()).append(",")
                    .append(p.getPriority()).append(",")
                    .append(p.getCompletionTime()).append(",")
                    .append(p.getTurnaroundTime()).append(",")
                    .append(p.getWaitingTime()).append(",")
                    .append(p.getResponseTime()).append(",")
                    .append("")
                    .append("\n");
        }

        for (Process p : lastPriorityResult.getProcesses()) {
            sb.append("Process Results,Priority,")
                    .append(p.getId()).append(",")
                    .append(p.getArrivalTime()).append(",")
                    .append(p.getBurstTime()).append(",")
                    .append(p.getPriority()).append(",")
                    .append(p.getCompletionTime()).append(",")
                    .append(p.getTurnaroundTime()).append(",")
                    .append(p.getWaitingTime()).append(",")
                    .append(p.getResponseTime()).append(",")
                    .append("")
                    .append("\n");
        }

        sb.append("Averages,SJF,,,,,,,,,")
                .append("WT=").append(String.format("%.2f", lastSjfAvgWT)).append(" | ")
                .append("TAT=").append(String.format("%.2f", lastSjfAvgTAT)).append(" | ")
                .append("RT=").append(String.format("%.2f", lastSjfAvgRT))
                .append("\n");

        sb.append("Averages,Priority,,,,,,,,,")
                .append("WT=").append(String.format("%.2f", lastPriorityAvgWT)).append(" | ")
                .append("TAT=").append(String.format("%.2f", lastPriorityAvgTAT)).append(" | ")
                .append("RT=").append(String.format("%.2f", lastPriorityAvgRT))
                .append("\n");

        sb.append("Comparison,Summary,,,,,,,,,")
                .append("Lower WT=").append(lastWtWinner).append(" | ")
                .append("Lower TAT=").append(lastTatWinner).append(" | ")
                .append("Lower RT=").append(lastRtWinner)
                .append("\n");

        sb.append("Rule,Priority,,,,,,,,,Smaller number means higher priority\n");

        sb.append("Gantt,SJF,,,,,,,,,").append(escapeCsv(buildGanttText(lastSjfResult.getGanttChart()))).append("\n");
        sb.append("Gantt,Priority,,,,,,,,,").append(escapeCsv(buildGanttText(lastPriorityResult.getGanttChart()))).append("\n");

        return sb.toString();
    }

    private String escapeCsv(String text) {
        String escaped = text.replace("\"", "\"\"");
        return "\"" + escaped.replace("\n", " | ") + "\"";
    }

    private String buildGanttText(List<GanttEntry> ganttChart) {
        StringBuilder sb = new StringBuilder();
        sb.append("Gantt Chart: ");

        for (GanttEntry entry : ganttChart) {
            sb.append("| ").append(entry.getProcessId()).append(" ");
        }
        sb.append("| ");

        if (!ganttChart.isEmpty()) {
            sb.append(" Times: ").append(ganttChart.get(0).getStartTime());
            for (GanttEntry entry : ganttChart) {
                sb.append(" ").append(entry.getEndTime());
            }
        }

        return sb.toString();
    }

    private void fillResultTable(DefaultTableModel model, List<Process> processes) {
        for (Process p : processes) {
            model.addRow(new Object[]{
                    p.getId(),
                    p.getArrivalTime(),
                    p.getBurstTime(),
                    p.getPriority(),
                    p.getCompletionTime(),
                    p.getTurnaroundTime(),
                    p.getWaitingTime(),
                    p.getResponseTime()
            });
        }
    }

    private void deleteSelectedProcess() {
        int selectedRow = processTable.getSelectedRow();

        if (selectedRow == -1) {
            statusLabel.setText("Delete error: no process selected");
            JOptionPane.showMessageDialog(this, "Please select a process to delete.", "Delete Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        processList.remove(selectedRow);
        tableModel.removeRow(selectedRow);
        statusLabel.setText(processList.size() + " processes loaded");
    }

    private void clearAllProcesses() {
        processList.clear();
        tableModel.setRowCount(0);
        sjfTableModel.setRowCount(0);
        priorityTableModel.setRowCount(0);
        summaryArea.setText("");
        conclusionArea.setText("");

        lastSjfResult = null;
        lastPriorityResult = null;

        lastSjfAvgWT = 0;
        lastSjfAvgTAT = 0;
        lastSjfAvgRT = 0;

        lastPriorityAvgWT = 0;
        lastPriorityAvgTAT = 0;
        lastPriorityAvgRT = 0;

        lastWtWinner = "";
        lastTatWinner = "";
        lastRtWinner = "";

        sjfGanttPanel.setGanttEntries(new ArrayList<>());
        priorityGanttPanel.setGanttEntries(new ArrayList<>());
        clearInputFields();
        statusLabel.setText("All data cleared");
    }

    private void loadSelectedScenario() {
        String selected = (String) scenarioComboBox.getSelectedItem();

        if (selected == null) {
            return;
        }

        clearAllProcesses();

        if (selected.equals("Basic")) {
            addScenarioProcess("P1", 0, 7, 3);
            addScenarioProcess("P2", 2, 4, 1);
            addScenarioProcess("P3", 4, 1, 4);
            addScenarioProcess("P4", 5, 4, 2);
            statusLabel.setText("Basic scenario loaded");
        } else if (selected.equals("Conflict")) {
            addScenarioProcess("P1", 0, 2, 5);
            addScenarioProcess("P2", 0, 10, 1);
            addScenarioProcess("P3", 1, 3, 4);
            addScenarioProcess("P4", 2, 1, 6);
            statusLabel.setText("Conflict scenario loaded");
        } else if (selected.equals("Starvation Sensitive")) {
            addScenarioProcess("P1", 0, 12, 5);
            addScenarioProcess("P2", 1, 2, 1);
            addScenarioProcess("P3", 2, 2, 1);
            addScenarioProcess("P4", 3, 2, 1);
            addScenarioProcess("P5", 4, 2, 1);
            statusLabel.setText("Starvation-sensitive scenario loaded");
        } else if (selected.equals("Validation Demo")) {
            statusLabel.setText("Validation demo selected. Try invalid inputs manually.");
            JOptionPane.showMessageDialog(
                    this,
                    "Validation Demo\n\nTry examples like:\nArrival Time = -1\nBurst Time = 0\nDuplicate Process ID\nPriority = -2",
                    "Validation Demo",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void addScenarioProcess(String id, int arrival, int burst, int priority) {
        Process process = new Process(id, arrival, burst, priority);
        processList.add(process);
        tableModel.addRow(new Object[]{id, arrival, burst, priority});
    }

    private void exportResults() {
        if (lastSjfResult == null || lastPriorityResult == null) {
            statusLabel.setText("Export error: no results to export");
            JOptionPane.showMessageDialog(this, "Please run comparison first.", "Export Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save CSV");
        chooser.setSelectedFile(new File("comparison_results.csv"));

        int userSelection = chooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = chooser.getSelectedFile();

            if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".csv");
            }

            try (FileWriter writer = new FileWriter(fileToSave)) {
                writer.write(buildExportCsv());
                statusLabel.setText("CSV exported successfully");
                JOptionPane.showMessageDialog(this, "CSV exported successfully.", "Export", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                statusLabel.setText("CSV export failed");
                JOptionPane.showMessageDialog(this, "Failed to export CSV file.", "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void clearInputFields() {
        idField.setText("");
        arrivalField.setText("");
        burstField.setText("");
        priorityField.setText("");
        idField.requestFocus();
    }

    private class GanttChartPanel extends JPanel {
        private List<GanttEntry> ganttEntries;
        private final String title;

        public GanttChartPanel(String title) {
            this.title = title;
            this.ganttEntries = new ArrayList<>();
            setBackground(Color.WHITE);
            setPreferredSize(new Dimension(800, 170));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createTitledBorder(title),
                    new EmptyBorder(8, 8, 8, 8)
            ));
        }

        public void setGanttEntries(List<GanttEntry> ganttEntries) {
            this.ganttEntries = ganttEntries;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (ganttEntries == null || ganttEntries.isEmpty()) {
                g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
                g2.setColor(textColor);
                g2.drawString("No Gantt data to display", 20, 60);
                return;
            }

            int startX = 20;
            int y = 45;
            int boxHeight = 45;
            int totalTime = getTotalTime();
            int availableWidth = Math.max(getWidth() - 50, 200);
            int currentX = startX;

            for (GanttEntry entry : ganttEntries) {
                int duration = entry.getEndTime() - entry.getStartTime();
                int boxWidth = Math.max(45, (duration * availableWidth) / Math.max(totalTime, 1));

                g2.setColor(getColorForProcess(entry.getProcessId()));
                g2.fillRoundRect(currentX, y, boxWidth, boxHeight, 12, 12);

                g2.setColor(Color.BLACK);
                g2.drawRoundRect(currentX, y, boxWidth, boxHeight, 12, 12);

                g2.setFont(new Font("SansSerif", Font.BOLD, 13));
                FontMetrics fm = g2.getFontMetrics();
                String label = entry.getProcessId();
                int textX = currentX + (boxWidth - fm.stringWidth(label)) / 2;
                int textY = y + ((boxHeight - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(label, textX, textY);

                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(String.valueOf(entry.getStartTime()), currentX - 3, y + boxHeight + 18);

                currentX += boxWidth;
            }

            GanttEntry last = ganttEntries.get(ganttEntries.size() - 1);
            g2.drawString(String.valueOf(last.getEndTime()), currentX - 5, y + boxHeight + 18);
        }

        private int getTotalTime() {
            if (ganttEntries == null || ganttEntries.isEmpty()) {
                return 0;
            }
            return ganttEntries.get(ganttEntries.size() - 1).getEndTime() - ganttEntries.get(0).getStartTime();
        }

        private Color getColorForProcess(String processId) {
            int hash = Math.abs(processId.hashCode());

            Color[] palette = {
                    new Color(173, 216, 230),
                    new Color(144, 238, 144),
                    new Color(255, 218, 185),
                    new Color(221, 160, 221),
                    new Color(255, 182, 193),
                    new Color(240, 230, 140),
                    new Color(176, 224, 230),
                    new Color(250, 200, 152)
            };

            return palette[hash % palette.length];
        }
    }
}