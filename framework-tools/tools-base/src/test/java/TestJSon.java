import com.alibaba.fastjson.annotation.JSONField;
import com.jeestudio.tools.base.utils.JSONHelper;

import java.util.Date;

/**
 * //TODO description
 * 2023年04月04日 16:06:00
 *
 * @author U-002
 */
public class TestJSon {

    static class pojo {
        private Date buildDate;

        @JSONField(format = "yyyy-MM-dd")
        public Date getBuildDate() {
            return buildDate;
        }

        public void setBuildDate(Date buildDate) {
            this.buildDate = buildDate;
        }
    }

    public static void main(String[] args) {
        pojo pojo = new pojo();
        pojo.setBuildDate(new Date());
        System.out.println(JSONHelper.toJSONObject(pojo));
    }
}
