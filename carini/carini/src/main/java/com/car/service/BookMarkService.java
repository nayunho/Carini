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

<<<<<<< HEAD

<<<<<<< HEAD
   Car selectCar(int carId);

   
=======
	int countBookmarkById(String memberId);

	
>>>>>>> upstream/main
=======
   Car selectCar(int carId);

   int countBookmarkById(String memberId);

>>>>>>> c2c34a4ff92ef5883d1d4688cf5e3b0e1a81b0ac
}
