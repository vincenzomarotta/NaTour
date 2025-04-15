package com.example.natour.utils;

import java.util.HashMap;
import java.util.Map;

public class SimplyStoreManager {
    private static SimplyStoreManager instance;
    private static Map<String,Object> map;

    private SimplyStoreManager(){}

    /**
     * Return the instance of SimplyStoreManager.
     * @return
     */
    public static SimplyStoreManager getInstance(){
        if(instance == null){
            instance = new SimplyStoreManager();
            map = new HashMap<>();
        }
        return instance;
    }

    /**
     * Verify the existence of a specified key into the store manager.
     * @param key reference name string
     * @return boolean value
     */
    public boolean containsKey(String key){
        if(map.containsKey(key))
            return true;
        else
            return false;
    }

    /**
     * Add a object reference to store manager.
     * If the return value is true the storage was successful, otherwise an object with the same key
     * is already present.
     * @param key reference name string
     * @param object object to store
     * @return boolean value
     */
    public boolean putObject(String key, Object object){
        if(!map.containsKey(key)) {
            map.put(key, object);
            return true;
        }
        else
            return false;
    }

    /**
     * He takes the object marked with the key.
     * If the object is not present in storage, the method returns null.
     * @param key reference name string
     * @return object reference or null
     */
    public Object getObject(String key){
        if(map.containsKey(key))
            return map.get(key);
        else
            return null;
    }

    /**
     * He takes the object marked with the key and deletes the object from storage.
     * If the object is not present in storage, the method returns null.
     * @param key
     * @return
     */
    public Object getObjectAndDelete(String key){
        Object object = null;
        if(map.containsKey(key)) {
            object = map.get(key);
            map.remove(key);
        }
        return object;
    }

    /**
     * Erase all keys and objects from storage.
     */
    public void clearStorage(){
        map.clear();
    }
}
