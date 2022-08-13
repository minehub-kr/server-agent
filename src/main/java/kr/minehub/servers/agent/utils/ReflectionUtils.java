package kr.minehub.servers.agent.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class ReflectionUtils {
  /* Welcome to Reflection HELL */

  public static boolean hasMethod(Class<?> clazz, String method, Class<?> ...args) {
    return getMethod(clazz, method) != null;
  }

  public static Method getMethod(Class<?> clazz, String method, Class<?> ...args) {
      try {
          return clazz.getDeclaredMethod(method, args);
      } catch(NoSuchMethodException e) {
          return null;
      }
  }

  public static<T extends Object> T callMethod(Object instance, String method, Object ...args) {
      try {
          Class<?> clazz = (Class<?>) instance.getClass();

          List<Class<?>> clazzList = new ArrayList<Class<?>>();
          for (Object obj: args) {
              clazzList.add(obj.getClass());
          }

          Class<?>[] list = clazzList.toArray(new Class<?>[0]);

          Method methodInstance = getMethod(clazz, method, list);
          if (methodInstance == null) return null;
          
          methodInstance.setAccessible(true);
          T result = (T) methodInstance.invoke(instance, args);

          return result;
      } catch(InvocationTargetException | IllegalAccessException e) {
          return null;
      }
  }
}
