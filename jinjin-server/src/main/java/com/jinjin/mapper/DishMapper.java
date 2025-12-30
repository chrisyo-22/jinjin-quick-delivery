package com.jinjin.mapper;

import com.jinjin.anno.AutoFill;
import com.jinjin.entity.Dish;
import com.jinjin.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DishMapper {

    /**
     * 根据分类id查询菜品数量
     *
     * @param categoryId
     * @return
     */
    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Long categoryId);

    void update(Dish dish);


    //    @Options(useGeneratedKeys = true, keyProperty = "id") use only here or the xml, do not use together
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);
}
