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
import org.apache.flink.walkthrough.common.entity.Alert;
import org.apache.flink.walkthrough.common.entity.Transaction;
import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.configuration.Configuration;

/**
 * Skeleton code for implementing a fraud detector.
 */
public class FraudDetector extends KeyedProcessFunction<Long, Transaction, Alert> {

	private static final long serialVersionUID = 1L;

	private static final double SMALL_AMOUNT = 1.00;
	private static final double LARGE_AMOUNT = 500.00;
	private static final long ONE_MINUTE = 60 * 1000;

	private transient ValueState<Boolean> flagState;
	private transient ValueState<Long> timerState;

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
	}

	@Override
	public void processElement(
			Transaction transaction,
			Context context,
			Collector<Alert> collector) throws Exception {

		// Get the current transaction amount
		double amount = transaction.getAmount();
		
		// Get the current flag state
		Boolean flag = flagState.value();
		
		// Check if this is a small transaction
		if (amount < SMALL_AMOUNT) {
			// Set the flag to true and start a timer
			flagState.update(true);
			
			long timer = context.timerService().currentProcessingTime() + ONE_MINUTE;
			context.timerService().registerProcessingTimeTimer(timer);
			timerState.update(timer);
		}
		
		// Check if this is a large transaction
		if (amount > LARGE_AMOUNT) {
			// Check if we had a small transaction before this
			if (flag != null && flag) {
				// This is fraudulent! Small transaction followed by large transaction
				Alert alert = new Alert();
				alert.setId(transaction.getAccountId());
				collector.collect(alert);
				
				// Clear the flag and cancel the timer
				flagState.clear();
				Long timer = timerState.value();
				if (timer != null) {
					context.timerService().deleteProcessingTimeTimer(timer);
					timerState.clear();
				}
			}
		}
	}

	@Override
	public void onTimer(long timestamp, OnTimerContext ctx, Collector<Alert> out) throws Exception {
		// Timer fired - this means we had a small transaction 1 minute ago
		// If we get here, it means no large transaction followed the small one
		// Clear the flag since the time window has expired
		flagState.clear();
		timerState.clear();
	}
}
