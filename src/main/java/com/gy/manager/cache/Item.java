package com.gy.manager.cache;

public class Item {
    private final String key;
    private final int count;

    public Item(String key, int count) {
        this.key = key;
        this.count = count;
    }

    public String getKey() {
        return key;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return "Item{" +
                "key='" + key + '\'' +
                ", count=" + count +
                '}';
    }
}
