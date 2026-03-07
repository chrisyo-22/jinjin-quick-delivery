package com.jinjin.controller.user;

import com.jinjin.context.BaseContext;
import com.jinjin.entity.AddressBook;
import com.jinjin.result.Result;
import com.jinjin.service.AddressBookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/addressBook")
@Tag(name = "User address book related interfaces")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    @GetMapping("/list")
    @Operation(summary = "List current user addresses")
    public Result<List<AddressBook>> list() {
        AddressBook addressBook = new AddressBook();
        addressBook.setUserId(BaseContext.getCurrentId());
        return Result.success(addressBookService.list(addressBook));
    }

    @PostMapping
    @Operation(summary = "Create address")
    public Result<String> save(@RequestBody AddressBook addressBook) {
        addressBookService.save(addressBook);
        return Result.success();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get address by id")
    public Result<AddressBook> getById(@PathVariable Long id) {
        return Result.success(addressBookService.getById(id));
    }

    @PutMapping
    @Operation(summary = "Update address")
    public Result<String> update(@RequestBody AddressBook addressBook) {
        addressBookService.update(addressBook);
        return Result.success();
    }

    @PutMapping("/default")
    @Operation(summary = "Set default address")
    public Result<String> setDefault(@RequestBody AddressBook addressBook) {
        addressBookService.setDefault(addressBook);
        return Result.success();
    }

    @DeleteMapping
    @Operation(summary = "Delete address by id")
    public Result<String> deleteById(Long id) {
        addressBookService.deleteById(id);
        return Result.success();
    }

    @GetMapping("/default")
    @Operation(summary = "Get default address")
    public Result<AddressBook> getDefault() {
        AddressBook addressBook = addressBookService.getDefault();
        if (addressBook == null) {
            return Result.error("No default address found");
        }
        return Result.success(addressBook);
    }
}
