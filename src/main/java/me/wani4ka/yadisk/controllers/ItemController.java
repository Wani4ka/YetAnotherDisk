package me.wani4ka.yadisk.controllers;

import me.wani4ka.yadisk.exceptions.ValidationFailedException;
import me.wani4ka.yadisk.exceptions.ItemNotFoundException;
import me.wani4ka.yadisk.models.ApiResult;
import me.wani4ka.yadisk.models.Item;
import me.wani4ka.yadisk.models.ItemHistoryUnit;
import me.wani4ka.yadisk.models.ItemImport;
import me.wani4ka.yadisk.services.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@RestController
public class ItemController {

    @Autowired
    private ItemService itemService;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    @GetMapping("/nodes/{id}")
    public Item getItem(@PathVariable String id) throws ItemNotFoundException {
        return itemService.getItem(id);
    }

    @PostMapping("/imports")
    public ApiResult importsPost(@Valid @RequestBody ItemImport.Request body) throws ValidationFailedException {
        itemService.importItems(body.getItems(), body.getUpdateDate());
        return ApiResult.OK;
    }

    @DeleteMapping("/delete/{id}")
    public ApiResult delete(@PathVariable String id, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date) throws ItemNotFoundException {
        itemService.deleteItem(itemService.getItem(id), date);
        return ApiResult.OK;
    }

    @GetMapping("/updates")
    public ItemHistoryUnit.Response getUpdates(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Date date) {
        return new ItemHistoryUnit.Response(itemService.getRecentlyChangedFiles(date));
    }

    @GetMapping("/node/{id}/history")
    public ItemHistoryUnit.Response getHistory(@PathVariable String id, @RequestParam(required = false) String dateStart, @RequestParam(required = false) String dateEnd) throws ParseException, ItemNotFoundException {
        Date from = null, to = dateEnd == null ? new Date() : sdf.parse(dateEnd);
        if (dateStart != null)
            from = sdf.parse(dateStart);
        return new ItemHistoryUnit.Response(itemService.getChangesHistory(itemService.getItem(id), from, to));
    }
}
