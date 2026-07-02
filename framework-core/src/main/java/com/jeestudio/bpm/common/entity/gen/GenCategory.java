package com.jeestudio.bpm.common.entity.gen;

import com.jeestudio.bpm.common.entity.system.Dict;

import java.util.List;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * @Description: 代码生成分类
 */
@XmlRootElement(name = "category")
public class GenCategory extends Dict {

    private static final long serialVersionUID = 1L;

    private List<String> template;
    private List<String> childTableTemplate;
    public static String CATEGORY_REF = "category-ref:";

    @XmlElement(name = "template")
    public List<String> getTemplate() {
        return this.template;
    }

    public void setTemplate(List<String> template) {
        this.template = template;
    }

    @XmlElementWrapper(name = "childTable")
    @XmlElement(name = "template")
    public List<String> getChildTableTemplate() {
        return this.childTableTemplate;
    }

    public void setChildTableTemplate(List<String> childTableTemplate) {
        this.childTableTemplate = childTableTemplate;
    }
}