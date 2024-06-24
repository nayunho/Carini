package com.car.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.car.dto.Agency;

@Service
public interface AgencyService {

	List<Agency> findagency(String carBrand);
	
}
