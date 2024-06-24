package com.car.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.car.dto.Agency;
import com.car.persistence.AgencyRepository;
import com.car.service.AgencyService;

@Service
public class AgencyServiceImpl implements AgencyService{

	
	@Autowired
	private AgencyRepository agencyRepository;

	@Override
	public List<Agency> findagency(String carBrand) {
		 
		List<Agency> agencies;
		if(carBrand.equals("")) {
			System.out.println("adsad");
			agencies= agencyRepository.findAll();
		}else {
			agencies= agencyRepository.findAllCarBrand(carBrand);
		}
		return agencies;
	}
	
	
}
