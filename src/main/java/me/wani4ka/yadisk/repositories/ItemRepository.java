package me.wani4ka.yadisk.repositories;

import me.wani4ka.yadisk.models.Item;
import org.springframework.data.repository.CrudRepository;

public interface ItemRepository extends CrudRepository<Item, String> {
}
