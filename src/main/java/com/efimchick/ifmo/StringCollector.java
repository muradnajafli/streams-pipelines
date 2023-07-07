package com.efimchick.ifmo;

import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.stream.Collectors;

public class StringCollector {
    private static final String STUDENT = "Student";
    private static final String TOTAL = "Total";
    private static final String MARK = "Mark";
    private static final String AVERAGE = "Average";

    private static final String SEPARATOR = " | ";
    private static final String END = " |";
    private int widthFirstColumn = STUDENT.length() + 1;
    private static String pattern;
    List<CourseResult> courseResults = new ArrayList<>();
    List<String> taskNames = new ArrayList<>();
    List<String> students = new ArrayList<>();
    Collecting collecting = new Collecting();

    public void buildResult(StringBuilder builder) {
        buildHeader(builder);
        builderStudentRows(builder);
        builderLastRows(builder);
    }

    private void buildHeader(StringBuilder builder) {
        students = getStudents(courseResults);
        widthFirstColumn = getLengthName(students);
        pattern = "%-" + widthFirstColumn + "s";
        builder.append(String.format(pattern, STUDENT)).append(SEPARATOR);
        taskNames = getTaskNames(courseResults);
        builder.append(String.join(SEPARATOR, taskNames))
                .append(SEPARATOR).append(TOTAL)
                .append(SEPARATOR).append(MARK)
                .append(END).append("\n");

    }

    private void builderStudentRows(StringBuilder builder) {
        Map<Person, Double> totalScores = collecting.totalScores(courseResults.stream());
        Map<Person, String> marks = collecting.defineMarks(courseResults.stream());
        builder.append(students.stream()
                .map(s -> {
                    StringBuilder result = new StringBuilder()
                            .append(String.format(pattern, s)).append(SEPARATOR)
                            .append(taskNames.stream()
                                    .map(t -> String.format("%" + t.length() + "s",
                                            findTaskResult(s, t)))
                                    .collect(Collectors.joining(SEPARATOR)))
                            .append(SEPARATOR)
                            .append(String.format("%.2f", getTotalOfStudent(s, totalScores)).replace(",", "."))
                            .append(SEPARATOR)
                            .append("   ").append(getMarkOfStudent(s, marks)).append(END);
                    return result.toString();
                }).collect(Collectors.joining("\n"))).append("\n");
    }

    private void builderLastRows(StringBuilder builder) {
        Map<String, Double> averageScores = collecting.averageScoresPerTask(courseResults.stream());
        double sum = collecting.averageTotalScore(courseResults.stream());
        builder.append(String.format(pattern, AVERAGE)).append(SEPARATOR)
                .append(taskNames.stream()
                        .map(s -> String.format("%" + s.length() + ".2f", averageScores.get(s)).replace(",", "."))
                        .collect(Collectors.joining(SEPARATOR)))
                .append(SEPARATOR)
                .append(String.format("%.2f", sum).replace(",", "."))
                .append(SEPARATOR)
                .append("   ")
                .append(collecting.scoreAndMarks.entrySet().stream()
                        .filter(v -> v.getKey() <= sum)
                        .map(Map.Entry::getValue)
                        .findFirst().orElse("F"))
                .append(END);

    }



    private List<String> getTaskNames(List<CourseResult> courseResults) {
        return courseResults
                .stream()
                .flatMap(t -> t.getTaskResults().keySet().stream())
                .distinct().sorted()
                .collect(Collectors.toList());
    }

    private List<String> getStudents(List<CourseResult> courseResults) {
        return courseResults
                .stream()
                .map(CourseResult::getPerson)
                .distinct().sorted(Comparator.comparing(Person::getLastName))
                .map(s -> s.getLastName() + " " + s.getFirstName())
                .collect(Collectors.toList());
    }

    private int getLengthName(List<String> students) {
        return Math.max(widthFirstColumn, students.stream().mapToInt(String::length)
                .max().orElse(widthFirstColumn));
    }

    public void addCourseResult(CourseResult courseResult) {
        courseResults.add(courseResult);
    }

    private int findTaskResult(String student, String taskName) {
        return courseResults.stream()
                .filter(s -> (s.getPerson().getLastName() + " " + s.getPerson().getFirstName()).equals(student))
                .flatMap(s -> s.getTaskResults().entrySet().stream())
                .filter(t -> (t.getKey().equals(taskName)))
                .mapToInt(Map.Entry::getValue)
                .findAny().orElse(0);
    }

    private double getTotalOfStudent(String student, Map<Person, Double> totalScores) {
        return totalScores.entrySet().stream()
                .filter(s -> (s.getKey().getLastName() + " " + s.getKey().getFirstName()).equals(student))
                .mapToDouble(Map.Entry::getValue).findAny().orElse(0);
    }

    private String getMarkOfStudent(String student, Map<Person, String> marks) {
        return marks.entrySet().stream()
                .filter(s -> (s.getKey().getLastName() + " " + s.getKey().getFirstName()).equals(student))
                .map(Map.Entry::getValue).findAny().orElse("F");
    }


}
