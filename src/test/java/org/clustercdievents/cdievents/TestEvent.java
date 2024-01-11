/*
 * Copyright 2024 Mart Somermaa
 * SPDX-License-Identifier: Apache-2.0
 */

package org.clustercdievents.cdievents;

import java.util.Objects;

@Clustered
public class TestEvent {

    private String data;

    public TestEvent() {
    }

    public TestEvent(String data) {
        this.data = data;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestEvent testEvent = (TestEvent) o;
        return Objects.equals(data, testEvent.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

}
