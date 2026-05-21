package com.example.admissions_management.application.service;

import com.example.admissions_management.application.dto.request.ScoreCalculationRequest;
import com.example.admissions_management.application.dto.response.ScoreResultResponse;
import com.example.admissions_management.application.service.candidate.OptionItem;

import java.util.List;

public interface VsatScoreService {

    List<OptionItem> getMajorOptions();

    List<OptionItem> getPriorityObjectOptions();

    List<OptionItem> getPriorityRegionOptions();

    List<OptionItem> getBonusSubjectOptions();

    List<String> getConvertibleSubjectCodes();

    ScoreResultResponse calculate(ScoreCalculationRequest request);
}