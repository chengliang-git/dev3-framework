package com.guanwei.framework.controller;

import com.guanwei.framework.common.result.Result;
import com.guanwei.framework.generator.CodeGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 代码生成器控制器
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Tag(name = "代码生成器", description = "代码生成相关接口")
@RestController
@RequestMapping("/generator")
public class GeneratorController {

    @Autowired
    private CodeGenerator codeGenerator;

    @Operation(summary = "生成代码", description = "根据数据库表生成对应的实体类、Mapper、Service、Controller等代码")
    @PostMapping("/generate")
    public Result<String> generateCode(@RequestBody GenerateRequest request) {
        try {
            codeGenerator.generateCode(request.getTableNames());
            return Result.success("代码生成成功");
        } catch (Exception e) {
            return Result.error("代码生成失败: " + e.getMessage());
        }
    }

    /**
     * 代码生成请求参数
     */
    public static class GenerateRequest {
        private String[] tableNames;

        public String[] getTableNames() {
            return tableNames;
        }

        public void setTableNames(String[] tableNames) {
            this.tableNames = tableNames;
        }
    }
}