package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Entity(name = "ItemHistory")
public class ItemHistoryUnit {

    @JsonIgnore
    @Id
    @GeneratedValue
    @Getter @Setter(AccessLevel.PROTECTED)
    private int id;
    @ManyToOne
    @JsonSerialize(using = ToStringSerializer.class)
    @Getter(onMethod = @__(@JsonProperty("id"))) @Setter(AccessLevel.PROTECTED)
    private Item item;
    @Getter @Setter(AccessLevel.PROTECTED)
    private String url;
    @Getter @Setter(AccessLevel.PROTECTED)
    private String parentId;
    @Getter @Setter(AccessLevel.PROTECTED)
    private ItemType type;
    @Getter @Setter(AccessLevel.PROTECTED)
    private int size;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Getter @Setter(AccessLevel.PROTECTED)
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

    @Data
    public static class Response {
        private final ItemHistoryUnit[] items;
    }
}
