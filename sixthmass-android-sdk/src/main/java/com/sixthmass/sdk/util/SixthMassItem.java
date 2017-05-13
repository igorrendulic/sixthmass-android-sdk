package com.sixthmass.sdk.util;
/*
  Created by Igor Rendulic on 5/1/17.
  
  Copyright 2017 
  
  Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
       http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
    
 */

/**
 * SixtMassItem object is used as a purchasable item in analytics system
 *
 * Convenient constructor with all parameters can be used to reduce code footprint in the SDK users app
 *
 * All parameters are required
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class SixthMassItem {
    /**
     * Item id (can be anything in string format)
     */
    private String id;

    /**
     * Name of the product
     */
    private String name;

    /**
     * Price of the product
     */
    private Double price;

    /**
     * Quantity purchased
     */
    private Integer quantity;

    /**
     * Constructor
     *
     * @param id item id (product id)
     * @param name name of the purchased product (item)
     * @param price price of the purchased product
     * @param quantity quantity
     */
    public SixthMassItem(String id, String name, Double price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * Quantity
     *
     * @return integer
     */
    public Integer getQuantity() {
        return quantity;
    }

    /**
     * Set quantity of purchased item
     *
     * @param quantity quantity
     */
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    /**
     * Getter for id
     *
     * @return String id
     */
    public String getId() {
        return id;
    }

    /**
     * Setter for id
     * @param id string
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Getter for name
     * @return string name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name
     *
     * @param name string name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for price
     *
     * @return double price
     */
    public Double getPrice() {
        return price;
    }

    /**
     * Setter for price
     *
     * @param price double price
     */
    public void setPrice(Double price) {
        this.price = price;
    }
}
