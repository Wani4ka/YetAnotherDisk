package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "ItemHistory")
public class ItemHistoryUnit {

    @JsonIgnore
    @Id
    @GeneratedValue
    private int id;
    @ManyToOne
    @JsonSerialize(using = ToStringSerializer.class)
    private Item item;
    private String url;
    private String parentId;
    private ItemType type;
    private int size;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Date date;

    protected ItemHistoryUnit() {
    }

    public ItemHistoryUnit(Item item, String url, String parentId, ItemType type, int size, Date date) {
        this.item = item;
        this.url = url;
        this.parentId = parentId;
        this.type = type;
        this.size = size;
        this.date = date;
    }

    public ItemHistoryUnit(Item item) {
        this(item, item.getUrl(), item.getParentId(), item.getType(), item.getSize(), item.getDate());
    }

    public int getId() {
        return id;
    }

    protected void setId(int index) {
        this.id = index;
    }

    @JsonProperty("id")
    public Item getItem() {
        return item;
    }

    protected void setItem(Item id) {
        this.item = id;
    }

    public String getUrl() {
        return url;
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    public String getParentId() {
        return parentId;
    }

    protected void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public ItemType getType() {
        return type;
    }

    protected void setType(ItemType type) {
        this.type = type;
    }

    public int getSize() {
        return size;
    }

    protected void setSize(int size) {
        this.size = size;
    }

    public Date getDate() {
        return date;
    }

    protected void setDate(Date date) {
        this.date = date;
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
