package com.zspt.blibli.common.utils;

import com.zspt.blibli.account.controller.requestParam.UserParam;

public class UserHolder {
  private static final   ThreadLocal<UserParam> threadLocal = new ThreadLocal<>();
  public static UserParam getUserId() {
      return threadLocal.get();
  }
  public static void setUserId(UserParam user) {
      threadLocal.set(user);
  }
  public static void removeUserId() {
      threadLocal.remove();
  }
}
