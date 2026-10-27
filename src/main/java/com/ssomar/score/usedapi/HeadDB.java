package com.ssomar.score.usedapi;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.bukkit.inventory.ItemStack;
import tsp.headdb.api.HeadAPI;

import java.lang.reflect.Method;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class HeadDB {

    public static ItemStack getHead(int id) {

        Method getByID = null;
        for (Method method : HeadAPI.class.getDeclaredMethods()) {
            if (method.getName().equals("getHeadByID")) {
                getByID = method;
                break;
            }
        }

        try {
            tsp.headdb.api.Head head = (tsp.headdb.api.Head) getByID.invoke(null, id);
            return head.getItemStack();
        } catch (Exception ignored) {
        } catch (Error ignored) {
        }

        try {
            tsp.headdb.implementation.Head head = (tsp.headdb.implementation.Head) getByID.invoke(null, id);
            return head.getMenuItem();
        } catch (Exception ignored) {
        } catch (Error ignored) {
        }
        return null;
    }

    public static String getBase64(int id) {

        Method getByID = null;
        for (Method method : HeadAPI.class.getDeclaredMethods()) {
            if (method.getName().equals("getHeadByID")) {
                getByID = method;
                break;
            }
        }

        try {
            tsp.headdb.api.Head head = (tsp.headdb.api.Head) getByID.invoke(null, id);
            return head.getValue();
        } catch (Exception ignored) {
        } catch (Error ignored) {
        }

        try {
            tsp.headdb.implementation.Head head = (tsp.headdb.implementation.Head) getByID.invoke(null, id);
            return head.getValue();
        } catch (Exception ignored) {
        } catch (Error ignored) {
        }
        return "";
    }
}