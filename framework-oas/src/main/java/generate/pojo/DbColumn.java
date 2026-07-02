package generate.pojo;

import lombok.Data;

/**
 * @Description: 数据库字段元数据
 */
@Data
public class DbColumn {

    private String table_name;
    private String column_name;
    private Integer column_sort;
}
