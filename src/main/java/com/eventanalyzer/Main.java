package com.eventanalyzer;

import com.eventanalyzer.core.AnalyzerApplication;
import com.eventanalyzer.core.AnalyzerFactory;

/** Command-line entry point. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        int exitCode = new AnalyzerApplication(new AnalyzerFactory(), System.out, System.err).run(args);
        System.exit(exitCode);
    }
}
