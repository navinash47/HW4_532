/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package spendreport;

import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

public class DetailedFraudDetector extends KeyedProcessFunction<Long, DetailedTransaction, DetailedAlert> {

    private static final long serialVersionUID = 1L;

    private static final double SMALL_AMOUNT = 10.00;
    private static final double LARGE_AMOUNT = 500.00;
    private static final long ONE_MINUTE = 60 * 1000;

    private transient ValueState<Boolean> flagState;
    private transient ValueState<Long> timerState;
    private transient ValueState<String> zipCodeState;

    @Override
    public void open(Configuration parameters) {
        ValueStateDescriptor<Boolean> flagDescriptor = new ValueStateDescriptor<>(
            "flag",
            Boolean.class);
        flagState = getRuntimeContext().getState(flagDescriptor);

        ValueStateDescriptor<Long> timerDescriptor = new ValueStateDescriptor<>(
            "timer-state",
            Long.class);
        timerState = getRuntimeContext().getState(timerDescriptor);
        
        ValueStateDescriptor<String> zipCodeDescriptor = new ValueStateDescriptor<>(
            "zip-code-state",
            String.class);
        zipCodeState = getRuntimeContext().getState(zipCodeDescriptor);
    }

    @Override
    public void processElement(
            DetailedTransaction transaction,
            Context context,
            Collector<DetailedAlert> collector) throws Exception {

        double amount = transaction.getAmount();
        String zipCode = transaction.getZipCode();
        
        Boolean flag = flagState.value();
        String storedZipCode = zipCodeState.value();
        
        if (amount < SMALL_AMOUNT) {
            flagState.update(true);
            zipCodeState.update(zipCode);
            
            long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
            context.timerService().registerProcessingTimeTimer(timer);
            timerState.update(timer);
        }
        
        if (amount >= LARGE_AMOUNT) {
            if (flag != null && flag) {
                if (storedZipCode != null && storedZipCode.equals(zipCode)) {
                    DetailedAlert alert = new DetailedAlert(
                        transaction.getAccountId(),
                        transaction.getTimestamp(),
                        zipCode,
                        amount
                    );
                    collector.collect(alert);
                }
                
                flagState.clear();
                zipCodeState.clear();
                Long timer = timerState.value();
                if (timer != null) {
                    context.timerService().deleteProcessingTimeTimer(timer);
                    timerState.clear();
                }
            }
        }
    }

    @Override
    public void onTimer(long timestamp, OnTimerContext ctx, Collector<DetailedAlert> out) throws Exception {
        flagState.clear();
        zipCodeState.clear();
        timerState.clear();
    }
}