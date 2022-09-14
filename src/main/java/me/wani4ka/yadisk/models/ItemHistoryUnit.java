package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public class ItemHistoryUnit {
    private final String id;
    private final String url;
    private final String parentId;
    private final ItemType type;
    private final int size;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private final Date date;

    public ItemHistoryUnit(String id, String url, String parentId, ItemType type, int size, Date date) {
        this.id = id;
        this.url = url;
        this.parentId = parentId;
        this.type = type;
        this.size = size;
        this.date = date;
    }

    public ItemHistoryUnit(Item item) {
        this(item.getId(), item.getUrl(), item.getParentId(), item.getType(), item.getSize(), item.getDate());
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getParentId() {
        return parentId;
    }

    public ItemType getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public Date getDate() {
        return date;
    }

    public static class Response {
        private final ItemHistoryUnit[] items;
        public Response(ItemHistoryUnit[] items) {
            this.items = items;
        }

        public ItemHistoryUnit[] getItems() {
            return items;
        }
    }
}
