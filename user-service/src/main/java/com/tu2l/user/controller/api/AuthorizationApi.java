package com.tu2l.user.controller.api;

import com.tu2l.common.model.base.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/authorize")
public interface AuthorizationApi {
    ResponseEntity<BaseResponse> checkPermission();

}
