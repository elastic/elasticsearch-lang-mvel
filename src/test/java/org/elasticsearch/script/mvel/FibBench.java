/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.elasticsearch.script.mvel;

import org.elasticsearch.common.StopWatch;
import org.elasticsearch.common.metrics.MeanMetric;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.unit.TimeValue;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.lang.String.format;

/**
 *
 */
public class FibBench {

    public static void main(String[] args) {
        recursiveFib(30);
        dynamicFib(30);
    }

    private static void recursiveFib(int n) {
        exec(0, 1, format(Locale.ROOT, "recursive_fib(%s)", n), format(Locale.ROOT, "def fib(n) { if (n == 0 || n == 1) { n } else { fib(n-1) + fib(n-2) } }; return fib(%s);", n));
    }

    private static void dynamicFib(int n) {
        exec(100, 200, format(Locale.ROOT, "dynamic_fib(%s)", n), format(Locale.ROOT, "values = new int[%s]; for (int i = 0; i < %s; i++) { if (i < 2) { values[i] = i; } else { values[i] = values[i-1] + values[i-2]; }} values[%s-1];", n, n, n));
    }

    private static void exec(int warmCount, int avgOver, String name, String script) {
        exec(warmCount, avgOver, name, script, new HashMap<String, Object>());
    }

    private static void exec(int warmCount, int avgOver, String name, String script, Map<String, Object> vars) {
        MvelScriptEngineService se = new MvelScriptEngineService(ImmutableSettings.Builder.EMPTY_SETTINGS);
        for (int i = 0; i < warmCount; i++) {
            Object compiled = se.compile(new String(script));
            se.execute(compiled, vars);
        }
        StopWatch stopWatch = new StopWatch();
        MeanMetric avg = new MeanMetric();
        for (int i = 0; i < avgOver; i++) {
            Object compiled = se.compile(new String(script));
            stopWatch.start();
            se.execute(compiled, vars);
            avg.inc(stopWatch.stop().lastTaskTime().millis());
        }
        System.out.println(format(Locale.ROOT, "[%s] => %s", name, TimeValue.timeValueMillis((long) avg.mean())));
    }

}
