package com.jinjin.service;

import com.jinjin.vo.BusinessDataVO;
import com.jinjin.vo.DishOverViewVO;
import com.jinjin.vo.OrderOverViewVO;
import com.jinjin.vo.SetmealOverViewVO;

import java.time.LocalDateTime;

public interface WorkspaceService {

    BusinessDataVO getBusinessData(LocalDateTime begin, LocalDateTime end);

    OrderOverViewVO getOrderOverView();

    DishOverViewVO getDishOverView();

    SetmealOverViewVO getSetmealOverView();
}
