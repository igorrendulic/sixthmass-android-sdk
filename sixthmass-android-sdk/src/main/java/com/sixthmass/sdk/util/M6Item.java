package com.sixthmass.sdk.util;
/*
  Created by Igor Rendulic on 4/26/17.
  
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Purchased item object
 *
 * This object is only for internals of SixthMass SDK. It's not advised to access or modify it.
 *
 * @author Igor Rendulic
 * @version 2017.0504
 * @since 0.0.1
 */
public final class M6Item implements Serializable {

    /**
     * User defined ID for the purchased item
     */
    public String id; // ID

    /**
     * Namme of the produt
     */
    public String n; // name

    /**
     * Price of the product
     */
    public Double pr; // price

    /**
     * Quantity purchased
     */
    public Integer q; // quantity

    public M6Item(SixthMassItem item) {
        this.id = item.getId();
        this.n = item.getName();
        this.pr = item.getPrice();
        this.q = item.getQuantity();
    }

    /**
     * Converts this object to Json
     *
     * @return Json object
     * @throws JSONException Exception when conversion failed
     */
    protected JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("n", this.n);
        json.put("id", this.id);
        json.put("pr", this.pr);
        json.put("q", this.q);
        return json;
    }
}
