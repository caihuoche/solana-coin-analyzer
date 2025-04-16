package com.creda.coin.price.service;

import com.creda.coin.price.util.JsonUtil;
import org.apache.commons.collections4.CollectionUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author gavin
 * @date 2024/11/09
 **/
public interface BaseDoris<T> {

    String getTableName();

    default boolean saveBatchStreamLoad(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return true;
        }
        DorisStreamLoadService.streamLoad(getTableName(), JsonUtil.toJson(entityList));
        return true;
    }

    default boolean saveOrUpdateBatchStreamLoad(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return true;
        }
        DorisStreamLoadService.streamLoad(getTableName(), JsonUtil.toJson(entityList));
        return true;
    }


    /**
     * 部分字段更新
     * @param entityList
     * @return
     */
    default boolean updateBatchStreamLoad(Collection<T> entityList) {
        if (CollectionUtils.isEmpty(entityList)) {
            return true;
        }

        // 1. 查出现有数据，假设实体类有一个方法可以获取主键（比如getUniqueId）
        List<T> existingData = getExistingDataByIds(entityList);

        // 2. 将现有数据和新数据合并
        Map<Object, T> existingDataMap = existingData.stream()
                .collect(Collectors.toMap(this::getUniqueId, data -> data));

        // 3. 新增和更新的数据
        List<T> toUpdate = new ArrayList<>();
        List<T> toInsert = new ArrayList<>();

        // 合并现有数据和更新数据，分为需要更新的和需要插入的
        for (T entity : entityList) {
            Object id = getUniqueId(entity);
            T existingEntity = existingDataMap.get(id);

            if (existingEntity != null) {
                // 如果存在，直接用新数据覆盖，添加到更新列表
                mergeFields(existingEntity, entity);
                toUpdate.add(existingEntity);
            } else {
                // 如果不存在，添加到插入列表
                toInsert.add(entity);
            }
        }

        // 4. 将合并后的数据转化为JSON，准备进行 Doris stream load 操作
        List<T> allEntities = new ArrayList<>();
        allEntities.addAll(toUpdate); // 添加更新数据
        allEntities.addAll(toInsert); // 添加插入数据

        // 5. 批量插入和更新都合并到一个批次中
        if (!allEntities.isEmpty()) {
            String jsonData = JsonUtil.toJson(allEntities);
            DorisStreamLoadService.streamLoad(getTableName(), jsonData);
        }

        return true;
    }


    // 假设这个方法是用来获取现有数据的，通过主键或其他条件
    default List<T> getExistingDataByIds(Collection<T> entityList){
        throw new UnsupportedOperationException("getExistingDataByIds is not implemented");
    }

    default  Object getUniqueId(T entity){
        throw new UnsupportedOperationException("getUniqueId is not implemented");
    }

    // 合并现有数据和新数据，假设只更新部分字段
    default void mergeFields(T existingEntity, T entityToUpdate) {
        if (existingEntity == null || entityToUpdate == null) {
            return;
        }

        Class<?> clazz = existingEntity.getClass();

        // 循环获取类及其父类的字段
        while (clazz != null) {
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true); // 设置为可访问，避免访问权限问题

                try {
                    // 获取字段的值
                    Object existingValue = field.get(existingEntity);
                    Object newValue = field.get(entityToUpdate);

                    // 如果字段值不相等，并且新值不为null，则更新
                    if (!Objects.equals(existingValue, newValue) && newValue != null) {
                        field.set(existingEntity, newValue); // 更新现有字段的值
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace(); // 反射过程中可能会抛出异常，记录日志并跳过
                }
            }

            // 获取父类，继续处理
            clazz = clazz.getSuperclass();
        }
    }

}