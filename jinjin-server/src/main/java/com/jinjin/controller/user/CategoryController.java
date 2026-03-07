package com.jinjin.controller.user;

import com.jinjin.entity.Category;
import com.jinjin.result.Result;
import com.jinjin.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userCategoryController")
@RequestMapping("/user/category")
@Tag(name = "User category related interfaces")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/list")
    @Operation(summary = "List categories by type")
    public Result<List<Category>> list(Integer type) {
        return Result.success(categoryService.list(type));
    }
}
