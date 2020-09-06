package vn.com.watanabe.etl.step.plugin.xml_transform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BaseSelectList {
  // private static Class<?> PKG = XMLTranformData.class; // for i18n purposes,
  // needed by Translator2!!
  // public static String PREFIX_TAG="XMLTransform";
  private List<Item> items;

  public BaseSelectList(List<Item> items) {
    this.items = items;
  }

  public BaseSelectList() {
    this.items = new ArrayList<Item>();
  }

  public BaseSelectList(Item[] items) {
    this.items = new ArrayList<Item>();
    for(int i =0; i< items.length;i++){
      this.items.add(items[i]);
    }
  }

  public Item getByName(String checkValue) {
    if (checkValue != null) {
      for (Item e : items) {
        if (e.value.equalsIgnoreCase(checkValue) || e.oldValue.equalsIgnoreCase(checkValue)) {
          return e;
        }
      }
    }
    return null;
  }

  public Item getByDesc(String checkValue) {
    if (checkValue != null) {
      for (Item e : items) {
        if (e.desc.equalsIgnoreCase(checkValue)) {
          return e;
        }
      }
    }
    return null;
  }

  public Item getById(int id) {
    if (items.size() > 0) {
      if (id < 0 || id >= items.size()) {
        return items.get(0);
      } else {
        return items.get(id);
      }
    } else {
      return null;
    }

  }



  public String[] getItemDescs() {
    String[] desc = new String[items.size()];
    for(int i =0 ; i<items.size();i++){
      desc[i] = items.get(i).desc;
    }
    return desc;
  }

  public static class Item {
    private int id;
    private String value;
    private String oldValue;
    private String desc;

    
    Item(String value, String oldValue, String desc) {
      this.value = value;
      this.oldValue = oldValue;
      this.desc = desc;
    }

    public String toString() {
      return this.value;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getOldValue() {
      return oldValue;
    }

    public void setOldValue(String oldValue) {
      this.oldValue = oldValue;
    }

    public void setDesc(String desc) {
      this.desc = desc;
    }

    public String getDesc() {
      return desc;
    }

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public Item(int id, String value, String oldValue, String desc) {
      this.id = id;
      this.value = value;
      this.oldValue = oldValue;
      this.desc = desc;
    }
  }

}