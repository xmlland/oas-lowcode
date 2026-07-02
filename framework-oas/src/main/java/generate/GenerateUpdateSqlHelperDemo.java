package generate;

/**
 * @Description: 数据库增量更新SQL生成示例
 */
public class GenerateUpdateSqlHelperDemo {
    static String url = "jdbc:mysql://ip:port/db?serverTimezone=Asia/Shanghai&characterEncoding=utf8";
    static String username = "dbuser";
    static String password = "dbpassword";


    static String savePath = "D:/temp/sql/update/";

    public static void main(String[] args) {
        String tableNames = "oa_word_template,gen_table,gen_table_column,sys_dictionary,sys_datapermission,sys_datarole_datapermission,sys_datarole,sys_menu,sys_role,sys_role_datarole,sys_role_menu,sys_subsystem,sys_subsystem_menu,oa_task_setting,oa_task_permission";
        GenerateUpdateSqlHelper.generate(url, username, password, savePath, tableNames);
    }

}
