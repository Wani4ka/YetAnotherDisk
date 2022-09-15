package me.wani4ka.yadisk.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ItemImport {

    @NotNull
    private final String id;
    private final String url;
    private final String parentId;
    @NotNull
    private final ItemType type;
    private final Integer size;

    public ItemImport(String id, String url, String parentId, ItemType type, Integer size) {
        this.id = id;
        this.url = url;
        this.parentId = parentId;
        this.type = type;
        this.size = size;
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

    public Integer getSize() {
        return size;
    }

    public boolean isValid() {
        ItemType type = getType();
        if ((type == ItemType.FOLDER) != (getUrl() == null))
            return false;
        if (type == ItemType.FILE && getUrl().length() > 255)
            return false;
        if (type == ItemType.FOLDER && getSize() != null)
            return false;
        return type != ItemType.FILE || getSize() > 0;
    }

    @Data
    public static class Request {
        @NotNull
        private ItemImport[] items;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
        @NotNull
        private Date updateDate;

        public Request(ItemImport[] items, Date updateDate) {
            this.items = items;
            this.updateDate = updateDate;
        }

        public Request() {}

        public static class Builder {
            private final List<ItemImport> items = new ArrayList<>();

            private Builder addImport(ItemImport req) {
                items.add(req);
                return this;
            }

            public Builder addFolder(String id, String parentId) {
                return addImport(new ItemImport(id, null, parentId, ItemType.FOLDER, null));
            }

            public Builder addFile(String id, String url, String parentId, int size) {
                return addImport(new ItemImport(id, url, parentId, ItemType.FILE, size));
            }

            public Request build() {
                return new Request(items.toArray(ItemImport[]::new), new Date());
            }
        }
    }
}
