package com.jinjin.service.impl;

import com.jinjin.context.BaseContext;
import com.jinjin.entity.AddressBook;
import com.jinjin.mapper.AddressBookMapper;
import com.jinjin.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    @Override
    public List<AddressBook> list(AddressBook addressBook) {
        return addressBookMapper.list(addressBook);
    }

    @Override
    public void save(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(0);
        addressBookMapper.insert(addressBook);
    }

    @Override
    public AddressBook getById(Long id) {
        return addressBookMapper.getById(id);
    }

    @Override
    public void update(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.update(addressBook);
    }

    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        AddressBook reset = new AddressBook();
        reset.setUserId(BaseContext.getCurrentId());
        reset.setIsDefault(0);
        addressBookMapper.updateIsDefaultByUserId(reset);

        addressBook.setUserId(BaseContext.getCurrentId());
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    @Override
    public void deleteById(Long id) {
        addressBookMapper.deleteById(id);
    }

    @Override
    public AddressBook getDefault() {
        AddressBook query = new AddressBook();
        query.setUserId(BaseContext.getCurrentId());
        query.setIsDefault(1);
        List<AddressBook> list = addressBookMapper.list(query);
        return list.isEmpty() ? null : list.getFirst();
    }
}
