package me.wani4ka.yadisk.repositories;

import me.wani4ka.yadisk.models.Item;
import org.springframework.data.repository.CrudRepository;

import java.util.Date;
import java.util.List;

public interface ItemRepository extends CrudRepository<Item, String> {
    List<Item> findItemsByDateAfter(Date start);
}
