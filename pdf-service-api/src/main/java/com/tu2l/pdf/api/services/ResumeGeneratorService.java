package com.tu2l.pdf.api.services;

import com.tu2l.pdf.api.models.base.BaseRequest;
import com.tu2l.pdf.api.models.base.BaseResponse;

public interface ResumeGeneratorService <Request extends BaseRequest, Response extends BaseResponse>  {
    Response generateResume(Request resumeData);
}
