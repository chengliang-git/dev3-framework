package com.guanwei.framework.generator;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import com.baomidou.mybatisplus.generator.fill.Column;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 代码生成器
 *
 * @author Enterprise Framework
 * @since 1.0.0
 */
@Component
public class CodeGenerator {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private GeneratorProperties generatorProperties;

    /**
     * 生成代码
     *
     * @param tableNames 表名列表
     */
    public void generateCode(String... tableNames) {
        AutoGenerator generator = new AutoGenerator(getDataSourceConfig());

        generator.global(getGlobalConfig());
        generator.packageInfo(getPackageConfig());
        generator.strategy(getStrategyConfig(tableNames));
        generator.template(getTemplateConfig());

        generator.execute();
    }

    /**
     * 数据源配置
     */
    private DataSourceConfig getDataSourceConfig() {
        try {
            return new DataSourceConfig.Builder(
                    dataSource.getConnection().getMetaData().getURL(),
                    dataSource.getConnection().getMetaData().getUserName(),
                    null).build();
        } catch (Exception e) {
            throw new RuntimeException("获取数据源配置失败", e);
        }
    }

    /**
     * 全局配置
     */
    private GlobalConfig getGlobalConfig() {
        return new GlobalConfig.Builder()
                .outputDir(System.getProperty("user.dir") + "/src/main/java")
                .author(generatorProperties.getAuthor())
                .enableSwagger()
                .dateType(DateType.TIME_PACK)
                .commentDate("yyyy-MM-dd HH:mm:ss")
                .build();
    }

    /**
     * 包配置
     */
    private PackageConfig getPackageConfig() {
        return new PackageConfig.Builder()
                .parent(generatorProperties.getPackageName())
                .entity("entity")
                .mapper("mapper")
                .service("service")
                .serviceImpl("service.impl")
                .controller("controller")
                .xml("mapper.xml")
                .build();
    }

    /**
     * 策略配置
     */
    private StrategyConfig getStrategyConfig(String... tableNames) {
        StrategyConfig.Builder builder = new StrategyConfig.Builder();

        if (tableNames != null && tableNames.length > 0) {
            builder.addInclude(tableNames);
        }

        // 表前缀过滤
        if (generatorProperties.getTablePrefix() != null) {
            builder.addTablePrefix(generatorProperties.getTablePrefix());
        }

        // 实体策略配置
        builder.entityBuilder()
                .enableLombok()
                .enableTableFieldAnnotation()
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .idType(IdType.ASSIGN_UUID)
                .addTableFills(
                        new Column("createTime", FieldFill.INSERT),
                        new Column("modifyTime", FieldFill.INSERT_UPDATE),
                        new Column("creator", FieldFill.INSERT),
                        new Column("modifier", FieldFill.INSERT_UPDATE))
                .enableRemoveIsPrefix()
                .logicDeleteColumnName("delFlag")
                .logicDeletePropertyName("delFlag");

        // 控制器策略配置
        builder.controllerBuilder()
                .enableRestStyle()
                .enableHyphenStyle();

        // 服务策略配置
        builder.serviceBuilder()
                .formatServiceFileName("%sService")
                .formatServiceImplFileName("%sServiceImpl");

        // Mapper策略配置
        builder.mapperBuilder()
                .enableMapperAnnotation()
                .enableBaseResultMap()
                .enableBaseColumnList();

        return builder.build();
    }

    /**
     * 模板配置
     */
    private TemplateConfig getTemplateConfig() {
        return new TemplateConfig.Builder()
                .entity("/templates/entity.java")
                .service("/templates/service.java")
                .serviceImpl("/templates/serviceImpl.java")
                .mapper("/templates/mapper.java")
                .xml("/templates/mapper.xml")
                .controller("/templates/controller.java")
                .build();
    }

    /**
     * 代码生成器配置属性
     */
    @Component
    @ConfigurationProperties(prefix = "framework.generator")
    public static class GeneratorProperties {
        private String author = "Guanwei Framework";
        private String packageName = "com.guanwei.framework";
        private String tablePrefix = "t_";
        private List<String> excludeColumns = Arrays.asList("id", "createTime", "modifyTime", "creator", "modifier",
                "delFlag", "orderNo");

        public String getAuthor() {
            return author;
        }

        public void setAuthor(String author) {
            this.author = author;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getTablePrefix() {
            return tablePrefix;
        }

        public void setTablePrefix(String tablePrefix) {
            this.tablePrefix = tablePrefix;
        }

        public List<String> getExcludeColumns() {
            return excludeColumns;
        }

        public void setExcludeColumns(List<String> excludeColumns) {
            this.excludeColumns = excludeColumns;
        }
    }
}