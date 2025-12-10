package com.zspt.blibli.admin.controller.requestParam;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SystemUserParam {

    private String username;

    private String password;
}
