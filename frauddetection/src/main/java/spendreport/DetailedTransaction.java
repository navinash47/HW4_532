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
 * DetailedTransaction class that extends the basic Transaction with zip code information.
 * This class includes all the standard transaction fields (accountId, amount, timestamp)
 * plus additional zip code information for enhanced fraud detection.
 */
public class DetailedTransaction {
    
    private Long accountId;
    private double amount;
    private long timestamp;
    private String zipCode;
    
    /**
     * Default constructor
     */
    public DetailedTransaction() {
    }
    
    /**
     * Constructor with all fields
     * @param accountId the account ID
     * @param amount the transaction amount
     * @param timestamp the transaction timestamp
     * @param zipCode the zip code where the transaction occurred
     */
    public DetailedTransaction(Long accountId, double amount, long timestamp, String zipCode) {
        this.accountId = accountId;
        this.amount = amount;
        this.timestamp = timestamp;
        this.zipCode = zipCode;
    }
    
    /**
     * Get the account ID
     * @return the account ID
     */
    public Long getAccountId() {
        return accountId;
    }
    
    /**
     * Set the account ID
     * @param accountId the account ID
     */
    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }
    
    /**
     * Get the transaction amount
     * @return the transaction amount
     */
    public double getAmount() {
        return amount;
    }
    
    /**
     * Set the transaction amount
     * @param amount the transaction amount
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    /**
     * Get the transaction timestamp
     * @return the transaction timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }
    
    /**
     * Set the transaction timestamp
     * @param timestamp the transaction timestamp
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    /**
     * Get the zip code where the transaction occurred
     * @return the zip code
     */
    public String getZipCode() {
        return zipCode;
    }
    
    /**
     * Set the zip code where the transaction occurred
     * @param zipCode the zip code
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
    
    @Override
    public String toString() {
        return "DetailedTransaction{" +
                "accountId=" + accountId +
                ", amount=" + amount +
                ", timestamp=" + timestamp +
                ", zipCode='" + zipCode + '\'' +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        
        DetailedTransaction that = (DetailedTransaction) o;
        
        if (Double.compare(that.amount, amount) != 0) return false;
        if (timestamp != that.timestamp) return false;
        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        return zipCode != null ? zipCode.equals(that.zipCode) : that.zipCode == null;
    }
    
    @Override
    public int hashCode() {
        int result = accountId != null ? accountId.hashCode() : 0;
        long temp = Double.doubleToLongBits(amount);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        return result;
    }
}
