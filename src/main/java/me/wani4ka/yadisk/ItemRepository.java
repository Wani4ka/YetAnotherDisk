package me.wani4ka.yadisk;

import me.wani4ka.yadisk.models.Item;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ItemRepository extends CrudRepository<Item, String> {
    @Query("SELECT i FROM Item i WHERE i.date > :start")
    List<Item> findRecentlyUpdatedItems(Date start);
}
