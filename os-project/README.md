Project Code: C4
Project Title: SJF vs Priority Comparison Project
Team Number: [92]
Repository URL: [https://github.com/Eng-Mosaab/OS-Project]

Team Members

[مصعب هشام ابو المجد امين ] - [20240981]
[يوسف محمد سعيد ] - [20241182]
[هاني تامر سيد محمد ] - [20241094]
[يوسف محمد حسني ] - [20241178]
[يوسف جمال سيد ] - [20241156]
[فتحي هاني فتحي محمد ] - [20240689]


Project Description
This project compares two CPU scheduling algorithms:

Preemptive SJF
Preemptive Priority Scheduling

The program runs both algorithms on the same workload and shows the results in a clear GUI.
It displays separate Gantt charts, separate result tables, a comparison summary, and a final conclusion.

Project Objective
The goal of this project is to compare shortest remaining job selection with urgency based on priority.
It studies how burst time and priority affect execution order, waiting time, turnaround time, response time, fairness, and starvation risk.

Required Inputs
The simulator accepts these values at runtime:

Process ID
Arrival Time
Burst Time
Priority Value

Priority Rule
Smaller priority number means higher priority.

Main Features

GUI interface
Dynamic process input
Input validation
Preemptive SJF scheduling
Preemptive Priority scheduling
Separate Gantt chart for each algorithm
Separate results table for each algorithm
Waiting Time, Turnaround Time, and Response Time for each process
Average WT, Average TAT, and Average RT
Comparison summary
Final conclusion
Built-in test scenarios
CSV export

Input Validation
The simulator rejects invalid input such as:

Negative arrival time
Zero or negative burst time
Duplicate process ID
Negative priority value
Missing required fields
Non-numeric input in numeric fields

Built-in Test Scenarios
The project includes these prepared scenarios:

Basic
Conflict
Starvation Sensitive
Validation Demo

Technologies Used

Java
Java Swing
Object-Oriented Programming

Project Structure
src/
model/
scheduler/
metrics/
gui/
util/

How to Run

Open the project folder in terminal.
Compile the source files.
Run Main.java.

Compile command
src/model/.java
src/scheduler/.java
src/metrics/.java
src/gui/.java
src/util/*.java

Run command
java -cp out Main

Program Output
The program displays:

Process Table
SJF Results Table
Priority Results Table
SJF Gantt Chart
Priority Gantt Chart
Comparison Summary
Final Conclusion

Notes

Both algorithms always use the same dataset.
The comparison is fair because the same workload is used for both algorithms.
The project focuses on efficiency versus urgency.
The project also discusses unfair delay and starvation risk
