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
import org.elasticsearch.script.ExecutableScript;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SimpleBench {

    public static void main(String[] args) {
        long[] results = new long[3];
        MeanMetric[] avgs = new MeanMetric[] {
                new MeanMetric(),
                new MeanMetric(),
                new MeanMetric()
        };

        for (int i = 0; i < 100; i++) {
            testIteration(results);
            avgs[0].inc(results[0]);
            avgs[1].inc(results[1]);
            avgs[2].inc(results[2]);
        }
        System.out.println("Execute Took: " + avgs[0].mean() + "ms");
        System.out.println("Execute Took: " + avgs[1].mean() + "ms");
        System.out.println("Execute Took: " + avgs[2].mean() + "ms");
    }

    public static void testIteration(long[] results) {
        MvelScriptEngineService se = new MvelScriptEngineService(ImmutableSettings.Builder.EMPTY_SETTINGS);
        Object compiled = se.compile("x + y");

        Map<String, Object> vars = new HashMap<String, Object>();
        // warm up
        for (int i = 0; i < 1000; i++) {
            vars.put("x", i);
            vars.put("y", i + 1);
            se.execute(compiled, vars);
        }

        final long ITER = 100000;

        StopWatch stopWatch = new StopWatch().start();
        for (long i = 0; i < ITER; i++) {
            se.execute(compiled, vars);
        }
        results[0] = stopWatch.stop().lastTaskTime().millis();

        stopWatch = new StopWatch().start();
        ExecutableScript executableScript = se.executable(compiled, vars);
        for (long i = 0; i < ITER; i++) {
            executableScript.run();
        }
        results[1] = stopWatch.stop().lastTaskTime().millis();

        stopWatch = new StopWatch().start();
        executableScript = se.executable(compiled, vars);
        for (long i = 0; i < ITER; i++) {
            for (Map.Entry<String, Object> entry : vars.entrySet()) {
                executableScript.setNextVar(entry.getKey(), entry.getValue());
            }
            executableScript.run();
        }
        results[2] = stopWatch.stop().lastTaskTime().millis();
    }
}
