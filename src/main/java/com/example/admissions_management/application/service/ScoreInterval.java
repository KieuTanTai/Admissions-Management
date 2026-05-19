package com.example.admissions_management.application.service;

public record ScoreInterval(double xLowerExclusive,
        double xUpperInclusive,
        double yLowerExclusive,
        double yUpperInclusive) {

    public boolean contains(double score) {
        return score > xLowerExclusive && score <= xUpperInclusive;
    }
}