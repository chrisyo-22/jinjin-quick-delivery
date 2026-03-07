package com.jinjin.service.impl;

import com.jinjin.context.BaseContext;
import com.jinjin.dto.ShoppingCartDTO;
import com.jinjin.entity.Dish;
import com.jinjin.entity.Setmeal;
import com.jinjin.entity.ShoppingCart;
import com.jinjin.mapper.DishMapper;
import com.jinjin.mapper.SetmealMapper;
import com.jinjin.mapper.ShoppingCartMapper;
import com.jinjin.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    @Override
    public void addShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = buildQueryCart(shoppingCartDTO);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);

        if (!shoppingCartList.isEmpty()) {
            ShoppingCart current = shoppingCartList.getFirst();
            current.setNumber(current.getNumber() + 1);
            shoppingCartMapper.updateNumberById(current);
            return;
        }

        if (shoppingCartDTO.getDishId() != null) {
            Dish dish = dishMapper.selectById(shoppingCartDTO.getDishId());
            shoppingCart.setName(dish.getName());
            shoppingCart.setImage(dish.getImage());
            shoppingCart.setAmount(dish.getPrice());
        } else {
            Setmeal setmeal = setmealMapper.getById(shoppingCartDTO.getSetmealId());
            shoppingCart.setName(setmeal.getName());
            shoppingCart.setImage(setmeal.getImage());
            shoppingCart.setAmount(setmeal.getPrice());
        }

        shoppingCart.setNumber(1);
        shoppingCart.setCreateTime(LocalDateTime.now());
        shoppingCartMapper.insert(shoppingCart);
    }

    @Override
    public void subShoppingCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart query = buildQueryCart(shoppingCartDTO);
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(query);
        if (shoppingCartList.isEmpty()) {
            return;
        }

        ShoppingCart current = shoppingCartList.getFirst();
        if (current.getNumber() == 1) {
            shoppingCartMapper.deleteById(current.getId());
            return;
        }

        current.setNumber(current.getNumber() - 1);
        shoppingCartMapper.updateNumberById(current);
    }

    @Override
    public List<ShoppingCart> showShoppingCart() {
        return shoppingCartMapper.list(ShoppingCart.builder().userId(BaseContext.getCurrentId()).build());
    }

    @Override
    public void cleanShoppingCart() {
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());
    }

    private ShoppingCart buildQueryCart(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(BaseContext.getCurrentId());
        return shoppingCart;
    }
}
