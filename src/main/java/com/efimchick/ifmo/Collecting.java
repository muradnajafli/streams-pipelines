package com.efimchick.ifmo;


import com.efimchick.ifmo.util.CourseResult;
import com.efimchick.ifmo.util.Person;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Collecting {
    public Map<Double, String> scoreAndMarks;

    {
        scoreAndMarks = new LinkedHashMap<>();
        scoreAndMarks.put(90.01, "A");
        scoreAndMarks.put(83.0, "B");
        scoreAndMarks.put(75.0, "C");
        scoreAndMarks.put(68.0, "D");
        scoreAndMarks.put(60.0, "E");
        scoreAndMarks.put(0.0, "F");
    }

    public int sum(IntStream intStream) {
        return intStream.sum();

    }

    public int production(IntStream intStream) {
        return intStream.reduce(1, (a, b) -> a * b);
    }

    public int oddSum(IntStream intStream) {
        return intStream.filter(x -> x % 2 != 0).sum();
    }

    public Map<Integer, Integer> sumByRemainder(int divider, IntStream intStream) {
        return intStream
                .boxed()
                .collect(Collectors.groupingBy(x -> x % divider,
                Collectors.summingInt(x -> x)));
    }

    public Map<Person, Double> totalScores(Stream<CourseResult> results) {
        List<CourseResult> courseResults = results.collect(Collectors.toList());

        return courseResults.stream()
                .collect(Collectors.toMap(CourseResult::getPerson, r -> r.getTaskResults()
                        .values().stream()
                        .mapToInt(Integer::intValue)
                        .sum() / (double) getCountTasks(courseResults)));
    }
    private long getCountTasks (List<CourseResult> courseResults) {
        return courseResults.stream()
                .flatMap(r -> r.getTaskResults()
                        .keySet().stream())
                .distinct().count();
    }
    private long getCountPeople (List<CourseResult> courseResults) {
        return courseResults.stream()
                .map(CourseResult::getPerson)
                .distinct().count();
    }

    public double averageTotalScore(Stream<CourseResult> results) {
        List<CourseResult> courseResults = results.collect(Collectors.toList());

        return courseResults.stream().flatMap(result -> result.getTaskResults().values().stream())
                .mapToDouble(Integer::intValue)
                .sum() / (getCountPeople(courseResults) * getCountTasks(courseResults));

    }

    public Map<String, Double> averageScoresPerTask(Stream<CourseResult> results) {
        List<CourseResult> courseResults = results.collect(Collectors.toList());

        return courseResults.stream()
                .flatMap(result -> result.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey,
                        Collectors.summingDouble(d -> d.getValue() / (double) getCountPeople(courseResults))));
    }

    public Map<Person, String> defineMarks(Stream<CourseResult> results) {
        List<CourseResult> courseResults = results.collect(Collectors.toList());

        return courseResults.stream().collect(Collectors.toMap(CourseResult::getPerson,
                courseResult ->
                {
                    double score = courseResult.getTaskResults().values().stream()
                            .mapToDouble(Double::valueOf)
                            .sum() / getCountTasks(courseResults);
                    return scoreAndMarks.entrySet().stream()
                            .filter(v -> v.getKey() <= score)
                            .map(Map.Entry::getValue)
                            .findFirst().orElse("F");
                }
        ));
    }


    public String easiestTask(Stream<CourseResult> results) {
        List<CourseResult> courseResults = results.collect(Collectors.toList());

        return courseResults.stream()
                .flatMap(courseResult -> courseResult.getTaskResults().entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Nothing found");

    }
    public Collector<CourseResult, ?, String> printableStringCollector() {
        Collector collector = new Collector() {
            @Override
            public Supplier supplier() {
                return StringCollector::new;
            }

            @Override
            public BiConsumer <StringCollector, CourseResult> accumulator() {
                return StringCollector::addCourseResult;
            }

            @Override
            public BinaryOperator combiner() {
                return null;
            }

            @Override
            public Function <StringCollector, String> finisher() {
                return specialPrint -> {StringBuilder builder = new StringBuilder();
                    specialPrint.buildResult(builder);
                    return builder.toString();
                };
            }

            @Override
            public Set<Characteristics> characteristics() {
                return Collections.emptySet();
            }
        };
        return collector;
    }
}

