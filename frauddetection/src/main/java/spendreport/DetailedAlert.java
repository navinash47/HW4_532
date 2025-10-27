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

/**
 * DetailedAlert class that extends the basic Alert with additional fraud information.
 * This class includes account ID, timestamp, zip code, and amount information
 * for enhanced fraud detection reporting.
 */
public class DetailedAlert {
    
    private Long accountId;
    private long timestamp;
    private String zipCode;
    private double amount;
    
    /**
     * Default constructor
     */
    public DetailedAlert() {
    }
    
    /**
     * Constructor with all fields
     * @param accountId the account ID where fraud was detected
     * @param timestamp the timestamp when fraud was detected
     * @param zipCode the zip code where the fraudulent activity occurred
     * @param amount the amount of the fraudulent transaction
     */
    public DetailedAlert(Long accountId, long timestamp, String zipCode, double amount) {
        this.accountId = accountId;
        this.timestamp = timestamp;
        this.zipCode = zipCode;
        this.amount = amount;
    }
    
    /**
     * Get the account ID where fraud was detected
     * @return the account ID
     */
    public Long getAccountId() {
        return accountId;
    }
    
    /**
     * Set the account ID where fraud was detected
     * @param accountId the account ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    /**
     * Get the timestamp when fraud was detected
     * @return the timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the timestamp when fraud was detected
     * @param timestamp the timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the zip code where the fraudulent activity occurred
     * @return the zip code
     */
    public String getZipCode() {
        return zipCode;
    }
    
    /**
     * Set the zip code where the fraudulent activity occurred
     * @param zipCode the zip code
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    /**
     * Get the amount of the fraudulent transaction
     * @return the amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Set the amount of the fraudulent transaction
     * @param amount the amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    @Override
    public String toString() {
        return "DetailedAlert{" +
                "accountId=" + accountId +
                ", timestamp=" + timestamp +
                ", zipCode='" + zipCode + '\'' +
                ", amount=" + amount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DetailedAlert that = (DetailedAlert) o;
        
        if (timestamp != that.timestamp) return false;
        if (Double.compare(that.amount, amount) != 0) return false;
        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        return zipCode != null ? zipCode.equals(that.zipCode) : that.zipCode == null;
    }
    
    @Override
    public int hashCode() {
        int result = accountId != null ? accountId.hashCode() : 0;
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        long temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
