package com.boulangerie.shared.service;

import com.boulangerie.shared.dto.UserSummary;

public interface UserDirectoryService {

    UserSummary getUser(String id);

}