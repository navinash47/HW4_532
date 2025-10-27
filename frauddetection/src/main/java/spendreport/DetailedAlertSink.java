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

import org.apache.flink.streaming.api.functions.sink.SinkFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DetailedAlertSink class that logs DetailedAlert instances using slf4j logging.
 * This sink receives DetailedAlert objects from the fraud detection stream
 * and logs them with detailed information including account ID, timestamp, zip code, and amount.
 */
public class DetailedAlertSink implements SinkFunction<DetailedAlert> {
    
    private static final Logger logger = LoggerFactory.getLogger(DetailedAlertSink.class);
    
    @Override
    public void invoke(DetailedAlert alert, Context context) throws Exception {
        // Log the detailed alert information
        logger.info("FRAUD DETECTED - Account: {}, Timestamp: {}, Zip: {}, Amount: ${}", 
                   alert.getAccountId(), 
                   alert.getTimestamp(), 
                   alert.getZipCode(), 
                   alert.getAmount());
    }
}
