package com.fran.entity;

/**
 * @author 程良明
 * @date 2022/3/31
 * * 说明:public.xml 实体
 **/
public class PublicXmlEntity {
    private String type;
    private String name;
    private String id;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "publicXmlEntity{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
