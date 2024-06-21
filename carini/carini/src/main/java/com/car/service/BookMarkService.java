package com.car.service;

import java.util.List;
import java.util.Optional;

import com.car.dto.Board;
import com.car.dto.Bookmark;
import com.car.dto.Car;
import com.car.dto.Member;
public interface BookMarkService {

	List<Bookmark> findAllBookmarkCar(String id);

	List<Car> findAllCar(List<Bookmark> bookmarkCar_ID);

	boolean isBookmarkedByMember(String memberId, int carId);
	
	void findBookmarkByCarDelete(int carId, String member_id);

	void insertMember(Bookmark bookmark, Member user);

	Car selectCar(int carId);

	
}
