package com.project.mentoridge.modules.upload.controller;

import com.project.mentoridge.config.security.CurrentUser;
import com.project.mentoridge.modules.account.vo.User;
import com.project.mentoridge.modules.upload.controller.request.UploadImageRequest;
import com.project.mentoridge.modules.upload.controller.response.UploadResponse;
import com.project.mentoridge.modules.upload.service.UploadService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = {"UploadController"})
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    // TODO - application.yml
    public static final String DIR = "image";

    private final UploadService uploadService;

    /*
    [2022-06-24 00:16:56:548406][http-nio-8080-exec-4] WARN  o.s.w.s.m.s.DefaultHandlerExceptionResolver
    - Resolved [org.springframework.web.HttpMediaTypeNotSupportedException: Content type 'application/json' not supported]
    */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> uploadImage(@CurrentUser User user, @ModelAttribute UploadImageRequest uploadImageRequest) {
/*
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }*/
        UploadResponse upload = uploadService.uploadImage(DIR, uploadImageRequest.getFile());
        return ResponseEntity.ok(upload);
    }

}
