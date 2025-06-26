package com.zspt.blibli.main.controller.vo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryVo {

    @Schema(name = "id", description = "分类ID", example = "1")
    private int categoryId;

    @Schema(name = "name", description = "分类名", example = "学习")
    private String name;

    @Schema(name = "父类ID", description = "父类ID", example = "0")
    private int parentId;

}
