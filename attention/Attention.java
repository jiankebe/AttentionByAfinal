package com.baidu.vsfinance.util.attention;

/**
 *
 *  @author jianchuanli
 * 
 *
 */

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.tsz.afinal.db.sqlite.DbModel;

import com.baidu.vsfinance.AppContext;
import com.common.util.Tools;

//关注（收藏）功能通用模板类
public class Attention<T> {
    private Map<String, Class<?>> mapFields;
    private List<SyncItem> sync;
    private String tableName;
    private String tableDbName;
    private Class<?> clazz;
    private Class<?> modelDb;
    private List<?> dbList;
    private AttentionInterface attentionInterface;
    private Map<String, Class<?>> dbFields;

    public AttentionInterface getAttentionInterface() {
        return attentionInterface;
    }

    public void setAttentionInterface(AttentionInterface attentionInterface) {
        this.attentionInterface = attentionInterface;
    }

    public Attention(Class<?> modelDb) {
        this.modelDb = modelDb;
        initFields();
    }

    private void initFields() {
        if (dbFields != null) {
            dbFields.clear();
        } else {
            dbFields = new HashMap<String, Class<?>>();
        }
        dbFields = getFieldsMap(modelDb.getDeclaredFields());
        if (itemFields != null) {
            itemFields.clear();
        } else {
            itemFields = new HashMap<String, Class<?>>();
        }
        itemFields = getFieldsMap(SyncItem.class.getDeclaredFields());
    }

    private Map<String, Class<?>> getFieldsMap(Field[] fields) {
        Map<String, Class<?>> map = new HashMap<String, Class<?>>();
        for (Field field : fields) {
            map.put(field.getName(), field.getType());

        }
        return map;
    }

    private void getParams(Field[] fields) {
        if (mapFields == null) {
            mapFields = new HashMap<String, Class<?>>();
        } else {
            mapFields.clear();
        }
        for (Field field : fields) {
            mapFields.put(field.getName(), field.getType());

        }

    }

    public List<T> getDatabaseByWhere(Class<T> clazz, List<String> where) {
        if (where == null || where.size() == 0) {
            return getDataBase(clazz);
        } else {
            checkTable();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < where.size(); i++) {
                buffer.append(" and " + where.get(i));
            }
            List<T> dataList = new ArrayList<T>();
            List<?> datas = AppContext.db.findAllByWhere(modelDb,
                    "isAttention=1");
            if (datas != null && datas.size() > 0) {
                for (int i = 0; i < datas.size(); i++) {
                    try {

                        List<T> temp = (List<T>) (AppContext.db.findAllByWhere(
                                clazz,
                                "fund_code='"
                                        + getDbStringMethod(datas.get(i),
                                                "getFund_code") + "'"
                                        + buffer.toString()));
                        if (temp != null && temp.size() > 0) {
                            dataList.add(temp.get(0));
                        }

                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
            }

            return dataList;
        }

    }

    public List<T> getDatabaseByWhere(Class<T> clazz, String where) {
        if (Tools.isEmpty(where)) {
            return getDataBase(clazz);
        } else {
            checkTable();
            List<T> dataList = new ArrayList<T>();
            List<?> datas = AppContext.db.findAllByWhere(modelDb,
                    "isAttention=1");
            if (datas != null && datas.size() > 0) {
                for (int i = 0; i < datas.size(); i++) {
                    try {

                        List<T> temp = (List<T>) (AppContext.db.findAllByWhere(
                                clazz,
                                "fund_code='"
                                        + getDbStringMethod(datas.get(i),
                                                "getFund_code") + "'" + " and "
                                        + where));
                        if (temp != null && temp.size() > 0) {
                            dataList.add(temp.get(0));
                        }

                    } catch (IndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }

            }
            return dataList;
        }

    }

    public List<T> getDataBase(Class<T> clazz) {
        checkTable();
        List<T> dataList = new ArrayList<T>();
        List<?> datas = AppContext.db.findAllByWhere(modelDb, "isAttention=1");
        if (datas != null && datas.size() > 0) {
            for (int i = 0; i < datas.size(); i++) {
                try {

                    List<T> temp = (List<T>) (AppContext.db.findAllByWhere(
                            clazz,
                            "fund_code='"
                                    + getDbStringMethod(datas.get(i),
                                            "getFund_code") + "'"));
                    if (temp != null && temp.size() > 0) {
                        dataList.add(temp.get(0));
                    }

                } catch (IndexOutOfBoundsException e) {
                    e.printStackTrace();
                }
            }
        }

        return dataList;
    }

    private void checkParams(Field[] fields) throws TheFieldError {
        getParams(fields);
        int i = 0;
        if (mapFields != null) {

            Iterator iter = mapFields.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry entry = (Map.Entry) iter.next();
                Object key = entry.getKey();
                Object val = entry.getValue();
                if (key.equals("isAttention")
                        && (val == Boolean.class || val == boolean.class)) {
                    i++;
                    continue;
                }
                if (key.equals("isChecked")
                        && (val == Boolean.class || val == boolean.class)) {
                    i++;
                    continue;
                }
                if (key.equals("isEdit")
                        && (val == Boolean.class || val == boolean.class)) {
                    i++;
                    continue;
                }
                if (key.equals("fund_code") && (val == String.class)) {
                    i++;
                    continue;
                }
            }

            if (i == 4) {
                return;
            } else {
                throw new TheFieldError("对象属性设置错误");
            }
        } else {
            throw new TheFieldError("对象属性设置错误");
        }

    }

    private void setValue(Method set, Object receiver, Class<?> clazz,
            Object value) {
        try {
            if (clazz == String.class) {
                set.invoke(receiver, value.toString());
            } else if (clazz == int.class || clazz == Integer.class) {
                set.invoke(
                        receiver,
                        value == null ? (Integer) null : Integer.parseInt(value
                                .toString()));
            } else if (clazz == float.class || clazz == Float.class) {
                set.invoke(
                        receiver,
                        value == null ? (Float) null : Float.parseFloat(value
                                .toString()));
            } else if (clazz == double.class || clazz == Double.class) {
                set.invoke(
                        receiver,
                        value == null ? (Double) null : Double
                                .parseDouble(value.toString()));
            } else if (clazz == long.class || clazz == Long.class) {
                set.invoke(
                        receiver,
                        value == null ? (Long) null : Long.parseLong(value
                                .toString()));
            } else if (clazz == boolean.class || clazz == Boolean.class) {
                set.invoke(
                        receiver,
                        value == null ? (Boolean) false : Boolean.valueOf(value
                                .toString()));
            } else if (clazz == Object.class) {
                set.invoke(receiver, value == null ? (Object) null
                        : new Object());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addDbModelFieldValue(Object obj, T attention)
            throws NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {
        Map<String, Class<?>> attentionFields = getFieldsMap(attention
                .getClass().getDeclaredFields());
        Map<String, Class<?>> fieldsMap = attentionInterface.createDbModel();
        Iterator<String> fields = fieldsMap.keySet().iterator();
        Iterator<Class<?>> type = fieldsMap.values().iterator();
        if (fieldsMap != null && fieldsMap.size() > 0) {
            while (fields.hasNext() && type.hasNext()) {
                String fieldName = fields.next();
                Class<?> fieldType = type.next();
                if (attentionFields.containsKey(fieldName)
                        && attentionFields.containsValue(fieldType)
                        && dbFields.containsKey(fieldName)
                        && dbFields.containsValue(fieldType)) {
                    String set = "set"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    String get = "get"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    Method getMethod = attention.getClass().getDeclaredMethod(
                            get);
                    Method setMethod = modelDb
                            .getDeclaredMethod(set, fieldType);
                    setValue(setMethod, obj, fieldType,
                            getMethod.invoke(attention));

                }

            }
        }
        fieldsMap.clear();
        attentionFields.clear();

    }

    private void changeKey(T attention) throws TheFieldError {

        try {
            updateModel(attention);
            saveOrUpdate(attention, getStringMethod(attention, "getFund_code"));
            Method setMethod;
            Method setModelMethod;
            Method getMethod;
            clazz = attention.getClass();
            Object obj = modelDb.newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {

                String fn = field.getName();
                String set = "set" + fn.substring(0, 1).toUpperCase()
                        + fn.substring(1);
                String get = "get" + fn.substring(0, 1).toUpperCase()
                        + fn.substring(1);
                if (fn.equals("isAttention")) {
                    getMethod = clazz.getDeclaredMethod(get);
                    setMethod = clazz.getDeclaredMethod(set, field.getType());
                    setModelMethod = modelDb.getDeclaredMethod(set,
                            field.getType());
                    if (getMethod.invoke(attention) != null) {
                        Boolean tmp = ((Boolean) getMethod.invoke(attention));
                        setValue(setMethod, attention, field.getType(), !tmp);
                        setModelMethod.invoke(obj, !tmp);
                    } else {
                        setValue(setMethod, attention, field.getType(), false);
                        setModelMethod.invoke(obj, false);
                    }
                } else if (fn.equals("isEdit")) {
                    getMethod = clazz.getDeclaredMethod(get);
                    setMethod = clazz.getDeclaredMethod(set, field.getType());
                    setModelMethod = modelDb.getDeclaredMethod(set,
                            field.getType());
                    if (getMethod.invoke(attention) != null) {
                        Boolean tmp = ((Boolean) getMethod.invoke(attention));
                        setValue(setMethod, attention, field.getType(), !tmp);
                        setModelMethod.invoke(obj, !tmp);
                    } else {
                        setValue(setMethod, attention, field.getType(), false);
                        setModelMethod.invoke(obj, false);
                    }
                } else if (fn.equals("fund_code")) {
                    getMethod = clazz.getDeclaredMethod(get);
                    setMethod = clazz.getDeclaredMethod(set, field.getType());
                    setModelMethod = modelDb.getDeclaredMethod(set,
                            field.getType());
                    if (getMethod.invoke(attention) != null) {
                        setValue(setMethod, attention, field.getType(),
                                ((String) getMethod.invoke(attention)));
                        setModelMethod.invoke(obj,
                                ((String) getMethod.invoke(attention)));
                    } else {
                        setValue(setMethod, attention, field.getType(), "");
                        setModelMethod.invoke(obj, "");
                    }
                } else if (fn.equals("isChecked")) {
                    getMethod = clazz.getDeclaredMethod(get);
                    setMethod = clazz.getDeclaredMethod(set, field.getType());
                    setModelMethod = modelDb.getDeclaredMethod(set,
                            field.getType());
                    if (getMethod.invoke(attention) != null) {
                        Boolean tmp = ((Boolean) getMethod.invoke(attention));
                        setValue(setMethod, attention, field.getType(), tmp);
                        setModelMethod.invoke(obj, tmp);
                    } else {
                        setValue(setMethod, attention, field.getType(), false);
                        setModelMethod.invoke(obj, false);
                    }
                }

            }
            if (attentionInterface != null) {
                addDbModelFieldValue(obj, attention);
            } else {
                setDbStringMethod(obj, "setVendor_id", getStringMethod(attention, "getVendor_id"));
            }
            getMethod = clazz.getDeclaredMethod("getFund_code");

            saveOrUpdateDb(obj, (String) getMethod.invoke(attention));

        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private void checkModle(Object dbObject) {
        try {
            if (dbObject == null)

                dbObject = modelDb.newInstance();

            else {
                Class<?> clazz;
                clazz = dbObject.getClass();

                Field[] fields = clazz.getDeclaredFields();
                try {
                    checkParams(fields);

                } catch (TheFieldError e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (InstantiationException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IllegalAccessException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

    }

    public void attention(T attention) {
        try {
            Object dbObject = modelDb.newInstance();
            checkModle(dbObject);
            clazz = attention.getClass();
            tableName = clazz.getSimpleName();
            tableDbName = tableName + "Db";
            changeKey(attention);
            // launchGetAttentionRequest(attention);
        } catch (TheFieldError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    Map<String, Class<?>> itemFields;

    private void addSyncFieldValue(SyncItem item, Object attention)
            throws NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Map<String, Class<?>> attentionFields = getFieldsMap(attention
                .getClass().getDeclaredFields());
        Map<String, Class<?>> fieldsMap = attentionInterface.createDbModel();
        Iterator<String> fields = fieldsMap.keySet().iterator();
        Iterator<Class<?>> type = fieldsMap.values().iterator();
        if (fieldsMap != null && fieldsMap.size() > 0) {
            while (fields.hasNext() && type.hasNext()) {
                String fieldName = fields.next();
                Class<?> fieldType = type.next();
                if (attentionFields.containsKey(fieldName)
                        && attentionFields.containsValue(fieldType)
                        && itemFields.containsKey(fieldName)
                        && itemFields.containsValue(fieldType)) {
                    String set = "set"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    String get = "get"
                            + fieldName.substring(0, 1).toUpperCase()
                            + fieldName.substring(1);
                    Method getMethod = attention.getClass().getDeclaredMethod(
                            get);
                    Method setMethod = item.getClass().getDeclaredMethod(set,
                            fieldType);
                    setValue(setMethod, item, fieldType,
                            getMethod.invoke(attention));

                }

            }
        }
        fieldsMap.clear();
        attentionFields.clear();
    }

    private Map<String, SyncItem> map = new HashMap<String, SyncItem>();

    private List<SyncItem> initAttentionDatas() {
        map.clear();
        dbList = AppContext.db.findAllByWhere(modelDb, "isEdit=" + "1");

        sync = new ArrayList<SyncItem>();
        if (dbList == null || dbList.size() == 0) {
            // dbList = new ArrayList<T>();
            // SyncItem item = new SyncItem();
            // sync.add(item);
        } else {
            try {

                for (int i = 0; i < dbList.size(); i++) {
                    SyncItem item = new SyncItem();
                    Class<?> clazz;
                    clazz = dbList.get(i).getClass();
                    Method getFund_code = clazz
                            .getDeclaredMethod("getFund_code");
                    Method getIsChecked = clazz
                            .getDeclaredMethod("getIsChecked");
                    item.setFund_code((String) getFund_code.invoke(dbList
                            .get(i)));
                    if (attentionInterface != null) {
                        addSyncFieldValue(item, dbList.get(i));
                    } else {
                        Method getVendor_id = clazz
                                .getDeclaredMethod("getVendor_id");

                        item.setVendor_id((String) getVendor_id.invoke(dbList
                                .get(i)));
                    }

                    if ((Boolean) getIsChecked.invoke(dbList.get(i))) {
                        item.setIs_delete("1");
                    }

                    else {
                        item.setIs_delete("0");
                    }
                    if (!map.containsKey(item.getFund_code())) {
                        map.put(item.getFund_code(), item);
                        sync.add(item);
                    }

                }

            } catch (SecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return sync;
    }

    private void checkTable() {
        if (Tools.isEmpty(tableDbName) || Tools.isEmpty(tableName)) {
            tableDbName = modelDb.getSimpleName();
            tableName = tableDbName.substring(0, tableDbName.length() - 2);
        }
    }

    private void saveOrUpdate(T attention, String fund_code) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableName + " where fund_code='" + fund_code + "'",
                attention.getClass());
        if (dbModel != null) {
            AppContext.db.update(attention, "fund_code='" + fund_code + "'");
        } else {
            AppContext.db.save(attention);
        }
    }

    private void saveOrUpdateDb(Object modelDb, String fund_code) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableDbName + " where fund_code='" + fund_code + "'",
                modelDb.getClass());
        if (dbModel == null) {
            AppContext.db.save(modelDb);
        } else {
            AppContext.db.update(modelDb, "fund_code='" + fund_code + "'");
        }
    }

    private void setDbStringMethod(Object obj, String method, String flag) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method setMethod;
                setMethod = obj.getClass().getDeclaredMethod(method,
                        String.class);
                setMethod.invoke(obj, flag);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);

        }
    }

    private void setStringMethod(T obj, String method, String flag) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method setMethod;
                setMethod = obj.getClass().getDeclaredMethod(method,
                        String.class);
                setMethod.invoke(obj, flag);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);

        }
    }

    private void setDbBooleanMethod(Object obj, String method, Boolean flag) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method setMethod;
                setMethod = obj.getClass().getDeclaredMethod(method,
                        Boolean.class);
                setMethod.invoke(obj, flag);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);

        }
    }

    @SuppressWarnings("null")
    private void setBooleanMethod(T obj, String method, Boolean flag) {
        Object object = null;
        {
            try {
                if (obj == null) {
                    object = obj.getClass().newInstance();
                    Method setMethod;
                    setMethod = object.getClass().getDeclaredMethod(method,
                            Boolean.class);
                    setMethod.invoke(object, flag);
                }

                else {
                    Method setMethod;
                    setMethod = obj.getClass().getDeclaredMethod(method,
                            Boolean.class);
                    setMethod.invoke(obj, flag);
                }

            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InstantiationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);

        }
    }

    private String getDbStringMethod(Object obj, String method) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method getMethod;
                getMethod = obj.getClass().getDeclaredMethod(method);
                return (String) getMethod.invoke(obj);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);
            return "";
        }
    }

    private String getStringMethod(T obj, String method) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method getMethod;
                getMethod = obj.getClass().getDeclaredMethod(method);
                return (String) getMethod.invoke(obj);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);
            return "";
        }
    }

    private Boolean getDbBooleanMethod(Object obj, String method) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method getMethod;
                getMethod = obj.getClass().getDeclaredMethod(method);
                return (Boolean) getMethod.invoke(obj);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);
            return false;
        }
    }

    private Boolean getBooleanMethod(Object obj, String method) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method getMethod;
                getMethod = obj.getClass().getDeclaredMethod(method);
                return (Boolean) getMethod.invoke(obj);
            } catch (NoSuchMethodException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // setValue(set, receiver, obj, value);
            return false;
        }
    }

    public List<SyncItem> getSyncAllData() {
        checkTable();
        return initAttentionDatas();
    }

    @SuppressWarnings("unchecked")
    public void updateModel(T attention) {
        if (attention == null)
            throw new NullPointerException("对象初始化错误");
        List<T> funds = (List<T>) AppContext.db.findAllByWhere(modelDb,
                "fund_code='" + getStringMethod(attention, "getFund_code")
                        + "'");
        if (funds != null && funds.size() > 0) {
            setBooleanMethod(attention, "setIsEdit",
                    getBooleanMethod(funds.get(0), "getIsEdit"));
            setBooleanMethod(attention, "setIsChecked",
                    getBooleanMethod(funds.get(0), "getIsChecked"));
            setBooleanMethod(attention, "setIsAttention",
                    getBooleanMethod(funds.get(0), "getIsAttention"));
        } else {
            setBooleanMethod(attention, "setIsEdit", false);
            setBooleanMethod(attention, "setIsChecked", false);
            setBooleanMethod(attention, "setIsAttention", false);
            // AppContext.db.save(attention);
        }
    }

    public Object updateModel(Class<?> clazz, String str) {
        List<?> list = AppContext.db.findAllByWhere(clazz, "fund_code='" + str
                + "'");
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }

        /*
         * if (attention == null) throw new NullPointerException("对象初始化错误"); List<T> funds = (List<T>)
         * AppContext.db.findAllByWhere(modelDb, "fund_code='" + getStringMethod(attention, "getFund_code") + "'"); if
         * (funds != null && funds.size() > 0) { setBooleanMethod(attention, "setIsEdit", getBooleanMethod(funds.get(0),
         * "getIsEdit")); setBooleanMethod(attention, "setIsChecked", getBooleanMethod(funds.get(0), "getIsChecked"));
         * setBooleanMethod(attention, "setIsAttention", getBooleanMethod(funds.get(0), "getIsAttention")); } else {
         * setBooleanMethod(attention, "setIsEdit", false); setBooleanMethod(attention, "setIsChecked", false);
         * setBooleanMethod(attention, "setIsAttention", false); AppContext.db.save(attention); }
         */
    }

    public void updateData() {
        updateDatabase();
    }

    public void updateData(List<T> newDatas) {

        updateDatabase(newDatas);
    }

    private void updateDatabase() {
        try {
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {

                    // Object obj;
                    setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    setDbBooleanMethod(dbList.get(i), "setIsChecked",
                            !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    saveOrUpdateDb(dbList.get(i),
                            getDbStringMethod(dbList.get(i), "getFund_code"));

                }
            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void saveDataBase(List<T> newDatas) {

        if (newDatas == null) {
            return;
        }
        try {
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {
                    setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    setDbBooleanMethod(dbList.get(i), "setIsChecked",
                            !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    saveOrUpdateDb(dbList.get(i),
                            getDbStringMethod(dbList.get(i), "getFund_code"));
                }

            }

            // List<?> modelDbs = AppContext.db.findAll(modelDb);
            // if (modelDbs != null) {
            // for (int i = 0; i < modelDbs.size(); i++) {
            //
            // AppContext.db.deleteByWhere(
            // modelDb,
            // "fund_code='"
            // + getDbStringMethod(modelDbs.get(i),
            // "getFund_code") + "'");
            //
            // }
            // }

            for (int i = 0; i < newDatas.size(); i++) {
                Object obj = modelDb.newInstance(); // newModelDb(obj); T
                T attention = newDatas.get(i);
                saveOrUpdate(attention,
                        getStringMethod(attention, "getFund_code"));
                setDbBooleanMethod(obj, "setIsAttention", true);
                setDbBooleanMethod(obj, "setIsEdit", false);
                setDbBooleanMethod(obj, "setIsChecked", true);
                setDbStringMethod(obj, "setFund_code",
                        getStringMethod(attention, "getFund_code"));

                setDbStringMethod(obj, "setVendor_id",
                        getStringMethod(attention, "getVendor_id"));
                saveOrUpdateDb(obj, getStringMethod(attention, "getFund_code"));

            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void updateData(List<T> newDatas, boolean isDelete) {
        updateDatabase(newDatas, isDelete);

    }
    
    public void deleteAll() {
        List<?> modelDbs = AppContext.db.findAll(modelDb);
        if (modelDbs != null) {
            for (int i = 0; i < modelDbs.size(); i++) {

                AppContext.db.deleteByWhere(
                        modelDb,
                        "fund_code='"
                                + getDbStringMethod(modelDbs.get(i),
                                        "getFund_code") + "'");

            }
        }
    }

    private void updateDatabase(List<T> newDatas, boolean isSave) {
        if (isSave) {
            try {
                if (dbList != null && dbList.size() > 0) {
                    for (int i = 0; i < dbList.size(); i++) {
                        setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                        setDbBooleanMethod(dbList.get(i), "setIsChecked",
                                !getBooleanMethod(dbList.get(i), "getIsChecked"));
                        saveOrUpdateDb(dbList.get(i),
                                getDbStringMethod(dbList.get(i), "getFund_code"));
                    }

                }

                List<?> modelDbs = AppContext.db.findAll(modelDb);
                if (modelDbs != null) {
                    for (int i = 0; i < modelDbs.size(); i++) {

                        AppContext.db.deleteByWhere(
                                modelDb,
                                "fund_code='"
                                        + getDbStringMethod(modelDbs.get(i),
                                                "getFund_code") + "'");

                    }
                }

            } catch (IllegalArgumentException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            updateDatabase(newDatas);
        }

    }

    private void updateDatabase(List<T> newDatas) {
        if (newDatas == null) {
            updateDatabase();
            return;
        }
        try {
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {
                    setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    setDbBooleanMethod(dbList.get(i), "setIsChecked",
                            !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    saveOrUpdateDb(dbList.get(i),
                            getDbStringMethod(dbList.get(i), "getFund_code"));
                }

            }

            List<?> modelDbs = AppContext.db.findAll(modelDb);
            if (modelDbs != null) {
                for (int i = 0; i < modelDbs.size(); i++) {

                    AppContext.db.deleteByWhere(
                            modelDb,
                            "fund_code='"
                                    + getDbStringMethod(modelDbs.get(i),
                                            "getFund_code") + "'");

                }
            }

            for (int i = 0; i < newDatas.size(); i++) {
                Object obj = modelDb.newInstance(); // newModelDb(obj); T
                T attention = newDatas.get(i);
                saveOrUpdate(attention,
                        getStringMethod(attention, "getFund_code"));
                setDbBooleanMethod(obj, "setIsAttention", true);
                setDbBooleanMethod(obj, "setIsEdit", false);
                setDbBooleanMethod(obj, "setIsChecked", true);
                setDbStringMethod(obj, "setFund_code",
                        getStringMethod(attention, "getFund_code"));

                setDbStringMethod(obj, "setVendor_id",
                        getStringMethod(attention, "getVendor_id"));
                saveOrUpdateDb(obj, getStringMethod(attention, "getFund_code"));

            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InstantiationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
