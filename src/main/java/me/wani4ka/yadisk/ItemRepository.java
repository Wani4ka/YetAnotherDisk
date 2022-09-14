package me.wani4ka.yadisk;

import me.wani4ka.yadisk.models.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, String> {
}
