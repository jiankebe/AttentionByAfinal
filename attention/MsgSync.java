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

import com.baidu.sapi2.SapiAccountManager;
import com.baidu.vsfinance.AppContext;
import com.baidu.vsfinance.models.MsgSyncItem;
import com.common.perference.BasePerference;
import com.common.util.Tools;

//关注（收藏）功能通用模板类
public class MsgSync<T> {
    private Map<String, Class<?>> mapFields;
    private List<MsgSyncItem> sync;
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

    public MsgSync(Class<?> modelDb) {
        this.modelDb = modelDb;
        initFields();
        checkTable();
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
        itemFields = getFieldsMap(MsgSyncItem.class.getDeclaredFields());
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
                                "id='"
                                        + getDbStringMethod(datas.get(i),
                                                "getId") + "'"
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
                                "id='"
                                        + getDbStringMethod(datas.get(i),
                                                "getId") + "'" + " and "
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
                            "id='" + getDbStringMethod(datas.get(i), "getId")
                                    + "'"));
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
                if (key.equals("id") && (val == String.class)) {
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

    /**
     * 添加更多的存储字段
     * 
     * @param obj 数据库存储对象
     * @param attention 关注对象
     * 
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
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

    /**
     * 改变对象关注状态信息
     * 
     * @param attention
     * @throws TheFieldError
     */
    private void changeKey(T attention) throws TheFieldError {

        try {
            updateModel(attention);
            saveOrUpdate(attention, getStringMethod(attention, "getId"));
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
                        if (!tmp) {
                            setValue(setMethod, attention, field.getType(), !tmp);
                            setModelMethod.invoke(obj, !tmp);
                        }
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
                        if (!tmp) {
                            setValue(setMethod, attention, field.getType(), !tmp);
                            setModelMethod.invoke(obj, !tmp);
                        }

                    } else {
                        setValue(setMethod, attention, field.getType(), false);
                        setModelMethod.invoke(obj, false);
                    }
                } else if (fn.equals("id")) {
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
                } else if (fn.equals("is_public")) {
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
                        setValue(setMethod, attention, field.getType(), "1");
                        setModelMethod.invoke(obj, "1");
                    }

                }

            }
            if (attentionInterface != null) {
                addDbModelFieldValue(obj, attention);
            } else {
                // setDbStringMethod(obj, "setVendor_id",
                // getStringMethod(attention, "getVendor_id"));
            }
            getMethod = clazz.getDeclaredMethod("getId");

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

    private void checkModel(Object dbObject) {
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
            checkTable();
            Object dbObject = modelDb.newInstance();
            checkModel(dbObject);
            if (clazz != attention.getClass()) {
                clazz = attention.getClass();
            }
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

    private void addSyncFieldValue(MsgSyncItem item, Object attention)
            throws NoSuchMethodException, IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Map<String, Class<?>> attentionFields = getFieldsMap(attention
                .getClass().getDeclaredFields());
        Map<String, Class<?>> fieldsMap = attentionInterface.createSyncModel();
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

    private Map<String, MsgSyncItem> map = new HashMap<String, MsgSyncItem>();

    private StringBuffer initMsgStrings() {
        StringBuffer buffer = new StringBuffer();
        map.clear();
        dbList = AppContext.db.findAllByWhere(modelDb, "isEdit=" + "1");

        sync = new ArrayList<MsgSyncItem>();
        if (dbList == null || dbList.size() == 0) {
            // dbList = new ArrayList<T>();
            // MsgSyncItem item = new MsgSyncItem();
            // sync.add(item);
        } else {
            try {

                for (int i = 0; i < dbList.size(); i++) {
                    MsgSyncItem item = new MsgSyncItem();
                    Class<?> clazz;
                    clazz = dbList.get(i).getClass();
                    Method getId = clazz.getDeclaredMethod("getId");
                    Method getIsChecked = clazz
                            .getDeclaredMethod("getIsChecked");
                    item.setId((String) getId.invoke(dbList.get(i)));
                    // if (attentionInterface != null) {
                    // addSyncFieldValue(item, dbList.get(i));
                    // } else {
                    // // Method getVendor_id = clazz
                    // // .getDeclaredMethod("getVendor_id");
                    // //
                    // // item.((String) getVendor_id.invoke(dbList
                    // // .get(i)));
                    // }

                    if (!(Boolean) getIsChecked.invoke(dbList.get(i))
                            && !Tools.isEmpty(getDbStringMethod(dbList.get(i), "getIs_public"))
                            && getDbStringMethod(dbList.get(i), "getIs_public").equals("0")) {
                        if (!map.containsKey(item.getId())) {
                            map.put(item.getId(), item);
                            buffer.append("," + item.getId());
                        }
                    }
                    // else {
                    // item.setIs_read("0");
                    // }

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
        if (buffer.length() > 1) {
            buffer.delete(0, 1);
        }

        return buffer;
    }

    private List<MsgSyncItem> initAttentionDatas() {
        StringBuffer buffer = new StringBuffer();
        map.clear();
        dbList = AppContext.db.findAllByWhere(modelDb, "isEdit=" + "1" + " and " + "is_public='1'");

        sync = new ArrayList<MsgSyncItem>();
        if (dbList == null || dbList.size() == 0) {
            dbList = new ArrayList<T>();
            MsgSyncItem item = new MsgSyncItem();
            sync.add(item);
        } else {
            try {

                for (int i = 0; i < dbList.size(); i++) {
                    MsgSyncItem item = new MsgSyncItem();
                    Class<?> clazz;
                    clazz = dbList.get(i).getClass();
                    Method getId = clazz.getDeclaredMethod("getId");
                    Method getIsChecked = clazz
                            .getDeclaredMethod("getIsChecked");
                    item.setId((String) getId.invoke(dbList.get(i)));
                    if (attentionInterface != null) {
                        addSyncFieldValue(item, dbList.get(i));
                    } else {
                        // Method getVendor_id = clazz
                        // .getDeclaredMethod("getVendor_id");
                        //
                        // item.((String) getVendor_id.invoke(dbList
                        // .get(i)));
                    }

                    if (!(Boolean) getIsChecked.invoke(dbList.get(i))) {
                        item.setIs_read("1");
                        if (!map.containsKey(item.getId())) {
                            map.put(item.getId(), item);

                            sync.add(item);
                            buffer.append("," + item.getId());
                        }
                    }
                    // else {
                    // item.setIs_read("0");
                    // }

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

    private void saveOrUpdate(T attention, String id) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableName + " where id='" + id + "'", attention.getClass());
        if (dbModel != null) {

            AppContext.db.update(attention, "id='" + id + "'");
        } else {
            AppContext.db.save(attention);
        }
    }

    private void saveOrUpdateDb(Object modelDb, String id) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableDbName + " where id='" + id + "'", modelDb.getClass());
        if (dbModel == null) {
            AppContext.db.save(modelDb);
        } else {
            AppContext.db.update(modelDb, "id='" + id + "'");
        }
    }

    private void deleteDb(Class<?> modelDb, String id) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableDbName + " where id='" + id + "'", modelDb);
        if (dbModel != null) {
            AppContext.db.deleteByWhere(modelDb, "id='" + id + "'");
        }

    }

    private void delete(T attention, String id) {
        checkTable();
        DbModel dbModel = AppContext.db.findDbModelBySQL("select * from "
                + tableName + " where id='" + id + "'", attention.getClass());
        if (dbModel != null) {
            AppContext.db.deleteByWhere(attention.getClass(), "id='" + id + "'");
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

    private long getLongMethod(T obj, String method) {
        if (obj == null)
            throw new NullPointerException("实例化对象错误");
        else {
            try {
                Method getMethod;
                getMethod = obj.getClass().getDeclaredMethod(method);
                return (Long) getMethod.invoke(obj);
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
            return 0;
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

    public List<MsgSyncItem> getSyncAllData() {
        checkTable();
        return initAttentionDatas();
    }

    public StringBuffer getSyncStrings() {
        checkTable();
        return initMsgStrings();
    }

    @SuppressWarnings("unchecked")
    public void updateModel(T attention) {

        if (attention == null)
            throw new NullPointerException("对象初始化错误");
        List<T> funds = (List<T>) AppContext.db.findAllByWhere(modelDb, "id='"
                + getStringMethod(attention, "getId") + "'");
        if (funds != null && funds.size() > 0) {

            setBooleanField(attention, getBooleanMethod(funds.get(0), "getIsAttention"),
                    getBooleanMethod(funds.get(0), "getIsEdit"), getBooleanMethod(funds.get(0), "getIsChecked"), false);

            // setBooleanMethod(attention, "setIsEdit",
            // getBooleanMethod(funds.get(0), "getIsEdit"));
            // setBooleanMethod(attention, "setIsChecked",
            // getBooleanMethod(funds.get(0), "getIsChecked"));
            // setBooleanMethod(attention, "setIsAttention",
            // getBooleanMethod(funds.get(0), "getIsAttention"));
        } else {
            // setBooleanMethod(attention, "setIsEdit", false);
            // setBooleanMethod(attention, "setIsChecked", false);
            // if (Tools.isEmpty(getStringMethod(attention, "getIs_read"))) {
            // setBooleanMethod(attention, "setIsAttention", false);
            // } else if (getStringMethod(attention, "getIs_read").equals("1")) {
            // setBooleanMethod(attention, "setIsAttention", true);
            // } else {
            // setBooleanMethod(attention, "setIsAttention", false);
            // }
            boolean field = false;
            if (!Tools.isEmpty(getStringMethod(attention, "getIs_public"))
                    && getStringMethod(attention, "getIs_public").equals("0")) {
                if (!Tools.isEmpty(getStringMethod(attention, "getStatus"))
                        && getStringMethod(attention, "getStatus").equals("1")) {
                    field = true;
                }

            }

            setBooleanField(attention, field,
                    false, false, false);
            // AppContext.db.save(attention);
        }
    }

    public Object updateModel(Class<?> clazz, String str) {
        List<?> list = AppContext.db.findAllByWhere(clazz, "id='" + str + "'");
        if (list != null && list.size() > 0) {
            return list.get(0);
        } else {
            return null;
        }

        /*
         * if (attention == null) throw new NullPointerException("对象初始化错误"); List<T> funds = (List<T>)
         * AppContext.db.findAllByWhere(modelDb, "id='" + getStringMethod(attention, "getId") + "'"); if (funds != null
         * && funds.size() > 0) { setBooleanMethod(attention, "setIsEdit", getBooleanMethod(funds.get(0), "getIsEdit"));
         * setBooleanMethod(attention, "setIsChecked", getBooleanMethod(funds.get(0), "getIsChecked"));
         * setBooleanMethod(attention, "setIsAttention", getBooleanMethod(funds.get(0), "getIsAttention")); } else {
         * setBooleanMethod(attention, "setIsEdit", false); setBooleanMethod(attention, "setIsChecked", false);
         * setBooleanMethod(attention, "setIsAttention", false); AppContext.db.save(attention); }
         */
    }

    public void updateData() {
        updateDatabase();
    }

    public void savePub(List<T> newDatas) {
        if (newDatas == null || newDatas.size() == 0) {
            return;
        }
        try {
            // Class<?> clazz = newDatas.get(0).getClass();
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {
                    setDbBooleanField(dbList.get(i), getDbStringMethod(dbList.get(i), "getId"),
                            getBooleanMethod(dbList.get(i), "getIsAttention"), false,
                            true);
                    // setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    // setDbBooleanMethod(dbList.get(i), "setIsChecked",
                    // !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    // saveOrUpdateDb(dbList.get(i),
                    // getDbStringMethod(dbList.get(i), "getId"));
                }

            }

            AppContext.db.deleteByWhere(newDatas.get(0).getClass(),
                    "is_public=" + "'0'");

            for (int i = 0; i < newDatas.size(); i++) {

                // newModelDb(obj); T
                T attention = newDatas.get(i);
                // List<?> objs = AppContext.db.findAllByWhere(modelDb, "id='"
                // + getStringMethod(attention, "getId") + "'");
                // if (objs != null && objs.size() > 0) {
                // obj = objs.get(0);
                // } else {
                // obj = modelDb.newInstance();
                // }
                // if(getLongMethod(attention,"getMsg_time") ) {
                //
                // }
                if (isPub(attention)) {

                    Object obj = modelDb.newInstance();

                    updateModel(attention);
                    setDbStringMethod(obj, "setIs_public", getStringMethod(attention, "getIs_public"));
                    setDbStringMethod(obj, "setId", getStringMethod(attention, "getId"));
                    setDbBooleanField(obj, getStringMethod(attention, "getId"),
                            getBooleanMethod(attention, "getIsAttention"), false,
                            getBooleanMethod(attention, "getIsChecked"));
                    // setDbBooleanMethod(obj, "setIsAttention", false);
                    // setDbBooleanMethod(obj, "setIsChecked", false);
                    // setDbBooleanMethod(obj, "setIsEdit", false);
                    // setDbStringMethod(obj, "setId",
                    // getStringMethod(attention, "getId"));
                    // saveOrUpdateDb(obj, getStringMethod(attention, "getId"));
                    setStringMethod(attention, "setTime",
                            Tools.longToDateBySec(getLongMethod(attention, "getMsg_time")));
                    saveOrUpdate(attention, getStringMethod(attention, "getId"));
                } else {
                    deleteDb(modelDb, getStringMethod(attention, "getId"));
                }

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

    public void updateData(List<T> newDatas) {

        updateDatabase(newDatas);
    }

    public List<T> getDatasByOrder(Class<T> clazz, String str) {
        checkTable();
        List<T> list = AppContext.db.findAll(clazz, str);
        if (list != null && list.size() > 0) {
            List<T> realList = new ArrayList<T>();
            for (int i = 0; i < list.size(); i++) {

                if (isDelete(list.get(i))) {
                    delete(list.get(i), getStringMethod(list.get(i), "getId"));
                    deleteDb(modelDb, getStringMethod(list.get(i), "getId"));
                } else {
                    realList.add(list.get(i));
                }

            }
            return realList;
        }

        return list;
    }

    private void updateDatabase() {
        try {
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {

                    // Object obj;
                    setDbBooleanField(dbList.get(i), getDbStringMethod(dbList.get(i), "getId"),
                            getBooleanMethod(dbList.get(i), "getIsAttention"), false,
                            true);
                    // setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    // setDbBooleanMethod(dbList.get(i), "setIsChecked",
                    // !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    // saveOrUpdateDb(dbList.get(i),
                    // getDbStringMethod(dbList.get(i), "getId"));

                }
            }

        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    // private void updateDatabase(List<T> newDatas) {
    // if (newDatas == null) {
    // updateDatabase();
    // return;
    // }
    // try {
    // if (dbList != null && dbList.size() > 0) {
    // for (int i = 0; i < dbList.size(); i++) {
    // setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
    // setDbBooleanMethod(dbList.get(i), "setIsChecked",
    // !getBooleanMethod(dbList.get(i), "getIsChecked"));
    // saveOrUpdateDb(dbList.get(i),
    // getDbStringMethod(dbList.get(i), "getId"));
    // }
    //
    // }
    //
    // List<?> modelDbs = AppContext.db.findAll(modelDb);
    // if (modelDbs != null) {
    // for (int i = 0; i < modelDbs.size(); i++) {
    //
    // AppContext.db.deleteByWhere(
    // modelDb,
    // "id='"
    // + getDbStringMethod(modelDbs.get(i),
    // "getId") + "'" + " and "+"is_public="+"'1'");
    //
    // }
    // }
    //
    // for (int i = 0; i < newDatas.size(); i++) {
    //
    // Object obj = modelDb.newInstance(); // newModelDb(obj); T
    // T attention = newDatas.get(i);
    // List<?> objs =
    // AppContext.db.findAllByWhere(modelDb,"id='"+getStringMethod(attention,
    // "getId")+"'");
    // if(objs!=null&&objs.size()>0) {
    // obj = objs.get(0);
    // }else {
    // obj = modelDb.newInstance();
    // }
    // saveOrUpdate(attention,
    // getStringMethod(attention, "getId"));
    //
    // if(Tools.isEmpty(getStringMethod(attention, "getIs_read"))) {
    // setDbBooleanMethod(obj, "setIsAttention", false);
    // setDbBooleanMethod(obj, "setIsChecked", false);
    // }else if(getStringMethod(attention, "getIs_read").equals("1")){
    // setDbBooleanMethod(obj, "setIsAttention", true);
    // setDbBooleanMethod(obj, "setIsChecked", true);
    // }else if(getStringMethod(attention, "getIs_read").equals("0")) {
    // setDbBooleanMethod(obj, "setIsAttention", false);
    // setDbBooleanMethod(obj, "setIsChecked", false);
    // }
    //
    // setDbBooleanMethod(obj, "setIsEdit", false);
    // setDbStringMethod(obj, "setId",
    // getStringMethod(attention, "getId"));
    //
    // // setDbStringMethod(obj, "setVendor_id",
    // // getStringMethod(attention, "getVendor_id"));
    // saveOrUpdateDb(obj, getStringMethod(attention, "getId"));
    //
    // }
    //
    // } catch (IllegalArgumentException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (IllegalAccessException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // } catch (InstantiationException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // }

    private void setDbBooleanField(Object obj, String id, boolean isAttention, boolean isEdit, boolean isChecked) {
        setDbBooleanMethod(obj, "setIsEdit", isEdit);
        setDbBooleanMethod(obj, "setIsChecked", isChecked);
        setDbBooleanMethod(obj, "setIsAttention", isAttention);
        saveOrUpdateDb(obj, id);
    }

    // private void setDbBooleanField(Object obj, boolean isAttention, boolean isEdit, boolean isChecked, boolean
    // isSave) {
    //
    // if (isSave) {
    // setDbBooleanField(obj,isAttention,isEdit,isChecked);
    // } else {
    // setDbBooleanMethod(obj, "setIsEdit", isEdit);
    // setDbBooleanMethod(obj, "setIsChecked", isChecked);
    // setDbBooleanMethod(obj, "setIsAttention", isAttention);
    // }
    //
    // }

    private void setBooleanField(T obj, boolean isAttention, boolean isEdit, boolean isChecked) {
        setBooleanMethod(obj, "setIsEdit", isEdit);
        setBooleanMethod(obj, "setIsChecked", isChecked);
        setBooleanMethod(obj, "setIsAttention", isAttention);
        saveOrUpdate(obj, getDbStringMethod(obj, "getId"));
    }

    private void setBooleanField(T obj, boolean isAttention, boolean isEdit, boolean isChecked, boolean isSave) {
        if (isSave) {
            setBooleanField(obj, isAttention, isEdit, isChecked);
        } else {
            setDbBooleanMethod(obj, "setIsEdit", isEdit);
            setDbBooleanMethod(obj, "setIsChecked", isChecked);
            setDbBooleanMethod(obj, "setIsAttention", isAttention);
        }

    }

    private boolean isDelete(T attention) {
        long curTime = System.currentTimeMillis();
        long expire_time = getLongMethod(attention, "getExpire_time") * 1000;
        if (isPub(attention)) {
            if (curTime > expire_time) {
                return true;
            }
        } else {
            if (!SapiAccountManager.getInstance().isLogin()) {
                return true;
            }
        }

        return false;

    }

    private boolean isPub(T attention) {
        if (!Tools.isEmpty(getStringMethod(attention, "getIs_public"))
                && getStringMethod(attention, "getIs_public").equals("0")) {
            return false;
        }
        return true;
    }

    private void updateDatabase(List<T> newDatas) {
        if (newDatas == null || newDatas.size() == 0) {
            updateDatabase();
            return;
        }
        if (clazz == null) {
            clazz = newDatas.get(0).getClass();
        }
        try {
            // Class<?> clazz = newDatas.get(0).getClass();
            if (dbList != null && dbList.size() > 0) {
                for (int i = 0; i < dbList.size(); i++) {
                    setDbBooleanField(dbList.get(i), getDbStringMethod(dbList.get(i), "getId"),
                            getBooleanMethod(dbList.get(i), "getIsAttention"), false,
                            true);
                    // setDbBooleanMethod(dbList.get(i), "setIsEdit", false);
                    // setDbBooleanMethod(dbList.get(i), "setIsChecked",
                    // !getBooleanMethod(dbList.get(i), "getIsChecked"));
                    // saveOrUpdateDb(dbList.get(i),
                    // getDbStringMethod(dbList.get(i), "getId"));
                }

            }

            List<T> modelDbs = (List<T>) AppContext.db.findAll(clazz);
            if (modelDbs != null) {
                for (int i = 0; i < modelDbs.size(); i++) {
                    if (!Tools.isEmpty(getStringMethod(modelDbs.get(i), "getIs_public"))) {
                        if(getStringMethod(modelDbs.get(i), "getIs_public").equals("0")) {
                            delete(modelDbs.get(i), getStringMethod(modelDbs.get(i), "getId"));
                            deleteDb(modelDb, getStringMethod(modelDbs.get(i), "getId"));
                        }
                   
                    }
                }
            }

            for (int i = 0; i < newDatas.size(); i++) {

                // newModelDb(obj); T
                T attention = newDatas.get(i);
                // List<?> objs = AppContext.db.findAllByWhere(modelDb, "id='"
                // + getStringMethod(attention, "getId") + "'");
                // if (objs != null && objs.size() > 0) {
                // obj = objs.get(0);
                // } else {
                // obj = modelDb.newInstance();
                // }
                // if(getLongMethod(attention,"getMsg_time") ) {
                //
                // }
                if (!isPub(attention)) {

                    Object obj = modelDb.newInstance();
                    boolean field = false;
                    if (!Tools.isEmpty(getStringMethod(attention, "getStatus"))
                            && getStringMethod(attention, "getStatus").equals("1")) {
                        field = true;
                    }
                    setDbStringMethod(obj, "setIs_public", getStringMethod(attention, "getIs_public"));
                    setDbStringMethod(obj, "setId", getStringMethod(attention, "getId"));
                    setDbBooleanField(obj, getStringMethod(attention, "getId"), field, false, field);

                    // setDbBooleanMethod(obj, "setIsEdit", false);
                    // setDbStringMethod(obj, "setId",
                    // getStringMethod(attention, "getId"));
                    // saveOrUpdateDb(obj, getStringMethod(attention, "getId"));
                } else {

                    Object obj = modelDb.newInstance();

                    updateModel(attention);
                    setDbStringMethod(obj, "setIs_public", getStringMethod(attention, "getIs_public"));
                    setDbStringMethod(obj, "setId", getStringMethod(attention, "getId"));
                    setDbBooleanField(obj, getStringMethod(attention, "getId"),
                            getBooleanMethod(attention, "getIsAttention"), false,
                            getBooleanMethod(attention, "getIsChecked"));
                    // setDbBooleanMethod(obj, "setIsAttention", false);
                    // setDbBooleanMethod(obj, "setIsChecked", false);
                    // setDbBooleanMethod(obj, "setIsEdit", false);
                    // setDbStringMethod(obj, "setId",
                    // getStringMethod(attention, "getId"));
                    // saveOrUpdateDb(obj, getStringMethod(attention, "getId"));

                }

                setStringMethod(attention, "setTime", Tools.longToDateBySec(getLongMethod(attention, "getMsg_time")));
                saveOrUpdate(attention, getStringMethod(attention, "getId"));
                // if (i == 0) {
                // if (Tools.isEmpty(getStringMethod(attention, "getId"))) {
                // BasePerference.getInstance()
                // .save("maxId", Integer.valueOf(getStringMethod(attention, "getId")));
                // }
                //
                // }

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
