package com.unique.rule.check.common.util;



import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;
import org.apache.ibatis.type.JdbcType;

import java.util.ArrayList;
import java.util.Collections;

/**
 * mybatis-plus 代码生成器
 */
public class CodeUtils {

    public static void main(String[] args) {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("rule_expression");
        strings.add("rule_config");
        strings.add("rule_tip");
        for (String tableName : strings) {
            genecode(tableName, "com.unique.rule.check");
        }
    }


    public static void genecode(String tableName, String filePath) {
        FastAutoGenerator.create("jdbc:mysql://localhost:3306/rule_check?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai", "root", "hadoop")
                .globalConfig(builder -> {
                    builder.author("haohaounique@163.com") // 设置作者
//                            .enableSwagger() // 开启 swagger 模式
//                            .fileOverride() // 覆盖已生成文件 已过时到strategyConfig 中去配置
                            .dateType(DateType.TIME_PACK)
                            .commentDate("yyyy-MM-dd HH:mm:ss")
                            .outputDir("D:\\code\\rule_check\\src\\main\\java"); // 指定输出目录
                }).dataSourceConfig(builder -> {
                    builder.typeConvertHandler((globalConfig, typeRegistry, metaInfo) -> {
                        if (JdbcType.BIT==metaInfo.getJdbcType()) {
                            // 自定义类型转换
                            return DbColumnType.INTEGER;
                        }
                        if (JdbcType.TINYINT == metaInfo.getJdbcType()) {
                            return DbColumnType.INTEGER;
                        }
                        if (JdbcType.SMALLINT == metaInfo.getJdbcType()) {
                            return DbColumnType.INTEGER;
                        }
                        return typeRegistry.getColumnType(metaInfo);
                    });
                })
                .packageConfig(builder -> {
                    builder.parent(filePath) // 设置父包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, "D:\\code\\rule_check\\src\\main\\resources\\mapper")); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude(tableName).entityBuilder()
                            .enableLombok(). enableFileOverride()
                            .serviceBuilder()
                            .mapperBuilder().entityBuilder().enableFileOverride()
                            .controllerBuilder().enableRestStyle()
                    ;
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}