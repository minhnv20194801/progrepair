package org.group10.suspiciouscalculator;

import org.group10.program.Program;

import java.util.Map;

public interface SuspiciousCalculator {
    Map<Integer, Double> calculateScore(Program targetProgram);
}
