package com.speakBuddy.speackBuddy_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class MarkNotificationsReadRequest {

    private List<Long> ids;
}
